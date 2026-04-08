package com.maxwell.highspeedlib.common.logic;

import com.maxwell.highspeedlib.HighSpeedLib;
import com.maxwell.highspeedlib.api.HighSpeedAbilityEvent;
import com.maxwell.highspeedlib.init.ModAttributes;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.MinecraftForge;
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
        if (MinecraftForge.EVENT_BUS.post(new HighSpeedAbilityEvent.Slam(player))) {
            return;
        }
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
                SlideManager.toggleSlide(player, true, 0, 1.0f);
            } else {
                ((ServerLevel) player.level()).sendParticles(ParticleTypes.FLASH,
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
        double slamBase = player.getAttributeValue(ModAttributes.SLAM_DAMAGE.get());
        double playerAttack = player.getAttributeValue(Attributes.ATTACK_DAMAGE);
        double scalingDamage = slamBase + (playerAttack * 0.5);
        int featherFallingLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.FALL_PROTECTION, player);
        float enchantMultiplier = 1.0f + (featherFallingLevel * 0.1f);
        float finalDamage = (float) (scalingDamage * enchantMultiplier);
        level.sendParticles(ParticleTypes.EXPLOSION, player.getX(), player.getY() - 0.2, player.getZ(), 5, 1.0, 0.1, 1.0, 0.1);
        level.playSound(null, player.blockPosition(), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.0f, 0.8f);
        AABB area = player.getBoundingBox().inflate(4.0, 2.0, 4.0);
        player.level().getEntitiesOfClass(LivingEntity.class, area, e -> e != player).forEach(target -> {
            target.hurt(player.damageSources().fall(), finalDamage);
            target.setDeltaMovement(0, 0.8, 0);
            target.hurtMarked = true;
        });
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
