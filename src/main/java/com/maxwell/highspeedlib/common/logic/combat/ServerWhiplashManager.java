package com.maxwell.highspeedlib.common.logic.combat;

import com.maxwell.highspeedlib.api.HighSpeedAbilityEvent;
import com.maxwell.highspeedlib.api.main.IHighSpeedInteractable;
import com.maxwell.highspeedlib.common.logic.state.PlayerCombatState;
import com.maxwell.highspeedlib.common.logic.state.PlayerStateManager;
import com.maxwell.highspeedlib.common.network.PacketHandler;
import com.maxwell.highspeedlib.common.network.packets.sync.S2CSyncWhiplashPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractSkullBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.util.Optional;

@SuppressWarnings("removal")
@Mod.EventBusSubscriber(modid = com.maxwell.highspeedlib.HighSpeedLib.MODID)
public class ServerWhiplashManager {
    public static final int NONE = 0;
    public static final int FLYING = 1;
    public static final int HOOKED = 2;
    public static final int RETRACTING = 3;
    public static final double MAX_RANGE = 70.0;
    public static final double FLY_SPEED = 3.0;
    public static final double PULL_SPEED = 2.0;

    public static HookData getServerData(ServerPlayer player) {
        PlayerCombatState state = PlayerStateManager.getState(player).getCombat();
        if (state.hookData == null) {
            state.hookData = new HookData();
        }
        return state.hookData;
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

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) return;
        if (event.player instanceof ServerPlayer sp) {
            tickServer(sp);
        }
    }

    private static void tickServer(ServerPlayer player) {
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
            for (Entity e : level.getEntities(player, aabb, ent -> ent.isAlive() && (ent.isPickable() || ent instanceof ItemEntity))) {
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
                ItemStack offhandStack = player.getOffhandItem();
                if (!offhandStack.isEmpty() && offhandStack.getItem() instanceof BlockItem blockItem) {
                    BlockPlaceContext context =
                            new BlockPlaceContext(player, InteractionHand.OFF_HAND, offhandStack, blockHit);
                    InteractionResult result = blockItem.place(context);
                    if (result.consumesAction()) {
                        level.playSound(null, blockHit.getLocation().x, blockHit.getLocation().y, blockHit.getLocation().z,
                                SoundEvents.STONE_PLACE, SoundSource.BLOCKS, 1.0f, 1.5f);
                        data.state = RETRACTING;
                        data.hitPos = blockHit.getLocation();
                        data.distance = eyePos.distanceTo(data.hitPos);
                        sync(player, data);
                        return;
                    }
                }
                if (state.getBlock() instanceof AbstractSkullBlock) {
                    ItemStack itemstack = new ItemStack(state.getBlock());
                    BlockEntity be = level.getBlockEntity(pos);
                    if (be != null) {
                        CompoundTag tag = be.saveWithFullMetadata();
                        itemstack.getOrCreateTag().put("BlockEntityTag", tag);
                    }
                    level.removeBlock(pos, false);
                    ItemEntity itemEntity = new ItemEntity(level,
                            pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, itemstack);
                    itemEntity.setDeltaMovement(Vec3.ZERO);
                    level.addFreshEntity(itemEntity);
                    data.state = HOOKED;
                    data.targetId = itemEntity.getId();
                    data.distance = eyePos.distanceTo(itemEntity.position());
                    sync(player, data);
                    level.playSound(null, pos, net.minecraft.sounds.SoundEvents.ARROW_HIT, SoundSource.BLOCKS, 1.0f, 1.2f);
                    return;
                }
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
                if (target instanceof ItemEntity itemEntity) {
                    ItemStack stack = itemEntity.getItem();
                    ItemStack offhand = player.getOffhandItem();
                    if (offhand.isEmpty()) {
                        player.setItemInHand(InteractionHand.OFF_HAND, stack.copy());
                        itemEntity.discard();
                    } else {
                        if (player.getInventory().add(stack)) {
                            itemEntity.discard();
                        }
                    }
                    level.playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.5f, 1.5f);
                }
                player.setDeltaMovement(Vec3.ZERO);
                player.hurtMarked = true;
                if (target instanceof LivingEntity living) {
                    living.setDeltaMovement(Vec3.ZERO);
                    living.hurtMarked = true;
                }
                data.state = NONE;
                sync(player, data);
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
                double speed = Math.min(2.8, distToTravel);
                target.setDeltaMovement(dirToTarget.scale(-speed).add(0, 0.1, 0));
                target.hurtMarked = true;
            }
        } else if (data.state == RETRACTING) {
            data.distance -= PULL_SPEED;
            if (data.distance <= 0) {
                data.state = NONE;
                data.distance = 0;
                sync(player, data);
            } else {
                if (player.tickCount % 2 == 0) {
                    sync(player, data);
                }
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
