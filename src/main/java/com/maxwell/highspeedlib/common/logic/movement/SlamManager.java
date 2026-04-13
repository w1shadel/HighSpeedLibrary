package com.maxwell.highspeedlib.common.logic.movement;

import com.maxwell.highspeedlib.HighSpeedLib;
import com.maxwell.highspeedlib.api.HighSpeedAbilityEvent;
import com.maxwell.highspeedlib.api.config.HighSpeedServerConfig;
import com.maxwell.highspeedlib.common.logic.state.PlayerMovementState;
import com.maxwell.highspeedlib.common.logic.state.PlayerStateManager;
import com.maxwell.highspeedlib.common.network.PacketHandler;
import com.maxwell.highspeedlib.common.network.packets.sync.S2CSyncSlamPacket;
import com.maxwell.highspeedlib.init.ModAttributes;
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
import net.minecraftforge.network.PacketDistributor;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = HighSpeedLib.MODID)
public class SlamManager {

    public static void startSlam(ServerPlayer player) {
        if (MinecraftForge.EVENT_BUS.post(new HighSpeedAbilityEvent.Slam(player))) {
            return;
        }
        PlayerMovementState state = PlayerStateManager.getState(player).getMovement();
        state.isSlamming = true;
        state.fallImmunity = true;
        state.slamBuffer = 10;
        
        double downSpeed = HighSpeedServerConfig.SLAM_DOWNWARD_SPEED.get();
        player.setDeltaMovement(0, -downSpeed, 0);
        player.hurtMarked = true;
        PacketHandler.INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
                new S2CSyncSlamPacket(player.getId(), true));
    }

    public static void stopSlam(ServerPlayer player) {
        PlayerStateManager.getState(player).getMovement().isSlamming = false;
        PacketHandler.INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
                new S2CSyncSlamPacket(player.getId(), false));
    }

    public static boolean isSlamming(UUID uuid) {
        return PlayerStateManager.getState(uuid).getMovement().isSlamming;
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        PlayerMovementState state = PlayerStateManager.getState(player).getMovement();
        
        if (state.slamBuffer > 0) {
            state.slamBuffer--;
        }

        if (state.isSlamming) {
            if (player.onGround() || player.verticalCollision) {
                performSlamImpact(player);
                stopSlam(player);
                PacketHandler.INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
                        new S2CSyncSlamPacket(player.getId(), player.position()));
                SlideManager.toggleSlide(player, true, 0, 1.0f);
            } else {
                player.setDeltaMovement(0, -3.0, 0);
                player.hurtMarked = true;
            }
        }
        if (player.onGround() && !state.isSlamming && state.slamBuffer <= 0) {
            state.fallImmunity = false;
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
        level.playSound(null, player.blockPosition(), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.0f, 0.8f);
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
