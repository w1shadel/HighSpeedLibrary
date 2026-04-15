package com.maxwell.highspeedlib.common.logic.combat;

import com.maxwell.highspeedlib.api.HighSpeedAbilityEvent;
import com.maxwell.highspeedlib.api.config.HighSpeedServerConfig;
import com.maxwell.highspeedlib.api.main.IHighSpeedInteractable;
import com.maxwell.highspeedlib.api.main.IParryable;
import com.maxwell.highspeedlib.client.state.ArmManager;
import com.maxwell.highspeedlib.common.entity.ThrownCoinEntity;
import com.maxwell.highspeedlib.common.logic.TimeManager;
import com.maxwell.highspeedlib.common.logic.state.PlayerAbilityState;
import com.maxwell.highspeedlib.common.logic.state.PlayerCombatState;
import com.maxwell.highspeedlib.common.logic.state.PlayerStateManager;
import com.maxwell.highspeedlib.common.network.PacketHandler;
import com.maxwell.highspeedlib.common.network.packets.action.S2CStartPunchAnimationPacket;
import com.maxwell.highspeedlib.common.network.packets.effect.S2CParryPacket;
import com.maxwell.highspeedlib.common.network.packets.effect.S2CScreenShakePacket;
import com.maxwell.highspeedlib.init.ModAttributes;
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
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.util.List;

@Mod.EventBusSubscriber(modid = com.maxwell.highspeedlib.HighSpeedLib.MODID)
public class ServerArmManager {
    private static final double FEEDBACKER_RANGE = 2.5;

