package com.maxwell.highspeedlib.common;

import com.maxwell.highspeedlib.HighSpeedLib;
import com.maxwell.highspeedlib.api.HighSpeedAbilityEvent;
import com.maxwell.highspeedlib.common.logic.AbilityAuthority;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = HighSpeedLib.MODID)
public class AbilityLockHandler {
    @SubscribeEvent
    public static void onPunch(HighSpeedAbilityEvent.Punch event) {
        if (!AbilityAuthority.canPunch(event.getPlayer().getUUID())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onDash(HighSpeedAbilityEvent.Dash event) {
        if (!AbilityAuthority.canDash(event.getPlayer().getUUID())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            AbilityAuthority.sync(player);
            CompoundTag nbt = player.getPersistentData().getCompound("HighSpeedLibData");
            AbilityAuthority.get(player.getUUID()).load(nbt);
            AbilityAuthority.sync(player);
        }
    }

    @SubscribeEvent
    public static void onWhiplash(HighSpeedAbilityEvent.Whiplash event) {
        if (!AbilityAuthority.canWhiplash(event.getPlayer().getUUID())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onCoinToss(HighSpeedAbilityEvent.CoinToss event) {
        if (!AbilityAuthority.get(event.getPlayer().getUUID()).punch) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onSliding(HighSpeedAbilityEvent.Sliding event) {
        if (!AbilityAuthority.canSlide(event.getPlayer().getUUID())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onSlam(HighSpeedAbilityEvent.Slam event) {
        if (!AbilityAuthority.canSlam(event.getPlayer().getUUID())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onWalljump(HighSpeedAbilityEvent.Walljump event) {
        if (!AbilityAuthority.canWallJump(event.getPlayer().getUUID())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onPlayerSave(PlayerEvent.SaveToFile event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            CompoundTag nbt = AbilityAuthority.get(player.getUUID()).save();
            player.getPersistentData().put("HighSpeedLibData", nbt);
        }
    }
}