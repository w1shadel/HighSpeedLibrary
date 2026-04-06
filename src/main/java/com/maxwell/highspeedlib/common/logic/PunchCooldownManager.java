package com.maxwell.highspeedlib.common.logic;

import com.maxwell.highspeedlib.HighSpeedLib;
import com.maxwell.highspeedlib.common.network.PacketHandler;
import com.maxwell.highspeedlib.common.network.packets.S2CSyncPunchEnergyPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = HighSpeedLib.MODID)
public class PunchCooldownManager {
    private static final Map<UUID, Double> punchEnergy = new HashMap<>();
    private static double clientEnergy = 2.0;

    public static double getEnergy(Player player) {
        if (player.level().isClientSide) return clientEnergy;
        return punchEnergy.getOrDefault(player.getUUID(), 2.0);
    }

    public static void setClientEnergy(double energy) {
        clientEnergy = energy;
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) return;
        Player player = event.player;
        UUID uuid = player.getUUID();
        double current = punchEnergy.getOrDefault(uuid, 2.0);
        if (current < 2.0) {
            double next = Math.min(2.0, current + (1.0 / 20.0));
            punchEnergy.put(uuid, next);
            if (next >= 2.0 || (int) (current * 5) != (int) (next * 5)) {
                syncToClient(player, next);
            }
        }
    }

    public static boolean tryConsume(Player player, boolean isRed) {
        double current = getEnergy(player);
        double cost = isRed ? 2.0 : 1.0;
        if (current >= cost) {
            if (!player.level().isClientSide) {
                double next = current - cost;
                punchEnergy.put(player.getUUID(), next);
                syncToClient(player, next);
            }
            return true;
        }
        return false;
    }

    private static void syncToClient(Player player, double energy) {
        if (player instanceof ServerPlayer serverPlayer) {
            PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> serverPlayer), new S2CSyncPunchEnergyPacket(energy));
        }
    }
}