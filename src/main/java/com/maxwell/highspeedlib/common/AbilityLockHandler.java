package com.maxwell.highspeedlib.common;

import com.maxwell.highspeedlib.HighSpeedLib;
import com.maxwell.highspeedlib.api.HighSpeedAbilityEvent;
import com.maxwell.highspeedlib.common.logic.ability.AbilityManager;
import com.maxwell.highspeedlib.common.logic.state.PlayerStateManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = HighSpeedLib.MODID)
public class AbilityLockHandler {
    @SubscribeEvent
    public static void onPunch(HighSpeedAbilityEvent.Punch event) {
        if (!AbilityManager.canPunch(event.getPlayer().getUUID())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onDash(HighSpeedAbilityEvent.Dash event) {
        if (!AbilityManager.canDash(event.getPlayer().getUUID())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            CompoundTag nbt = player.getPersistentData().getCompound("HighSpeedLibData");
            PlayerStateManager.getState(player).load(nbt);
            AbilityManager.sync(player);
        }
    }

    @SubscribeEvent
    public static void onWhiplash(HighSpeedAbilityEvent.Whiplash event) {
        if (!AbilityManager.canWhiplash(event.getPlayer().getUUID())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onCoinToss(HighSpeedAbilityEvent.CoinToss event) {
        if (!PlayerStateManager.getState(event.getPlayer()).getAbility().punch) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onSliding(HighSpeedAbilityEvent.Sliding event) {
        if (!AbilityManager.canSlide(event.getPlayer().getUUID())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onSlam(HighSpeedAbilityEvent.Slam event) {
        if (!AbilityManager.canSlam(event.getPlayer().getUUID())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onWalljump(HighSpeedAbilityEvent.Walljump event) {
        if (!AbilityManager.canWallJump(event.getPlayer().getUUID())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onPlayerSave(PlayerEvent.SaveToFile event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            CompoundTag nbt = PlayerStateManager.getState(player).save();
            player.getPersistentData().put("HighSpeedLibData", nbt);
        }
    }
}