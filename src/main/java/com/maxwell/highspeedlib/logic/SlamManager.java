package com.maxwell.highspeedlib.logic;

import com.maxwell.highspeedlib.HighSpeedLib;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;

@Mod.EventBusSubscriber(modid = HighSpeedLib.MODID)
public class SlamManager {
    private static final Set<UUID> slammingPlayers = new HashSet<>();
    private static final Map<UUID, Integer> slamBuffer = new HashMap<>();

    private static final Set<UUID> fallImmunityPlayers = new HashSet<>();

    public static void startSlam(ServerPlayer player) {
        slammingPlayers.add(player.getUUID());
        fallImmunityPlayers.add(player.getUUID()); 
        slamBuffer.put(player.getUUID(), 10);
        player.setDeltaMovement(0, -3.0, 0);
        player.hurtMarked = true;
    }

    public static void stopSlam(ServerPlayer player) {
        slammingPlayers.remove(player.getUUID());
    }

    public static boolean isSlamming(UUID uuid) {
        return slammingPlayers.contains(uuid);
    }
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        slamBuffer.entrySet().removeIf(entry -> {
            entry.setValue(entry.getValue() - 1);
            return entry.getValue() <= 0;
        });
    }
    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        if (isSlamming(player.getUUID())) {

            if (player.onGround()) {
                performSlamImpact(player);
                stopSlam(player);

                SlideManager.toggleSlide(player, true);
            } else {

                ((ServerLevel)player.level()).sendParticles(ParticleTypes.FLASH,
                        player.getX(), player.getY(), player.getZ(), 1, 0, 0, 0, 0);

                player.setDeltaMovement(0, -3.0, 0);
                player.hurtMarked = true;
            }
        }

        if (player.onGround() && !isSlamming(player.getUUID()) && !slamBuffer.containsKey(player.getUUID())) {
            fallImmunityPlayers.remove(player.getUUID());
        }
    }

    private static void performSlamImpact(ServerPlayer player) {
        ServerLevel level = (ServerLevel) player.level();

        level.sendParticles(ParticleTypes.EXPLOSION, player.getX(), player.getY(), player.getZ(), 5, 1.0, 0.1, 1.0, 0.1);
        level.playSound(null, player.blockPosition(), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.0f, 1.2f);

        AABB area = player.getBoundingBox().inflate(4.0, 2.0, 4.0);
        player.level().getEntitiesOfClass(LivingEntity.class, area, e -> e != player).forEach(target -> {
            target.hurt(player.damageSources().fall(), 8.0f);
            target.setDeltaMovement(0, 0.8, 0); 
            target.hurtMarked = true;
        });
    }

    @SubscribeEvent
    public static void onJump(LivingEvent.LivingJumpEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (slamBuffer.containsKey(player.getUUID())) {

                float pitch = player.getXRot(); 

                double verticalMultiplier;
                if (pitch < 45.0f) {



                    verticalMultiplier = 1.0 + (Math.max(0, 45.0 - pitch) / 135.0) * 4.5;
                } else {
                    verticalMultiplier = 1.0;
                }

                Vec3 motion = player.getDeltaMovement();

                player.setDeltaMovement(motion.x, 0.5 * verticalMultiplier * 2.5, motion.z);
                player.hurtMarked = true;

                fallImmunityPlayers.add(player.getUUID());

                slamBuffer.remove(player.getUUID());

                player.level().playSound(null, player.blockPosition(), SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.PLAYERS, 1.0f, 0.8f);
            }
        }
    }

    @SubscribeEvent
    public static void onFall(LivingFallEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (fallImmunityPlayers.contains(player.getUUID())) {
                event.setCanceled(true);
                fallImmunityPlayers.remove(player.getUUID());
            }
        }
    }
}
