package com.maxwell.highspeedlib.common.entity;

import com.maxwell.highspeedlib.common.logic.combat.ServerArmManager;
import com.maxwell.highspeedlib.common.logic.util.AbsoluteDamageManager;
import com.maxwell.highspeedlib.init.ModEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

import java.util.List;

public class ThrownCoinEntity extends ThrowableItemProjectile {
    private int parryCooldown = 0;
    private int parryCount = 0;
    private int shiningTicks = 0;
    private boolean hasShone = false;

    public ThrownCoinEntity(EntityType<? extends ThrownCoinEntity> type, Level level) {
        super(type, level);
    }

    public ThrownCoinEntity(Level level, LivingEntity shooter) {
        super(ModEntities.TCOIN.get(), shooter, level);
    }

    public static void performRicochet(ThrownCoinEntity coin, Entity attacker, float incomingDamage, int chainCount) {
        if (coin.isRemoved()) return;
        Level level = coin.level();
        if (!(level instanceof ServerLevel serverLevel)) return;
        boolean isCrit = coin.isShining();
        int currentChain = chainCount + 1;
        coin.discard();
        List<ThrownCoinEntity> otherCoins = level.getEntitiesOfClass(ThrownCoinEntity.class,
                coin.getBoundingBox().inflate(15.0), e -> e != coin && !e.isRemoved());
        if (!otherCoins.isEmpty()) {
            ThrownCoinEntity nextCoin = otherCoins.get(0);
            ServerArmManager.spawnBeam(serverLevel, coin.position(), nextCoin.position());
            performRicochet(nextCoin, attacker, incomingDamage, currentChain);
        } else {
            List<LivingEntity> enemies = level.getEntitiesOfClass(LivingEntity.class,
                    coin.getBoundingBox().inflate(25.0), e -> e != attacker && e.isAlive());
            enemies.sort((e1, e2) -> Float.compare(e1.distanceTo(coin), e2.distanceTo(coin)));
            if (!enemies.isEmpty()) {
                float playerAtk = 1.0f;
                if (attacker instanceof LivingEntity living) {
                    playerAtk = (float) living.getAttributeValue(Attributes.ATTACK_DAMAGE);
                }
                float multiplier = currentChain + coin.getParryCount();
                float baseFinalDamage = (incomingDamage + playerAtk) * multiplier;
                int targetsToHit = (isCrit && enemies.size() >= 2) ? 2 : 1;
                float damageMultiplier = isCrit ? 2.0f : 1.0f;
                for (int i = 0; i < targetsToHit; i++) {
                    LivingEntity target = enemies.get(i);
                    ServerArmManager.spawnBeam(serverLevel, coin.position(), target.getEyePosition());
                    float finalDamage = baseFinalDamage * damageMultiplier;
                    AbsoluteDamageManager.dealAbsoluteDamage(target, finalDamage);
                    float pitch = isCrit ? 2.0f : 1.5f;
                    level.playSound(null, target.blockPosition(), SoundEvents.ZOMBIE_VILLAGER_CONVERTED, SoundSource.PLAYERS, 1.0f, pitch);
                }
            }
        }
        level.playSound(null, coin.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0f, 1.5f);
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

    public void increaseParryCount() {
        this.parryCount++;
    }

    public int getParryCount() {
        return this.parryCount;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.level().isClientSide || this.isRemoved()) return false;
        Entity attacker = source.getEntity();
        Entity directEntity = source.getDirectEntity();
        if (source.is(DamageTypes.PLAYER_ATTACK) || directEntity instanceof LivingEntity) {
            if (this.canBeParried() && attacker instanceof LivingEntity living) {
                ServerArmManager.performCoinPunch(this, living);
                this.setParryCooldown(5);
                return true;
            }
        }
        if (directEntity instanceof Projectile) {
            performRicochet(this, attacker, amount, 0);
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
        if (!this.level().isClientSide) {
            double vy = this.getDeltaMovement().y;
            if (Math.abs(vy) < 0.1 && !hasShone) {
                this.shiningTicks = 8;
                this.hasShone = true;
                ((ServerLevel) this.level()).sendParticles(ParticleTypes.FLASH, this.getX(), this.getY(), this.getZ(), 1, 0, 0, 0, 0);
                this.level().playSound(null, this.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0f, 2.0f);
            }
            if (shiningTicks > 0) shiningTicks--;
        }
        if (this.level().isClientSide && shiningTicks > 0) {
            this.level().addParticle(ParticleTypes.END_ROD, this.getX(), this.getY(), this.getZ(), 0, 0, 0);
        }
    }

    public boolean isShining() {
        return shiningTicks > 0;
    }

    @Override
    protected void onHit(HitResult result) {
        if (!this.level().isClientSide) {
            this.discard();
        }
    }
}