    public static boolean isPlayerParrying(LivingEntity entity) {
        if (!(entity instanceof Player player)) {
            return false;
        }
        return PlayerStateManager.getState(player).getCombat().activeParryWindow > 0;
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) return;
        PlayerCombatState state = PlayerStateManager.getState(event.player).getCombat();
        if (state.activeParryWindow > 0) {
            state.activeParryWindow--;
        }
    }

    public static void attemptPunch(ServerPlayer player) {
        com.maxwell.highspeedlib.common.logic.combat.ArmType arm = com.maxwell.highspeedlib.client.state.ArmManager.getArm(player);
        if (MinecraftForge.EVENT_BUS.post(new HighSpeedAbilityEvent.Punch(player, arm))) {
            return;
        }
        boolean isRed = (arm == com.maxwell.highspeedlib.common.logic.combat.ArmType.KNUCKLEBLASTER);
        if (!PunchCooldownManager.tryConsume(player, isRed)) {
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.DISPENSER_FAIL, SoundSource.PLAYERS, 1.0f, 2.0f);
            return;
        }
        PacketHandler.INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
                new S2CStartPunchAnimationPacket(player.getId()));
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
        double range = FEEDBACKER_RANGE;
        AABB searchBox = getForwardParryBox(player, range);
        List<Entity> allEntities = level.getEntities((Entity) null, searchBox, e -> e != player);
        boolean isSpecialHit = false;
        List<Projectile> projectiles = level.getEntitiesOfClass(Projectile.class, searchBox,
                p -> !(p instanceof ThrownCoinEntity));
        for (Projectile p : projectiles) {
            if (p instanceof IParryable parryable && !parryable.canBeParried(player)) {
                continue;
            }
            performProjectileParry(p, player);
            isSpecialHit = true;
            break;
        }
        if (!isSpecialHit) {
            List<ThrownCoinEntity> coins = level.getEntitiesOfClass(ThrownCoinEntity.class, searchBox, c -> isTargetable(c, eyePos, lookVec, range, 0.7));
            for (ThrownCoinEntity coin : coins) {
                if (coin.canBeParried()) {
                    performCoinPunch(coin, player);
                    coin.setParryCooldown(5);
                    isSpecialHit = true;
                    break;
                }
            }
        }
        if (!isSpecialHit) {
            List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, searchBox, e -> e != player && isTargetable(e, eyePos, lookVec, range, 0.6));
            for (Entity entity : allEntities) {
                if (!(entity instanceof LivingEntity target)) continue;
                if (!isTargetable(target, eyePos, lookVec, range, 0.6)) continue;
                if (target instanceof IHighSpeedInteractable interactable) {
                    if (interactable.onHandPunch(player, !isRed)) {
                        triggerParryEffects(player);
                        return;
                    }
                }
                double punchBase = player.getAttributeValue(ModAttributes.PUNCH_DAMAGE.get());
                double rawAD = player.getAttributeValue(Attributes.ATTACK_DAMAGE);
                double adFactor = 1.0 + (rawAD - 1.0) * 0.2;
                double velocity = player.getDeltaMovement().horizontalDistance();
                PlayerAbilityState settings = PlayerStateManager.getState(player).getAbility();
                double configBaseDamage = settings.punchDamageBase;
                double velocityModifier = Math.min(1.4, 1.0 + (velocity * 0.5));
                float finalDamage = (float) ((punchBase + configBaseDamage) * adFactor * 0.4 * velocityModifier);
//                AbsoluteHook.applyTrueDamage(target, finalDamage);
                target.setDeltaMovement(lookVec.scale(0.5).add(0, 0.1, 0));
                target.hurtMarked = true;
                if (level instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.CRIT, target.getX(), target.getEyeY(), target.getZ(), 5, 0.1, 0.1, 0.1, 0.1);
                }
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_WEAK, SoundSource.PLAYERS, 1.0f, 1.2f);
                break;
            }
        }
        if (isSpecialHit) {
            triggerParryEffects(player);
        }
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
        com.maxwell.highspeedlib.common.logic.combat.ArmType arm = com.maxwell.highspeedlib.client.state.ArmManager.getArm(player);
        ServerLevel level = (ServerLevel) player.level();
        Vec3 look = player.getLookAngle();
        Vec3 punchPos = player.getEyePosition().add(look.scale(1.5));
        double punchBase = player.getAttributeValue(ModAttributes.PUNCH_DAMAGE.get());
        double rawAD = player.getAttributeValue(Attributes.ATTACK_DAMAGE);
        double adFactor = 1.0 + (rawAD - 1.0) * 0.2;
        double velocity = player.getDeltaMovement().horizontalDistance();
        double velocityModifier = Math.min(1.4, 1.0 + (velocity * 0.5));
        PlayerAbilityState settings = PlayerStateManager.getState(player).getAbility();
        double configBaseDamage = settings.punchDamageBase;
        float blastDamage = (float) ((punchBase + configBaseDamage) * adFactor * 1.5 * velocityModifier);
        AABB area = new AABB(punchPos.subtract(2.5, 2.5, 2.5), punchPos.add(2.5, 2.5, 2.5));
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, area, e -> e != player);
        boolean anySpecialHandled = false;
        for (LivingEntity target : targets) {
            if (target instanceof IHighSpeedInteractable interactable) {
                if (interactable.onHandPunch(player, true)) {
                    anySpecialHandled = true;
                    continue;
                }
            }
//            AbsoluteHook.applyTrueDamage(target, blastDamage);
            target.setDeltaMovement(look.scale(1.2).add(0, 0.4, 0));
            target.hurtMarked = true;
        }
        if (anySpecialHandled) triggerParryEffects(player);
        level.getEntitiesOfClass(Projectile.class, area).forEach(p -> {
            if (p.getOwner() != player) p.discard();
        });
        level.sendParticles(ParticleTypes.CRIT, punchPos.x, punchPos.y, punchPos.z, 10, 0.1, 0.1, 0.1, 0.1);
        level.playSound(null, player.blockPosition(), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.0f, 1.2f);
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
        p.shoot(look.x, look.y, look.z, 3.5f, 0.0f);
        p.setOwner(player);
        if (!(p instanceof ThrownCoinEntity)) {
            p.getPersistentData().putBoolean("hs_explosive", true);
        }
    }

    public static void triggerParryEffects(ServerPlayer player) {
        TimeManager.setHitstop(5);
        PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new S2CParryPacket());
        PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new S2CScreenShakePacket(2.0f, 5));
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ZOMBIE_ATTACK_IRON_DOOR, SoundSource.PLAYERS, 1.0f, 1.8f);
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
            if (level instanceof ServerLevel serverLevel) {
                spawnBeam(serverLevel, coin.position(), target.getEyePosition());
            }
        }
        coin.increaseParryCount();
        Vec3 look = attacker.getLookAngle();
        Vec3 teleportPos = attacker.getEyePosition().add(look.scale(1.5));
        coin.setPos(teleportPos.x, teleportPos.y, teleportPos.z);
        coin.shoot(look.x, look.y + 0.5, look.z, 0.8f, 0f);
        level.playSound(null, coin.getX(), coin.getY(), coin.getZ(),
                SoundEvents.ARROW_HIT_PLAYER, SoundSource.PLAYERS, 1.0f, 2.0f);
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
