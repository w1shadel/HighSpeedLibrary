package com.maxwell.highspeedlib.logic;

import com.maxwell.highspeedlib.entity.ThrownCoinEntity;
import com.maxwell.highspeedlib.network.PacketHandler;
import com.maxwell.highspeedlib.network.packets.S2CParryPacket;
import com.maxwell.highspeedlib.network.packets.S2CScreenShakePacket;
import com.maxwell.highspeedlib.network.packets.S2CStartPunchAnimationPacket;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;

import java.util.List;

public class ParrySystem {
    private static final double PARRY_RANGE = 3.5;

    public static void attemptParry(ServerPlayer player) {
        Level level = player.level();
        Vec3 eyePos = player.getEyePosition();
        Vec3 lookVec = player.getLookAngle();
        PacketHandler.INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
                new S2CStartPunchAnimationPacket(player.getId()));
        double range = 5.0;
        Vec3 sensorCenter = eyePos.add(lookVec.scale(2.0));
        AABB hitBox = new AABB(sensorCenter.subtract(3, 3, 3), sensorCenter.add(3, 3, 3));
        boolean success = false;
        List<Projectile> projectiles = level.getEntitiesOfClass(Projectile.class, hitBox);
        for (Projectile p : projectiles) {
            Vec3 toEntity = p.position().subtract(eyePos).normalize();
            double dot = lookVec.dot(toEntity);
            if (dot > 0.4) {
                performProjectileParry(p, player);
                success = true;
                break;
            }
        }
        List<ThrownCoinEntity> coins = level.getEntitiesOfClass(ThrownCoinEntity.class, hitBox);
        for (ThrownCoinEntity coin : coins) {
            if (coin.canBeParried()) {
                performCoinRichoshot(coin, player);
                coin.setParryCooldown(5);
                success = true;
                break;
            }
        }
        if (!success) {
            List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, hitBox, e -> e != player);
            for (LivingEntity target : targets) {
                Vec3 toEntity = target.position().subtract(eyePos).normalize();
                if (lookVec.dot(toEntity) > 0.5) {
                    performMeleeParry(target, player);
                    success = true;
                    break;
                }
            }
        }
        if (success) {
            PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new S2CParryPacket());
            PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new S2CScreenShakePacket(2.0f, 5));
        }
    }
    public static void performCoinRichoshot(ThrownCoinEntity coin, ServerPlayer player) {
        Level level = coin.level();

        List<LivingEntity> enemies = level.getEntitiesOfClass(LivingEntity.class,
                coin.getBoundingBox().inflate(20.0), e -> e != player && e.isAlive());

        LivingEntity target = enemies.stream()
                .min((e1, e2) -> Float.compare(e1.distanceTo(coin), e2.distanceTo(coin)))
                .orElse(null);

        if (target != null) {
            target.hurt(player.damageSources().magic(), 10.0f);
            ((ServerLevel)level).sendParticles(ParticleTypes.ELECTRIC_SPARK,
                    target.getX(), target.getEyeY(), target.getZ(), 10, 0.1, 0.1, 0.1, 0.1);

            level.playSound(null, coin.getX(), coin.getY(), coin.getZ(),
                    SoundEvents.ARROW_HIT_PLAYER, SoundSource.PLAYERS, 1.0f, 2.0f);
        }
        Vec3 look = player.getLookAngle();
        Vec3 teleportPos = player.getEyePosition().add(look.scale(1.5));
        coin.setPos(teleportPos.x, teleportPos.y, teleportPos.z);
        coin.shoot(look.x, look.y + 0.6, look.z, 0.8f, 0f);
        ((ServerLevel)level).sendParticles(ParticleTypes.FLASH, coin.getX(), coin.getY(), coin.getZ(), 1, 0, 0, 0, 0);
    }
    public static void performCoinPunch(ThrownCoinEntity coin, LivingEntity attacker) {
        Level level = coin.level();

        List<LivingEntity> enemies = level.getEntitiesOfClass(LivingEntity.class,
                coin.getBoundingBox().inflate(20.0), e -> e != attacker && e.isAlive());

        LivingEntity target = enemies.stream()
                .min((e1, e2) -> Float.compare(e1.distanceTo(coin), e2.distanceTo(coin)))
                .orElse(null);

        if (target != null) {

            float damage = 5.0f + (coin.getParryCount() * 2.0f);
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

        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.FLASH, coin.getX(), coin.getY(), coin.getZ(), 1, 0, 0, 0, 0);
        }
    }

    public static void spawnBeam(ServerLevel level, Vec3 start, Vec3 end) {
        Vec3 dir = end.subtract(start);
        double dist = dir.length();
        for (double i = 0; i < dist; i += 0.5) {
            Vec3 pos = start.add(dir.scale(i / dist));
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, pos.x, pos.y, pos.z, 1, 0, 0, 0, 0);
        }
    }
    private static void performProjectileParry(Projectile p, ServerPlayer player) {
        Vec3 look = player.getLookAngle();
        p.setDeltaMovement(look.scale(3.0));
        p.setOwner(player);
        if (p instanceof AbstractArrow arrow) {
            arrow.setBaseDamage(arrow.getBaseDamage() * 2.0);
            arrow.setCritArrow(true);
        }
        ((ServerLevel) p.level()).sendParticles(ParticleTypes.EXPLOSION, p.getX(), p.getY(), p.getZ(), 1, 0, 0, 0, 0);
    }

    private static void performMeleeParry(LivingEntity target, ServerPlayer player) {
        target.hurt(player.damageSources().mobAttack(player), 10.0f);
        Vec3 knockback = player.getLookAngle().scale(2.0);
        target.setDeltaMovement(knockback.x, 0.5, knockback.z);
        player.heal(20.0f);
    }
}