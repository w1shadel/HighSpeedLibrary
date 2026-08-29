package com.maxwell.highspeedlib.common.logic.movement;

import com.maxwell.highspeedlib.HighSpeedLib;
import com.maxwell.highspeedlib.common.logic.state.PlayerStateManager;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

@EventBusSubscriber(modid = HighSpeedLib.MODID)
public class MovementStateHandler {
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!event.getEntity().level().isClientSide) {
            if (event.getEntity().onGround()) {
                PlayerStateManager.getState(event.getEntity()).getMovement().wallJumpCount = 0;
            }
        }
    }
    @SubscribeEvent
    public static void onEntitySize(EntityEvent.Size event) {
        if (event.getEntity() instanceof Player player) {
            if (SlideManager.isSliding(player)) {

                event.setNewSize(EntityDimensions.scalable(0.6F, 0.6F).withEyeHeight(0.6F));
            }
        }
    }
    @SubscribeEvent
    public static void onLivingJump(LivingEvent.LivingJumpEvent event) {
        if (event.getEntity() instanceof Player player && !player.level().isClientSide) {
            PlayerStateManager.getState(player).getMovement().wallJumpCount = 0;
        }
    }

    @SubscribeEvent
    public static void onLivingFall(LivingFallEvent event) {
        if (event.getEntity() instanceof Player player && !player.level().isClientSide) {
            PlayerStateManager.getState(player).getMovement().wallJumpCount = 0;
        }
    }
}
