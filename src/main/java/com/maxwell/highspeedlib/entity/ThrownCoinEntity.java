package com.maxwell.highspeedlib.entity;

import com.maxwell.highspeedlib.ModEntities;
import com.maxwell.highspeedlib.logic.ParrySystem;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class ThrownCoinEntity extends ThrowableItemProjectile {
    private int parryCooldown = 0;
    private int parryCount = 0;
    public ThrownCoinEntity(EntityType<? extends ThrownCoinEntity> type, Level level) {
        super(type, level);
    }
    public ThrownCoinEntity(Level level, LivingEntity shooter) {
        super(ModEntities.TCOIN.get(), shooter, level);
    }
    @Override
    public boolean isPickable() {
        return true; 
    }
    @Override
    protected Item getDefaultItem() {
        return Items.GOLD_NUGGET;
    }
    public boolean canBeParried() {
        return parryCooldown <= 0;
    }
    public void increaseParryCount() { this.parryCount++; }
    public int getParryCount() { return this.parryCount; }
    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.level().isClientSide || this.isRemoved()) return false;

        Entity attacker = source.getEntity();
        Entity directEntity = source.getDirectEntity();

        if (source.is(DamageTypes.PLAYER_ATTACK) || directEntity instanceof LivingEntity) {
            if (this.canBeParried() && attacker instanceof LivingEntity living) {
                ParrySystem.performCoinPunch(this, living);
                this.setParryCooldown(5);
                return true;
            }
        }

        if (directEntity instanceof Projectile) {
            performRicochet(this, attacker, amount);
            directEntity.discard();
            return true;
        }

        return super.hurt(source, amount);
    }
    public void setParryCooldown(int ticks) {
        this.parryCooldown = ticks;
    }
    @Override
    public void tick() {
        super.tick();
        if (parryCooldown > 0) parryCooldown--;
        if (this.level().isClientSide) {
            this.level().addParticle(ParticleTypes.WAX_ON, this.getX(), this.getY(), this.getZ(), 0, 0, 0);
        }
        if (!this.level().isClientSide) {
            if (Math.abs(this.getDeltaMovement().y) < 0.05) {
                ((ServerLevel)this.level()).sendParticles(ParticleTypes.FLASH, this.getX(), this.getY(), this.getZ(), 1, 0, 0, 0, 0);
            }
        }
    }

    @Override
    protected void onHit(HitResult result) {
        if (!this.level().isClientSide) {
            this.discard(); 
        }
    }
    public static void performRicochet(ThrownCoinEntity coin, Entity attacker, float damage) {
        // 1. すでに処理中、または削除済みの場合は何もしない（無限ループ防止）
        if (coin.isRemoved()) return;

        Level level = coin.level();
        if (!(level instanceof ServerLevel serverLevel)) return;
        coin.discard();
        List<ThrownCoinEntity> otherCoins = level.getEntitiesOfClass(ThrownCoinEntity.class,
                coin.getBoundingBox().inflate(15.0),
                e -> e != coin && !e.isRemoved());

        if (!otherCoins.isEmpty()) {
            // 次のコインへ連鎖
            ThrownCoinEntity nextCoin = otherCoins.get(0);
            ParrySystem.spawnBeam(serverLevel, coin.position(), nextCoin.position());
            performRicochet(nextCoin, attacker, damage + 5.0f);
        } else {
            List<LivingEntity> enemies = level.getEntitiesOfClass(LivingEntity.class,
                    coin.getBoundingBox().inflate(25.0), e -> e != attacker && e.isAlive());

            LivingEntity target = enemies.stream()
                    .min((e1, e2) -> Float.compare(e1.distanceTo(coin), e2.distanceTo(coin)))
                    .orElse(null);

            if (target != null) {
                ParrySystem.spawnBeam(serverLevel, coin.position(), target.getEyePosition());
                float finalDamage = damage + 10.0f + (coin.getParryCount() * 5.0f);
                target.hurt(level.damageSources().magic(), finalDamage);

                level.playSound(null, target.blockPosition(), SoundEvents.ZOMBIE_VILLAGER_CONVERTED, SoundSource.PLAYERS, 1.0f, 2.0f);
            }
        }
        level.playSound(null, coin.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0f, 1.5f);
    }
}