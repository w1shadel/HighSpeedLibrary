package com.maxwell.highspeedlib.common.logic.movement;

import com.maxwell.highspeedlib.HighSpeedLib;
import com.maxwell.highspeedlib.api.HighSpeedAbilityEvent;
import com.maxwell.highspeedlib.api.config.HighSpeedServerConfig;
import com.maxwell.highspeedlib.common.logic.state.PlayerMovementState;
import com.maxwell.highspeedlib.common.logic.state.PlayerStateManager;
import com.maxwell.highspeedlib.common.network.packets.sync.S2CSyncSlidePacket;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

public class SlideManager {
    public static void toggleSlide(ServerPlayer player, boolean start, float xInput, float zInput) {
        if (NeoForge.EVENT_BUS.post(new HighSpeedAbilityEvent.Sliding(player)).isCanceled()) {
            return;
        }
        PlayerMovementState state = PlayerStateManager.getState(player).getMovement();
        if (start) {
            if (!state.isSliding) {
                state.isSliding = true;
                Vec3 slideDir;
                if (xInput == 0 && zInput == 0) {
                    slideDir = new Vec3(player.getLookAngle().x, 0, player.getLookAngle().z).normalize();
                } else {
                    float yaw = player.getYRot();
                    float f1 = (float) Math.sin(yaw * (Math.PI / 180.0));
                    float f2 = (float) Math.cos(yaw * (Math.PI / 180.0));
                    slideDir = new Vec3(xInput * f2 - zInput * f1, 0, zInput * f2 + xInput * f1).normalize();
                }
                state.slideDir = slideDir;
            }
        } else {
            if (state.isSliding) {
                state.isSliding = false;
                state.slideDir = null;
            }
        }
        player.refreshDimensions();
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, new S2CSyncSlidePacket(player.getId(), start));
    }

    public static boolean isSliding(Player player) {
        if (player == null) return false;
        if (player.level().isClientSide) {
            return com.maxwell.highspeedlib.client.logic.ClientSlideHandler.isPlayerSliding(player.getId());
        }
        return PlayerStateManager.getState(player).getMovement().isSliding;
    }

    @EventBusSubscriber(modid = HighSpeedLib.MODID)
    public static class Events {
        @SubscribeEvent
        public static void onPlayerTick(PlayerTickEvent.Post event) {
            Player player = event.getEntity();
            if (player.level().isClientSide) return;
            if (isSliding(player)) {
                PlayerMovementState state = PlayerStateManager.getState(player).getMovement();
                if (!player.onGround()) {
                    state.airTicks++;
                    if (state.airTicks > HighSpeedServerConfig.SLIDE_AIR_TIMEOUT_TICKS.get()) {
                        toggleSlide((ServerPlayer) player, false, 0, 0);
                        state.airTicks = 0;
                        return;
                    }
                } else {
                    state.airTicks = 0;
                    if (player.tickCount % 2 == 0) {
                        ((ServerLevel) player.level()).sendParticles(
                                ParticleTypes.CRIT,
                                player.getX(), player.getY(), player.getZ(),
                                1, 0, 0, 0, 0.05
                        );
                    }
                    Vec3 slideDir = state.slideDir != null ? state.slideDir : new Vec3(player.getLookAngle().x, 0, player.getLookAngle().z).normalize();
                    double slideSpeed = HighSpeedServerConfig.SLIDE_SPEED.get();
                    player.setDeltaMovement(slideDir.x * slideSpeed, player.getDeltaMovement().y, slideDir.z * slideSpeed);
                    player.hurtMarked = true;
                }
            }
        }

        @SubscribeEvent
        public static void onLivingJump(LivingEvent.LivingJumpEvent event) {
            if (event.getEntity() instanceof Player player && isSliding(player)) {
                if (!player.level().isClientSide) {
                    PlayerMovementState state = PlayerStateManager.getState(player).getMovement();
                    Vec3 motion = player.getDeltaMovement();
                    double hSpeed = Math.sqrt(motion.x * motion.x + motion.z * motion.z);
                    double jumpPower = HighSpeedServerConfig.SLIDE_JUMP_VERTICAL_BASE.get()
                            + (hSpeed * HighSpeedServerConfig.SLIDE_JUMP_VERTICAL_SPEED_MULT.get());
                    double hMult = (state.slamImpactTimer > 0)
                            ? HighSpeedServerConfig.SLAM_JUMP_HORIZONTAL_MULT.get()
                            : HighSpeedServerConfig.SLIDE_JUMP_HORIZONTAL_MULT.get();
                    player.setDeltaMovement(motion.x * hMult, jumpPower, motion.z * hMult);
                    toggleSlide((ServerPlayer) player, false, 0, 0);
                }
            }
        }
    }
}
