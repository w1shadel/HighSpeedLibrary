package com.maxwell.highspeedlib.common.logic.combat;

import com.maxwell.highspeedlib.HighSpeedLib;
import com.maxwell.highspeedlib.api.config.HighSpeedServerConfig;
import com.maxwell.highspeedlib.common.logic.state.PlayerCombatState;
import com.maxwell.highspeedlib.common.logic.state.PlayerStateManager;
import com.maxwell.highspeedlib.common.network.PacketHandler;
import com.maxwell.highspeedlib.common.network.packets.sync.S2CSyncPunchEnergyPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = HighSpeedLib.MODID)
public class PunchCooldownManager {
    private static double clientEnergy = 2.0;

    public static double getEnergy(Player player) {
        if (player.level().isClientSide) return clientEnergy;
        return PlayerStateManager.getState(player).getCombat().punchEnergy;
    }

    public static void setClientEnergy(double energy) {
        clientEnergy = energy;
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity().level().isClientSide) return;
        Player player = event.getEntity();
        PlayerCombatState state = PlayerStateManager.getState(player).getCombat();
        double current = state.punchEnergy;
        if (current < 2.0) {
            double regen = HighSpeedServerConfig.PUNCH_ENERGY_REGEN_PER_TICK.get();
            double next = Math.min(2.0, current + regen);
            state.punchEnergy = next;
            if (next >= 2.0 || (int) (current * 5) != (int) (next * 5)) {
                syncToClient(player, next);
            }
        }
    }

    public static boolean tryConsume(Player player, boolean isRed) {
        PlayerCombatState state = PlayerStateManager.getState(player).getCombat();
        double current = state.punchEnergy;
        double cost = isRed ? 2.0 : 1.0;
        if (current >= cost) {
            if (!player.level().isClientSide) {
                double next = current - cost;
                state.punchEnergy = next;
                syncToClient(player, next);
            }
            return true;
        }
        return false;
    }

    private static void syncToClient(Player player, double energy) {
        if (player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, new S2CSyncPunchEnergyPacket(energy));
        }
    }
}
