package com.maxwell.highspeedlib.logic;

import com.maxwell.highspeedlib.HighSpeedLib;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WallJumpManager {
    private static final Map<UUID, Integer> wallJumpCounts = new HashMap<>();
    private static final int MAX_WALL_JUMPS = 3;

    public static void performWallJump(ServerPlayer player) {
        if (!player.onGround()) {
            int count = wallJumpCounts.getOrDefault(player.getUUID(), 0);
            if (count < MAX_WALL_JUMPS) {
                Direction wallDir = Events.getTouchingWall(player);

                if (wallDir != null) {

                    Vec3 push = new Vec3(wallDir.getOpposite().getStepX(), 0.0, wallDir.getOpposite().getStepZ()).normalize().scale(0.75D);
                    player.setDeltaMovement(push.x, 0.6D, push.z);
                    player.hurtMarked = true;

                    wallJumpCounts.put(player.getUUID(), count + 1);

                    player.level().playSound(null, player.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0f, 1.8f);
                }
            }
        }
    }

    @Mod.EventBusSubscriber(modid = HighSpeedLib.MODID)
    public static class Events {
        @SubscribeEvent
        public static void onPlayerTick(TickEvent.PlayerTickEvent event) {

            if (event.phase == TickEvent.Phase.END && event.player.onGround() && !event.player.level().isClientSide) {
                wallJumpCounts.put(event.player.getUUID(), 0);
            }
        }

        @SubscribeEvent
        public static void onLivingJump(LivingEvent.LivingJumpEvent event) {

            if (event.getEntity() instanceof Player player && !player.level().isClientSide) {
                wallJumpCounts.put(player.getUUID(), 0);
            }
        }

        @SubscribeEvent
        public static void onLivingFall(LivingFallEvent event) {

            if (event.getEntity() instanceof Player player && !player.level().isClientSide) {
                wallJumpCounts.put(player.getUUID(), 0);
            }
        }

        /**
         * プレイヤーが壁に触れているか多点チェック（足下、腰、頭上）
         */
        public static Direction getTouchingWall(Player player) {
            double[] heights = {0.1D, 1.0D, 1.8D}; 
            double offset = 0.42D; 

            for (Direction dir : Direction.Plane.HORIZONTAL) {
                for (double h : heights) {
                    BlockPos pos = BlockPos.containing(
                            player.getX() + dir.getStepX() * offset,
                            player.getY() + h,
                            player.getZ() + dir.getStepZ() * offset
                    );
                    if (player.level().getBlockState(pos).isCollisionShapeFullBlock(player.level(), pos)) {
                        return dir;
                    }
                }
            }
            return null;
        }
    }
}