package com.maxwell.highspeedlib.common.logic.combat;

import com.maxwell.highspeedlib.api.HighSpeedAbilityEvent;
import com.maxwell.highspeedlib.api.config.HighSpeedServerConfig;
import com.maxwell.highspeedlib.api.main.IHighSpeedInteractable;
import com.maxwell.highspeedlib.api.main.IParryable;
import com.maxwell.highspeedlib.client.state.ArmManager;
import com.maxwell.highspeedlib.common.entity.ThrownCoinEntity;
import com.maxwell.highspeedlib.common.logic.TimeManager;
import com.maxwell.highspeedlib.common.logic.ability.AbilityManager;
import com.maxwell.highspeedlib.common.logic.state.PlayerAbilityState;
import com.maxwell.highspeedlib.common.logic.state.PlayerCombatState;
import com.maxwell.highspeedlib.common.logic.state.PlayerStateManager;
import com.maxwell.highspeedlib.common.network.packets.action.S2CStartPunchAnimationPacket;
import com.maxwell.highspeedlib.common.network.packets.effect.S2CParryPacket;
import com.maxwell.highspeedlib.common.network.packets.effect.S2CScreenShakePacket;
import com.maxwell.highspeedlib.init.ModAttributes;
import com.maxwell.highspeedlib.init.ModDamageTypes;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

