package com.maxwell.highspeedlib.common.logic;

import com.maxwell.highspeedlib.HighSpeedLib;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DashManager {
    private static final Map<UUID, Integer> dashInvulTicksMap = new HashMap<>();

    public static void startDashInvulnerability(ServerPlayer player, int ticks) {
        dashInvulTicksMap.put(player.getUUID(), ticks);
    }

    public static boolean hasDashInvulnerability(Player player) {
        return dashInvulTicksMap.getOrDefault(player.getUUID(), 0) > 0;
    }

    @Mod.EventBusSubscriber(modid = HighSpeedLib.MODID)
    public static class Events {
        @SubscribeEvent
        public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;
            Player player = event.player;
            if (player.level().isClientSide) return;
            UUID uuid = player.getUUID();
            if (dashInvulTicksMap.containsKey(uuid)) {
                int ticks = dashInvulTicksMap.get(uuid);
                if (ticks > 0) {
                    dashInvulTicksMap.put(uuid, ticks - 1);
                } else {
                    dashInvulTicksMap.remove(uuid);
                }
            }
        }

        @SubscribeEvent
        public static void onLivingHurt(LivingHurtEvent event) {
            if (event.getEntity() instanceof Player player) {
                if (hasDashInvulnerability(player)) {
                    event.setCanceled(true);
                }
            }
        }
    }
}
