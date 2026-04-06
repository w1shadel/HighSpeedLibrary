package com.maxwell.highspeedlib.common.logic;

import com.maxwell.highspeedlib.HighSpeedLib;
import com.maxwell.highspeedlib.common.network.PacketHandler;
import com.maxwell.highspeedlib.common.network.packets.S2CSyncSlidePacket;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.util.*;

public class SlideManager {
    private static final Set<UUID> slidingPlayers = new HashSet<>();
    private static final Map<UUID, Integer> airTicksMap = new HashMap<>();
    private static final Map<UUID, Vec3> slideDirMap = new HashMap<>();
    private static final double SLIDE_SPEED = 0.75;

    public static void toggleSlide(ServerPlayer player, boolean start, float xInput, float zInput) {
        if (start) {
            if (!slidingPlayers.contains(player.getUUID())) {
                slidingPlayers.add(player.getUUID());
                Vec3 slideDir;
                if (xInput == 0 && zInput == 0) {
                    slideDir = new Vec3(player.getLookAngle().x, 0, player.getLookAngle().z).normalize();
                } else {
                    float yaw = player.getYRot();
                    float f1 = (float) Math.sin(yaw * (Math.PI / 180.0));
                    float f2 = (float) Math.cos(yaw * (Math.PI / 180.0));
                    slideDir = new Vec3(xInput * f2 - zInput * f1, 0, zInput * f2 + xInput * f1).normalize();
                }
                slideDirMap.put(player.getUUID(), slideDir);
                HighSpeedLib.LOGGER.info("Sliding START (Fixed Direction): {}", player.getName().getString());
            }
        } else {
            if (slidingPlayers.contains(player.getUUID())) {
                slidingPlayers.remove(player.getUUID());
                slideDirMap.remove(player.getUUID());
                HighSpeedLib.LOGGER.info("Sliding STOP: {}", player.getName().getString());
            }
        }
        player.refreshDimensions();
        PacketHandler.INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
                new S2CSyncSlidePacket(player.getId(), start));
    }

    public static boolean isSliding(Player player) {
        if (player == null) return false;
        if (player.level().isClientSide) {
            return com.maxwell.highspeedlib.client.ClientSlideHandler.isPlayerSliding(player.getId());
        }
        return slidingPlayers.contains(player.getUUID());
    }

    @Mod.EventBusSubscriber(modid = HighSpeedLib.MODID)
    public static class Events {
        @SubscribeEvent
        public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;
            Player player = event.player;
            if (player.level().isClientSide) return;
            if (isSliding(player)) {
                if (!player.onGround()) {
                    int airTicks = airTicksMap.getOrDefault(player.getUUID(), 0) + 1;
                    airTicksMap.put(player.getUUID(), airTicks);
                    if (airTicks > 20) {
                        toggleSlide((ServerPlayer) player, false, 0, 0);
                        airTicksMap.remove(player.getUUID());
                        return;
                    }
                } else {
                    airTicksMap.put(player.getUUID(), 0);
                    if (player.tickCount % 2 == 0) {
                        ((ServerLevel) player.level()).sendParticles(
                                ParticleTypes.CRIT,
                                player.getX(), player.getY(), player.getZ(),
                                1, 0, 0, 0, 0.05
                        );
                    }
                    Vec3 slideDir = slideDirMap.getOrDefault(player.getUUID(), new Vec3(player.getLookAngle().x, 0, player.getLookAngle().z).normalize());
                    player.setDeltaMovement(slideDir.x * SLIDE_SPEED, player.getDeltaMovement().y, slideDir.z * SLIDE_SPEED);
                    player.hurtMarked = true;
                }
            }
        }

        @SubscribeEvent
        public static void onLivingJump(LivingEvent.LivingJumpEvent event) {
            if (event.getEntity() instanceof Player player && isSliding(player)) {
                if (!player.level().isClientSide) {
                    Vec3 motion = player.getDeltaMovement();
                    double hSpeed = Math.sqrt(motion.x * motion.x + motion.z * motion.z);
                    double jumpPower = 0.42 + (hSpeed * 0.25);
                    player.setDeltaMovement(motion.x * 1.8, jumpPower, motion.z * 1.8);
                    toggleSlide((ServerPlayer) player, false, 0, 0);
                }
            }
        }
    }
}