@EventBusSubscriber(modid = com.maxwell.highspeedlib.HighSpeedLib.MODID)
public class ServerArmManager {
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PlayerAbilityState state = PlayerStateManager.getState(player).getAbility();
            state.refreshFromConfig();
            AbilityManager.sync(player);
        }
    }

    public static boolean isPlayerParrying(LivingEntity entity) {
        if (!(entity instanceof Player player)) {
            return false;
        }
        return PlayerStateManager.getState(player).getCombat().activeParryWindow > 0;
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity().level().isClientSide) return;
        PlayerCombatState state = PlayerStateManager.getState(event.getEntity()).getCombat();
        if (state.activeParryWindow > 0) {
            state.activeParryWindow--;
        }
    }

    public static void attemptPunch(ServerPlayer player) {
        com.maxwell.highspeedlib.common.logic.combat.ArmType arm = com.maxwell.highspeedlib.client.state.ArmManager.getArm(player);
        if (NeoForge.EVENT_BUS.post(new HighSpeedAbilityEvent.Punch(player, arm)).isCanceled()) {
            return;
        }
        boolean isRed = (arm == com.maxwell.highspeedlib.common.logic.combat.ArmType.KNUCKLEBLASTER);
        if (!PunchCooldownManager.tryConsume(player, isRed)) {
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.DISPENSER_FAIL, SoundSource.PLAYERS, 1.0f, 2.0f);
            return;
        }
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, new S2CStartPunchAnimationPacket(player.getId()));
        if (isRed) {
            performKnuckleBlast(player);
        } else {
            int parryTicks = (int) Math.round(HighSpeedServerConfig.PARRY_INVUL_SECONDS.get() * 20.0);
            PlayerStateManager.getState(player).getCombat().activeParryWindow = parryTicks;
            performFeedbackerPunch(player);
        }
    }

    private static void performFeedbackerPunch(ServerPlayer player) {
        Level level = player.level();
        Vec3 eyePos = player.getEyePosition();
        Vec3 lookVec = player.getLookAngle();
        ArmType arm = ArmManager.getArm(player);
        boolean isRed = (arm == ArmType.KNUCKLEBLASTER);
        double range = 2.5;
        AABB searchBox = getForwardParryBox(player, range);
        List<Entity> allEntities = level.getEntities((Entity) null, searchBox, e -> e != player);
        boolean isProjectileParry = false;
        boolean isCoinPunch = false;
        List<Projectile> projectiles = level.getEntitiesOfClass(Projectile.class, searchBox,
                p -> !(p instanceof ThrownCoinEntity));
        for (Projectile p : projectiles) {
            if (p instanceof IParryable parryable && !parryable.canBeParried(player)) continue;
            performProjectileParry(p, player);
            isProjectileParry = true;
            break;
        }
        if (!isProjectileParry) {
            List<ThrownCoinEntity> coins = level.getEntitiesOfClass(ThrownCoinEntity.class, searchBox,
                    c -> isTargetable(c, eyePos, lookVec, range, 0.7));
            for (ThrownCoinEntity coin : coins) {
                if (coin.canBeParried()) {
                    performCoinPunch(coin, player);
                    coin.setParryCooldown(5);
                    isCoinPunch = true;
                    break;
                }
            }
        }
        if (!isProjectileParry && !isCoinPunch) {
            for (Entity entity : allEntities) {
                if (!(entity instanceof LivingEntity target)) continue;
                if (!isTargetable(target, eyePos, lookVec, range, 0.6)) continue;
                if (target instanceof IHighSpeedInteractable interactable) {
                    if (interactable.onHandPunch(player, !isRed)) {
                        triggerParryEffects(player);
                        return;
                    }
                }
                PlayerAbilityState settings = PlayerStateManager.getState(player).getAbility();
                double baseDamage = settings.punchDamageBase;
                double rawAD = player.getAttributeValue(Attributes.ATTACK_DAMAGE);
                double adBonus = (rawAD - 1.0) * HighSpeedServerConfig.PUNCH_AD_FACTOR.get();
                double velocity = player.getDeltaMovement().horizontalDistance();
                double velocityModifier = Math.min(HighSpeedServerConfig.PUNCH_VELOCITY_MAX_MODIFIER.get(), 1.0 + (velocity * HighSpeedServerConfig.PUNCH_VELOCITY_FACTOR.get()));
                double armMult = isRed ? HighSpeedServerConfig.PUNCH_KNUCKLE_DAMAGE_MULT.get() : HighSpeedServerConfig.PUNCH_FEEDBACKER_DAMAGE_MULT.get();
                float finalDamage = (float) ((baseDamage + adBonus) * armMult * velocityModifier);
                target.hurt(ModDamageTypes.feedbuckerAttack(level, target), finalDamage);
                target.setDeltaMovement(lookVec.scale(0.5).add(0, 0.1, 0));
                target.hurtMarked = true;
                if (level instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.CRIT, target.getX(), target.getEyeY(), target.getZ(), 5, 0.1, 0.1, 0.1, 0.1);
                }
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_WEAK, SoundSource.PLAYERS, 1.0f, 1.2f);
                break;
            }
        }
        if (isProjectileParry) {
            triggerParryEffects(player);
        } else if (isCoinPunch) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.IRON_GOLEM_HURT, SoundSource.PLAYERS, 0.7f, 2.0f);
        }
    }

    public static void performCoinPunch(ThrownCoinEntity coin, LivingEntity attacker) {
        Level level = coin.level();
        List<LivingEntity> enemies = level.getEntitiesOfClass(LivingEntity.class,
                coin.getBoundingBox().inflate(20.0), e -> e != attacker && e.isAlive());
        LivingEntity target = enemies.stream()
                .min((e1, e2) -> Float.compare(e1.distanceTo(coin), e2.distanceTo(coin)))
                .orElse(null);
        if (target != null) {
            float coinBase = HighSpeedServerConfig.COIN_BASE_DAMAGE.get().floatValue();
            float coinParryBonus = HighSpeedServerConfig.COIN_PARRY_DAMAGE_PER_COUNT.get().floatValue();
            float damage = coinBase + (coin.getParryCount() * coinParryBonus);
            target.hurt(attacker.damageSources().magic(), damage);
            Vec3 headPos = target.getEyePosition();
            coin.setPos(headPos.x, headPos.y, headPos.z);
            if (level instanceof ServerLevel serverLevel) {
                spawnBeam(serverLevel, attacker.getEyePosition(), headPos);
                serverLevel.sendParticles(ParticleTypes.FLASH, headPos.x, headPos.y, headPos.z, 1, 0, 0, 0, 0);
            }
            coin.setDeltaMovement(0, 0.5, 0);
        } else {
            Vec3 look = attacker.getLookAngle();
            Vec3 teleportPos = attacker.getEyePosition().add(look.scale(1.5));
            coin.setPos(teleportPos.x, teleportPos.y, teleportPos.z);
            coin.shoot(look.x, look.y + 0.5, look.z, 0.8f, 0f);
        }
        coin.increaseParryCount();
        coin.hurtMarked = true;
        level.playSound(null, coin.getX(), coin.getY(), coin.getZ(),
                SoundEvents.ARROW_HIT_PLAYER, SoundSource.PLAYERS, 1.0f, 2.0f);
    }

    private static AABB getForwardParryBox(ServerPlayer player, double range) {
        Vec3 eyePos = player.getEyePosition();
        Vec3 lookVec = player.getLookAngle();
        Vec3 center = eyePos.add(lookVec.scale(1.5));
        double boxSize = range;
        return new AABB(
                center.x - boxSize, center.y - boxSize, center.z - boxSize,
                center.x + boxSize, center.y + boxSize, center.z + boxSize
        );
    }

    private static void performKnuckleBlast(ServerPlayer player) {
        ServerLevel level = (ServerLevel) player.level();
        double range = 2.5;
        AABB searchBox = getForwardParryBox(player, range);
        List<Entity> allEntities = level.getEntities((Entity) null, searchBox, e -> e != player);
        PlayerAbilityState settings = PlayerStateManager.getState(player).getAbility();
        double baseDamage = settings.punchDamageBase;
        double punchAttr = player.getAttributeValue(ModAttributes.PUNCH_DAMAGE);
        double rawAD = player.getAttributeValue(Attributes.ATTACK_DAMAGE);
        double adBonus = (rawAD - 1.0) * HighSpeedServerConfig.PUNCH_AD_FACTOR.get();
        double velocity = player.getDeltaMovement().horizontalDistance();
        double velocityModifier = Math.min(HighSpeedServerConfig.PUNCH_VELOCITY_MAX_MODIFIER.get(), 1.0 + (velocity * HighSpeedServerConfig.PUNCH_VELOCITY_FACTOR.get()));
        double knuckleMult = HighSpeedServerConfig.PUNCH_KNUCKLE_DAMAGE_MULT.get();
        float finalBlastDamage = (float) ((baseDamage + punchAttr + adBonus) * knuckleMult * velocityModifier);
        Vec3 look = player.getLookAngle();
        Vec3 punchPos = player.getEyePosition().add(look.scale(1.5));
        double blastRadius = HighSpeedServerConfig.PUNCH_KNUCKLE_RADIUS.get();
        AABB area = new AABB(punchPos.subtract(blastRadius, blastRadius, blastRadius), punchPos.add(blastRadius, blastRadius, blastRadius));
        for (Entity entity : allEntities) {
            if (!(entity instanceof LivingEntity target)) continue;
            if (target instanceof IHighSpeedInteractable interactable) {
                if (interactable.onHandPunch(player, true)) {
                    continue;
                }
            }
            target.hurt(ModDamageTypes.blastAttack(level, target), finalBlastDamage);
            target.setDeltaMovement(look.scale(1.2).add(0, 0.4, 0));
            target.hurtMarked = true;
        }
        level.getEntitiesOfClass(Projectile.class, area).forEach(p -> {
            if (p.getOwner() != player) p.discard();
        });
        level.sendParticles(ParticleTypes.CRIT, punchPos.x, punchPos.y, punchPos.z, 10, 0.1, 0.1, 0.1, 0.1);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.0f, 1.2f);
    }

    private static boolean isTargetable(net.minecraft.world.entity.Entity entity, Vec3 eyePos, Vec3 lookVec, double range, double angleCos) {
        Vec3 toEntity = entity.position().add(0, entity.getBbHeight() * 0.5, 0).subtract(eyePos);
        double dist = toEntity.length();
        if (dist > range) return false;
        return lookVec.dot(toEntity.normalize()) > angleCos;
    }

    public static void performProjectileParry(Projectile p, Player player) {
        Vec3 look = player.getLookAngle();
        p.setPos(player.getX() + look.x, player.getEyeY() + look.y, player.getZ() + look.z);
        float parrySpeed = HighSpeedServerConfig.PROJECTILE_PARRY_SPEED.get().floatValue();
        p.shoot(look.x, look.y, look.z, parrySpeed, 0.0f);
        p.setOwner(player);
        if (!(p instanceof ThrownCoinEntity)) {
            p.getPersistentData().putBoolean("hs_explosive", true);
        }
    }

    public static void triggerParryEffects(ServerPlayer player) {
        TimeManager.setHitstop(HighSpeedServerConfig.PARRY_HITSTOP_TICKS.get());
        PacketDistributor.sendToPlayer(player, new S2CParryPacket());
        float shakePower = HighSpeedServerConfig.PARRY_SCREEN_SHAKE_POWER.get().floatValue();
        int shakeTicks = HighSpeedServerConfig.PARRY_SCREEN_SHAKE_TICKS.get();
        PacketDistributor.sendToPlayer(player, new S2CScreenShakePacket(shakePower, shakeTicks));
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ZOMBIE_ATTACK_IRON_DOOR, SoundSource.PLAYERS, 1.0f, 1.8f);
    }

    public static void spawnBeam(ServerLevel level, Vec3 start, Vec3 end) {
        Vec3 dir = end.subtract(start);
        double dist = dir.length();
        for (double i = 0; i < dist; i += 0.5) {
            Vec3 pos = start.add(dir.scale(i / dist));
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, pos.x, pos.y, pos.z, 1, 0, 0, 0, 0);
        }
    }

}
