package com.maxwell.highspeedlib.common.logic.movement;

import com.maxwell.highspeedlib.HighSpeedLib;
import com.maxwell.highspeedlib.api.HighSpeedAbilityEvent;
import com.maxwell.highspeedlib.api.config.HighSpeedServerConfig;
import com.maxwell.highspeedlib.common.logic.state.PlayerMovementState;
import com.maxwell.highspeedlib.common.logic.state.PlayerStateManager;
import com.maxwell.highspeedlib.common.network.PacketHandler;
import com.maxwell.highspeedlib.common.network.packets.sync.S2CSyncSlamPacket;
import com.maxwell.highspeedlib.init.ModAttributes;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.phys.AABB;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.UUID;

@EventBusSubscriber(modid = HighSpeedLib.MODID)
public class SlamManager {
    public static void startSlam(ServerPlayer player) {
        if (NeoForge.EVENT_BUS.post(new HighSpeedAbilityEvent.Slam(player)).isCanceled()) {
            return;
        }
        PlayerMovementState state = PlayerStateManager.getState(player).getMovement();
        state.isSlamming = true;
        state.fallImmunity = true;
        state.slamBuffer = 10;
        double downSpeed = HighSpeedServerConfig.SLAM_DOWNWARD_SPEED.get();
        player.setDeltaMovement(0, -downSpeed, 0);
        player.hurtMarked = true;
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, new S2CSyncSlamPacket(player.getId(), true));
    }

    public static void stopSlam(ServerPlayer player) {
        PlayerMovementState state = PlayerStateManager.getState(player).getMovement();
        state.isSlamming = false;
        boolean storageActive = state.slamStorageActive || state.slamStorageTimer > 0;
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, new S2CSyncSlamPacket(player.getId(), false, storageActive));
    }

    public static boolean isSlamming(UUID uuid) {
        return PlayerStateManager.getState(uuid).getMovement().isSlamming;
    }

    @SubscribeEvent
    public static void onLivingTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        PlayerMovementState state = PlayerStateManager.getState(player).getMovement();
        if (state.slamBuffer > 0) {
            state.slamBuffer--;
        }
        if (state.isSlamming) {
            if (player.onGround() || player.verticalCollision) {
                PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, new S2CSyncSlamPacket(player.getId(), player.position()));
                performSlamImpact(player);
                stopSlam(player);
                if (state.slamXInput != 0f || state.slamZInput != 0f) {
                    SlideManager.toggleSlide(player, true, state.slamXInput, state.slamZInput);
                } else {
                    SlideManager.toggleSlide(player, false, 0, 0);
                    player.setDeltaMovement(0, player.getDeltaMovement().y, 0);
                    player.hurtMarked = true;
                }
                PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, new S2CSyncSlamPacket(player.getId(), player.position()));
                SlideManager.toggleSlide(player, true, 0, 1.0f);
                state.slamImpactTimer = 10;
            } else {
                player.setDeltaMovement(0, -HighSpeedServerConfig.SLAM_DOWNWARD_SPEED.get(), 0);
                player.hurtMarked = true;
            }
        }
        if (player.onGround() && !state.isSlamming && state.slamBuffer <= 0) {
            state.fallImmunity = false;
        }
    }
    private static void performSlamImpact(ServerPlayer player) {
        ServerLevel level = (ServerLevel) player.level();
        double slamBase = player.getAttributeValue(ModAttributes.SLAM_DAMAGE);
        double playerAttack = player.getAttributeValue(Attributes.ATTACK_DAMAGE);
        double scalingDamage = slamBase + (playerAttack * HighSpeedServerConfig.SLAM_DAMAGE_ATTACK_FACTOR.get());

        int featherFallingLevel = level.registryAccess()
                .lookupOrThrow(Registries.ENCHANTMENT)
                .get(Enchantments.FEATHER_FALLING)
                .map(holder -> EnchantmentHelper.getEnchantmentLevel(holder, player))
                .orElse(0);

        float enchantMultiplier = 1.0f + (featherFallingLevel * HighSpeedServerConfig.SLAM_ENCHANT_FACTOR.get().floatValue());
        float finalDamage = (float) (scalingDamage * enchantMultiplier);

        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.0f, 0.8f);

        double radius = HighSpeedServerConfig.SLAM_RADIUS.get();
        double knockup = HighSpeedServerConfig.SLAM_KNOCKUP_POWER.get();
        AABB area = player.getBoundingBox().inflate(radius, radius * 0.5, radius);
        player.level().getEntitiesOfClass(LivingEntity.class, area, e -> e != player).forEach(target -> {
            target.hurt(player.damageSources().fall(), finalDamage);
            target.setDeltaMovement(0, knockup, 0);
            target.hurtMarked = true;
        });
    }

    @SubscribeEvent
    public static void onFall(LivingFallEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PlayerMovementState state = PlayerStateManager.getState(player).getMovement();
            if (state.fallImmunity) {
                event.setCanceled(true);
                state.fallImmunity = false;
            }
        }
    }
}
