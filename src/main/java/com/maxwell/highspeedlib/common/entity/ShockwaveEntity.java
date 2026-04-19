package com.maxwell.highspeedlib.common.entity;

import com.maxwell.highspeedlib.init.ModEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ShockwaveEntity extends Entity {
    private static final EntityDataAccessor<Float> RADIUS = SynchedEntityData.defineId(ShockwaveEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> MAX_RADIUS = SynchedEntityData.defineId(ShockwaveEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> SPEED = SynchedEntityData.defineId(ShockwaveEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> HEIGHT = SynchedEntityData.defineId(ShockwaveEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> COLOR = SynchedEntityData.defineId(ShockwaveEntity.class, EntityDataSerializers.INT);
    private final Set<Entity> hitEntities = new HashSet<>();
    private float damage = 5.0f;
    private LivingEntity owner;

    public ShockwaveEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public ShockwaveEntity(Level level, LivingEntity owner, float damage, float maxRadius, float speed, float height, int color) {
        this(ModEntities.SHOCKWAVE.get(), level);
        this.owner = owner;
        this.damage = damage;
        this.setMaxRadius(maxRadius);
        this.setSpeed(speed);
        this.setHeight(height);
        this.setColor(color);
        this.setPos(owner.getX(), owner.getY(), owner.getZ());
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(RADIUS, 0.0f);
        this.entityData.define(MAX_RADIUS, 10.0f);
        this.entityData.define(SPEED, 0.5f);
        this.entityData.define(HEIGHT, 1.0f);
        this.entityData.define(COLOR, 0xFFFFFFFF); // デフォルトは白
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide) {
            float currentRadius = getRadius();
            float nextRadius = currentRadius + getSpeed();
            setRadius(nextRadius);
            if (nextRadius > getMaxRadius()) {
                this.discard();
                return;
            }
            checkCollisions(currentRadius, nextRadius);
        }
    }

    private void checkCollisions(float innerRadius, float outerRadius) {
        float h = getHeight(); // 同期された高さを使用
        // Y軸の判定を高さに基づいて拡張
        AABB searchBox = this.getBoundingBox().inflate(outerRadius, h, outerRadius);
        List<Entity> targets = this.level().getEntities(this, searchBox, EntitySelector.NO_SPECTATORS);

        for (Entity entity : targets) {
            if (entity == owner || hitEntities.contains(entity)) continue;

            double dist = Math.sqrt(this.distanceToSqr(entity));
            if (dist >= innerRadius && dist <= outerRadius) {
                // Y座標が衝撃波の底面(this.getY())から高さ(h)の範囲内にあるか判定
                if (entity.getY() < this.getY() + h && entity.getY() + entity.getBbHeight() > this.getY()) {
                    applyEffect(entity);
                    hitEntities.add(entity);
                }
            }
        }
    }


    protected void applyEffect(Entity target) {
        target.hurt(this.damageSources().magic(), this.damage);
        if (target instanceof LivingEntity living) {
            living.knockback(0.5, this.getX() - target.getX(), this.getZ() - target.getZ());
            living.setDeltaMovement(living.getDeltaMovement().add(0, 0.4, 0));
        }
    }

    public float getRadius() {
        return this.entityData.get(RADIUS);
    }

    public void setRadius(float r) {
        this.entityData.set(RADIUS, r);
    }

    public float getMaxRadius() {
        return this.entityData.get(MAX_RADIUS);
    }

    public void setMaxRadius(float r) {
        this.entityData.set(MAX_RADIUS, r);
    }

    public float getSpeed() {
        return this.entityData.get(SPEED);
    }

    public void setSpeed(float s) {
        this.entityData.set(SPEED, s);
    }
    public float getHeight() { return this.entityData.get(HEIGHT); }
    public void setHeight(float h) { this.entityData.set(HEIGHT, h); }
    public int getColor() { return this.entityData.get(COLOR); }
    public void setColor(int c) { this.entityData.set(COLOR, c); }
    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
    }
}