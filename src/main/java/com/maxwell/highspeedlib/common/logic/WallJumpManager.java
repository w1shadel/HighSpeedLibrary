package com.maxwell.highspeedlib.common.logic;

import com.maxwell.highspeedlib.HighSpeedLib;
import com.maxwell.highspeedlib.api.config.HighSpeedServerConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attributes;
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

    public static void performWallJump(ServerPlayer player) {
        AbilityAuthority.PlayerSettings settings = AbilityAuthority.get(player.getUUID());
        if (net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new com.maxwell.highspeedlib.api.HighSpeedAbilityEvent.Walljump(player))) {
            return;
        }
        if (!settings.wallJump) return;
        if (!player.onGround()) {
            int count = wallJumpCounts.getOrDefault(player.getUUID(), 0);
            if (count < settings.maxWallJumpCount) {
                Direction wallDir = Events.getTouchingWall(player);
                if (wallDir != null) {
                    double baseJumpPower = player.getAttributeValue(Attributes.JUMP_STRENGTH);
                    double jumpBoostBonus = 0.0D;
                    if (player.hasEffect(MobEffects.JUMP)) {
                        jumpBoostBonus = (double) ((float) (player.getEffect(MobEffects.JUMP).getAmplifier() + 1) * 0.1F);
                    }
                    double finalJumpHeight = (baseJumpPower + jumpBoostBonus) * HighSpeedServerConfig.WALLJUMP_VERTICAL_MULT.get();
                    double hPower = HighSpeedServerConfig.WALLJUMP_HORIZONTAL_POWER.get();
                    Vec3 push = new Vec3(wallDir.getOpposite().getStepX(), 0.0, wallDir.getOpposite().getStepZ())
                            .normalize().scale(hPower);
                    player.setDeltaMovement(push.x, finalJumpHeight, push.z);
                    player.hurtMarked = true;
                    wallJumpCounts.put(player.getUUID(), count + 1);
                    player.level().playSound(null, player.blockPosition(),
                            SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0f, 1.8f);
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