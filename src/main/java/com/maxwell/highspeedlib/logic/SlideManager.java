package com.maxwell.highspeedlib.logic;

import com.maxwell.highspeedlib.HighSpeedLib;
import com.maxwell.highspeedlib.network.PacketHandler;
import com.maxwell.highspeedlib.network.packets.S2CSyncSlidePacket;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class SlideManager {
    private static final Set<UUID> slidingPlayers = new HashSet<>();

    public static void toggleSlide(ServerPlayer player, boolean start) {
        if (start) {
            slidingPlayers.add(player.getUUID());
            player.setPose(Pose.SWIMMING);
        } else {
            slidingPlayers.remove(player.getUUID());
            player.setPose(Pose.STANDING);
        }

        PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new S2CSyncSlidePacket(start));
    }

    public static boolean isSliding(Player player) {
        return slidingPlayers.contains(player.getUUID());
    }

    @Mod.EventBusSubscriber(modid = HighSpeedLib.MODID)
    public static class Events {
        @SubscribeEvent
        public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
            Player player = event.player;
            if (player.level().isClientSide) return;

            if (isSliding(player)) {
                if (!player.onGround()) {
                    toggleSlide((ServerPlayer) player, false);
                    return;
                }
                if (player.tickCount % 2 == 0) {
                    ((ServerLevel)player.level()).sendParticles(
                            ParticleTypes.CRIT,
                            player.getX(), player.getY(), player.getZ(),
                            1, 0, 0, 0, 0.05
                    );
                }
                if (player.tickCount % 10 == 0) {
                    player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.ARMOR_EQUIP_NETHERITE, SoundSource.PLAYERS, 0.5f, 1.5f);
                }
                Vec3 look = player.getLookAngle();
                Vec3 slideVec = new Vec3(look.x, 0, look.z).normalize().scale(0.6);
                player.setDeltaMovement(slideVec.x, player.getDeltaMovement().y, slideVec.z);
                player.hurtMarked = true;
            }
        }

        @SubscribeEvent
        public static void onLivingJump(LivingEvent.LivingJumpEvent event) {
            if (event.getEntity() instanceof Player player && isSliding(player)) {
                if (!player.level().isClientSide) {
                    Vec3 motion = player.getDeltaMovement();
                    player.setDeltaMovement(motion.x * 1.8, 0.6, motion.z * 1.8);
                    toggleSlide((ServerPlayer) player, false);
                }
            }
        }
    }
}