package com.maxwell.highspeedlib.common.logic;

import com.maxwell.highspeedlib.api.HighSpeedAbilityEvent;
import com.maxwell.highspeedlib.api.IHighSpeedInteractable;
import com.maxwell.highspeedlib.common.network.PacketHandler;
import com.maxwell.highspeedlib.common.network.packets.S2CSyncWhiplashPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@SuppressWarnings("removal")
public class ServerWhiplashManager {
    public static final int NONE = 0;
    public static final int FLYING = 1;
    public static final int HOOKED = 2;
    public static final int RETRACTING = 3;
    public static final double MAX_RANGE = 70.0;
    public static final double FLY_SPEED = 10.0;
    public static final double PULL_SPEED = 6.0;
    private static final Map<UUID, HookData> serverHooks = new HashMap<>();

    public static HookData getServerData(ServerPlayer player) {
        return serverHooks.computeIfAbsent(player.getUUID(), k -> new HookData());
    }

    public static void start(ServerPlayer player) {
        HookData data = getServerData(player);
        if (MinecraftForge.EVENT_BUS.post(new HighSpeedAbilityEvent.Whiplash(player))) {
            return;
        }
        if (data.state != NONE) return;
        data.state = FLYING;
        data.distance = 0;
        data.shootDir = player.getLookAngle();
        data.targetId = -1;
        data.hitPos = null;
        data.obstructionTicks = 0;
        sync(player, data);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                net.minecraft.sounds.SoundEvents.FISHING_BOBBER_THROW, net.minecraft.sounds.SoundSource.PLAYERS, 1.0f, 1.5f);
    }

    public static void stop(ServerPlayer player) {
        HookData data = getServerData(player);
        data.state = RETRACTING;
        sync(player, data);
    }

    public static void tickServer(ServerPlayer player) {
        HookData data = getServerData(player);
        if (data.state == NONE) return;
        Level level = player.level();
        Vec3 eyePos = player.getEyePosition();
        if (data.state == FLYING) {
            data.distance += FLY_SPEED;
            if (data.distance > MAX_RANGE) {
                data.state = RETRACTING;
                sync(player, data);
                return;
            }
            Vec3 rayEnd = eyePos.add(data.shootDir.scale(data.distance));
            Vec3 rayStart = eyePos.add(data.shootDir.scale(Math.max(0, data.distance - FLY_SPEED)));
            BlockHitResult blockHit = level.clip(new ClipContext(rayStart, rayEnd, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
            Vec3 targetEnd = blockHit.getType() != HitResult.Type.MISS ? blockHit.getLocation() : rayEnd;
            AABB aabb = new AABB(rayStart, targetEnd).inflate(2.0);
            double minDist = Double.MAX_VALUE;
            Entity hitEntity = null;
            for (Entity e : level.getEntities(player, aabb, ent -> ent.isAlive() && ent.isPickable())) {
                AABB eAABB = e.getBoundingBox().inflate(0.5);
                Optional<Vec3> optHit = eAABB.clip(rayStart, targetEnd);
                if (optHit.isPresent()) {
                    double dist = rayStart.distanceToSqr(optHit.get());
                    if (dist < minDist) {
                        minDist = dist;
                        hitEntity = e;
                    }
                }
            }
            if (hitEntity != null) {
                data.state = HOOKED;
                data.targetId = hitEntity.getId();
                data.hitPos = null;
                data.distance = eyePos.distanceTo(hitEntity.position());
                sync(player, data);
            } else if (blockHit.getType() == HitResult.Type.BLOCK) {
                BlockPos pos = blockHit.getBlockPos();
                BlockState state = level.getBlockState(pos);
                if (state.getBlock() instanceof IHighSpeedInteractable interactable) {
                    IHighSpeedInteractable.WhiplashReaction reaction = interactable.onWhiplash(player);
                    if (reaction == IHighSpeedInteractable.WhiplashReaction.PULL_PLAYER) {
                        data.state = HOOKED;
                        data.hitPos = blockHit.getLocation();
                        sync(player, data);
                        return;
                    } else if (reaction == IHighSpeedInteractable.WhiplashReaction.IGNORE) {
                        data.state = RETRACTING;
                        sync(player, data);
                        return;
                    }
                }
                data.state = RETRACTING;
                data.hitPos = blockHit.getLocation();
                data.distance = eyePos.distanceTo(data.hitPos);
                sync(player, data);
            }

        } else if (data.state == HOOKED) {
            Entity target = level.getEntity(data.targetId);
            if (target == null || !target.isAlive()) {
                data.state = RETRACTING;
                sync(player, data);
                return;
            }
            Vec3 targetMidPos = target.position().add(0, target.getBbHeight() * 0.5, 0);
            data.distance = eyePos.distanceTo(targetMidPos);
            double stopDistance = 2.0;
            if (data.distance <= stopDistance + 0.5) {
                player.setDeltaMovement(Vec3.ZERO);
                player.hurtMarked = true;
                if (target instanceof LivingEntity living) {
                    living.setDeltaMovement(Vec3.ZERO);
                    living.hurtMarked = true;
                }
                data.state = NONE;
                sync(player, data);
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        net.minecraft.sounds.SoundEvents.ITEM_FRAME_BREAK, net.minecraft.sounds.SoundSource.PLAYERS, 1.0f, 2.0f);
                return;
            }
            Vec3 dirToTarget = targetMidPos.subtract(eyePos).normalize();
            HitResult los = level.clip(new ClipContext(eyePos, targetMidPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
            if (los.getType() == HitResult.Type.BLOCK && los.getLocation().distanceTo(eyePos) < data.distance - 0.5) {
                data.obstructionTicks++;
                if (data.obstructionTicks > 10) {
                    data.state = RETRACTING;
                    sync(player, data);
                }
                return;
            } else {
                data.obstructionTicks = 0;
            }
            TagKey<EntityType<?>> bossTag = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation("forge:bosses"));
            boolean isBoss = target.getType().is(bossTag);
            boolean pullPlayer = target.getBbHeight() >= player.getBbHeight() || isBoss;
            double distToTravel = data.distance - stopDistance;
            if (pullPlayer) {
                double speed = Math.min(2.5, distToTravel);
                player.setDeltaMovement(dirToTarget.scale(speed));
                player.fallDistance = 0;
                player.hurtMarked = true;
            } else {
                if (target instanceof LivingEntity living) {
                    double speed = Math.min(2.8, distToTravel);
                    living.setDeltaMovement(dirToTarget.scale(-speed).add(0, 0.1, 0));
                    living.hurtMarked = true;
                }
            }
        } else if (data.state == RETRACTING) {
            data.distance -= PULL_SPEED;
            if (data.distance <= 0) {
                data.state = NONE;
                sync(player, data);
            }
        }
    }

    private static void sync(ServerPlayer player, HookData data) {
        PacketHandler.INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player), new S2CSyncWhiplashPacket(player.getUUID(), data.state, data.distance, data.targetId, data.hitPos, data.shootDir));
    }

    public static class HookData {
        public int state = NONE;
        public double distance = 0;
        public int targetId = -1;
        public Vec3 hitPos = null;
        public Vec3 shootDir = Vec3.ZERO;
        public int obstructionTicks = 0;
    }
}
