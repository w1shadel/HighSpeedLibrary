package com.maxwell.highspeedlib.common.logic.movement;

import com.maxwell.highspeedlib.api.config.HighSpeedServerConfig;
import com.maxwell.highspeedlib.common.logic.state.PlayerAbilityState;
import com.maxwell.highspeedlib.common.logic.state.PlayerMovementState;
import com.maxwell.highspeedlib.common.logic.state.PlayerStateManager;
import com.maxwell.highspeedlib.common.network.PacketHandler;
import com.maxwell.highspeedlib.common.network.packets.sync.S2CSyncSlamPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;

public class WallJumpManager {
    public static void tickWallJumpReseter(ServerPlayer player) {
        if (player.onGround()) {
            PlayerMovementState state = PlayerStateManager.getState(player).getMovement();
            if (state.wallJumpCount > 0) {
                state.wallJumpCount = 0;
            }
        }
    }
    public static void performWallJump(ServerPlayer player, boolean isStorageAttempt) {
        PlayerAbilityState settings = PlayerStateManager.getState(player).getAbility();
        PlayerMovementState state = PlayerStateManager.getState(player).getMovement();
        if (player.onGround()) {
            return;
        }
        if (net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new com.maxwell.highspeedlib.api.HighSpeedAbilityEvent.Walljump(player))) {
            return;
        }
        if (!settings.wallJump) return;
        if (player.isInWater() || player.isInLava()) return;
        double margin = isStorageAttempt ? 0.3D : 0.05D;
        Direction wallDir = getTouchingWall(player, margin);
        if (state.wallJumpCount < settings.maxWallJumpCount) {
            if (wallDir != null) {
                if (isStorageAttempt || state.isSlamming) {
                    state.slamStorageActive = true;
                    SlamManager.stopSlam(player);
                    PacketHandler.INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
                            new S2CSyncSlamPacket(player.getId(), false, true)); 
                }

                double baseJumpPower = player.getAttributeValue(Attributes.JUMP_STRENGTH);
                double jumpBoostBonus = 0.0D;
                if (player.hasEffect(MobEffects.JUMP)) {
                    jumpBoostBonus = (double) ((float) (player.getEffect(MobEffects.JUMP).getAmplifier() + 1) * 0.1F);
                }
                double boost = isStorageAttempt ? 1.2D : 1.0D;
                double finalJumpHeight = (baseJumpPower + jumpBoostBonus) * HighSpeedServerConfig.WALLJUMP_VERTICAL_MULT.get() * 0.8D * boost;
                double hPower = HighSpeedServerConfig.WALLJUMP_HORIZONTAL_POWER.get() * 1.4D;
                Vec3 push = new Vec3(wallDir.getOpposite().getStepX(), 0.0, wallDir.getOpposite().getStepZ())
                        .normalize().scale(hPower);
                player.setDeltaMovement(push.x, finalJumpHeight, push.z);
                player.hurtMarked = true;
                state.wallJumpCount++;
                player.connection.send(new ClientboundSetEntityMotionPacket(player));
                player.level().playSound(null, player.blockPosition(),
                        SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0f, 1.8f);
            }
        }
    }

    public static Direction getTouchingWall(Player player, double margin) {
        net.minecraft.world.phys.AABB playerBox = player.getBoundingBox();

        for (Direction dir : Direction.Plane.HORIZONTAL) {
            net.minecraft.world.phys.AABB detectionBox = switch (dir) {
                case NORTH -> new net.minecraft.world.phys.AABB(playerBox.minX, playerBox.minY, playerBox.minZ - margin, playerBox.maxX, playerBox.maxY, playerBox.minZ);
                case SOUTH -> new net.minecraft.world.phys.AABB(playerBox.minX, playerBox.minY, playerBox.maxZ, playerBox.maxX, playerBox.maxY, playerBox.maxZ + margin);
                case WEST -> new net.minecraft.world.phys.AABB(playerBox.minX - margin, playerBox.minY, playerBox.minZ, playerBox.minX, playerBox.maxY, playerBox.maxZ);
                case EAST -> new net.minecraft.world.phys.AABB(playerBox.maxX, playerBox.minY, playerBox.minZ, playerBox.maxX + margin, playerBox.maxY, playerBox.maxZ);
                default -> playerBox;
            };
            if (!player.level().noCollision(player, detectionBox)) {
                return dir;
            }
        }
        return null;
    }
}