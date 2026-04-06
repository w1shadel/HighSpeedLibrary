package com.maxwell.highspeedlib.common.entity;

import com.maxwell.highspeedlib.init.ModEntities;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

@SuppressWarnings("removal")
public class WhiplashHookEntity extends Projectile {
    private static final EntityDataAccessor<Integer> OWNER_ID = SynchedEntityData.defineId(WhiplashHookEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> STATE = SynchedEntityData.defineId(WhiplashHookEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> TARGET_ID = SynchedEntityData.defineId(WhiplashHookEntity.class, EntityDataSerializers.INT);
    private static final int FLYING = 0;
    private static final int HOOKED = 1;
    private int maxTicks = 25;
    private int obstructionTicks = 0;

    public WhiplashHookEntity(EntityType<? extends Projectile> type, Level level) {
        super(type, level);
    }

    public WhiplashHookEntity(Level level, Player owner) {
        super(ModEntities.WHIPLASH_HOOK.get(), level);
        this.setOwner(owner);
        this.entityData.set(OWNER_ID, owner.getId());
        this.entityData.set(STATE, FLYING);
        Vec3 look = owner.getLookAngle();
        // 左手側(左肩付近)へオフセット (0.3m)
        Vec3 leftOffset = look.cross(new Vec3(0, 1, 0)).normalize().scale(-0.3);
        this.setPos(owner.getX() + leftOffset.x, owner.getEyeY() - 0.2 + leftOffset.y, owner.getZ() + leftOffset.z);
        this.shoot(look.x, look.y, look.z, 1.8F, 0.0F);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(OWNER_ID, -1);
        this.entityData.define(STATE, FLYING);
        this.entityData.define(TARGET_ID, -1);
    }

    public int getOwnerId() {
        return this.entityData.get(OWNER_ID);
    }

    private int getHookState() {
        return this.entityData.get(STATE);
    }

    private int getTargetId() {
        return this.entityData.get(TARGET_ID);
    }

    @Override
    public void tick() {
        super.tick();
        Level level = this.level();
        Entity owner = this.getOwner();
        if (owner == null && !level.isClientSide) {
            this.discard();
            return;
        }
        if (getHookState() == FLYING) {
            if (!level.isClientSide) {
                if (this.tickCount > maxTicks) {
                    this.discard();
                    return;
                }
                HitResult hitresult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
                if (hitresult.getType() != HitResult.Type.MISS) {
                    this.onHit(hitresult);
                }
            }
            Vec3 vel = this.getDeltaMovement();
            this.setPos(this.getX() + vel.x, this.getY() + vel.y, this.getZ() + vel.z);
        } else if (getHookState() == HOOKED) {
            Entity target = level.getEntity(getTargetId());
            if (target == null || !target.isAlive()) {
                if (!level.isClientSide) this.discard();
                return;
            }
            this.setPos(target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ());
            if (!level.isClientSide && owner instanceof Player player) {
                Vec3 playerPos = player.getEyePosition();
                Vec3 targetPos = this.position();
                double dist = playerPos.distanceTo(targetPos);
                if (dist < 1.5) {
                    this.discard();
                    return;
                }
                Vec3 dirToTarget = targetPos.subtract(playerPos).normalize();
                Vec3 traceEnd = targetPos.subtract(dirToTarget.scale(0.5));
                HitResult los = level.clip(new ClipContext(playerPos, traceEnd,
                        ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
                if (los.getType() == HitResult.Type.BLOCK) {
                    obstructionTicks++;
                    if (obstructionTicks > 20) {
                        this.discard();
                    }
                    return;
                } else {
                    if (obstructionTicks > 0) obstructionTicks--;
                }
                double playerHeight = player.getBbHeight();
                double targetHeight = target.getBbHeight();
                TagKey<EntityType<?>> bossTag = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation("forge", "bosses"));
                boolean isBoss = target.getType().is(bossTag);
                Vec3 pullDir = targetPos.subtract(player.position()).normalize();
                if (targetHeight >= playerHeight || isBoss) {
                    player.setDeltaMovement(pullDir.scale(1.0));
                    player.fallDistance = 0;
                    player.hurtMarked = true;
                } else {
                    if (target instanceof LivingEntity living) {
                        living.setDeltaMovement(pullDir.scale(-1.2).add(0, 0.2, 0));
                        living.hurtMarked = true;
                    }
                }
            }
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (getHookState() == FLYING && !level().isClientSide) {
            Entity target = result.getEntity();
            if (target != this.getOwner()) {
                this.entityData.set(STATE, HOOKED);
                this.entityData.set(TARGET_ID, target.getId());
                this.setDeltaMovement(Vec3.ZERO);
            }
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (result.getType() == HitResult.Type.BLOCK && getHookState() == FLYING && !level().isClientSide) {
            this.discard();
        }
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        return super.canHitEntity(entity) && entity != this.getOwner() && getHookState() == FLYING;
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag nbt) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag nbt) {
    }
}
