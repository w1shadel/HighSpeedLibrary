package com.maxwell.highspeedlib.client.logic;

import com.maxwell.highspeedlib.client.state.ClientPlayerState;
import com.maxwell.highspeedlib.client.state.ClientStateManager;
import com.maxwell.highspeedlib.common.logic.combat.ServerWhiplashManager;
import com.maxwell.highspeedlib.common.network.packets.sync.S2CSyncWhiplashPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.UUID;

@SuppressWarnings("removal")
@EventBusSubscriber(value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public class ClientWhiplashManager {
    public static ServerWhiplashManager.HookData getHookData(UUID uuid) {
        return ClientStateManager.getPlayerState(uuid).whiplashHookData;
    }

    public static int getRenderTicks(UUID uuid) {
        return ClientStateManager.getPlayerState(uuid).whiplashRenderTicks;
    }

    public static void handleSync(S2CSyncWhiplashPacket msg) {
        ServerWhiplashManager.HookData data = getHookData(msg.playerUuid);
        data.state = msg.state;
        data.distance = msg.distance;
        data.targetId = msg.targetId;
        data.hitPos = msg.hitPos;
        data.shootDir = msg.shootDir;
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        for (Player player : mc.level.players()) {
            UUID uuid = player.getUUID();
            ClientPlayerState state = ClientStateManager.getPlayerState(uuid);
            ServerWhiplashManager.HookData data = state.whiplashHookData;
            if (data == null || data.state == ServerWhiplashManager.NONE) {
                if (state.whiplashRenderTicks > 0) {
                    state.whiplashRenderTicks--;
                }
                continue;
            }
            if (data.state == ServerWhiplashManager.FLYING) {
                data.distance += com.maxwell.highspeedlib.api.config.HighSpeedServerConfig.WHIPLASH_FLY_SPEED.get();
            } else if (data.state == ServerWhiplashManager.RETRACTING) {
                data.distance -= com.maxwell.highspeedlib.api.config.HighSpeedServerConfig.WHIPLASH_PULL_SPEED.get();
                if (data.distance <= 0) {
                    data.distance = 0;
                    data.state = ServerWhiplashManager.NONE;
                }
            } else if (data.state == ServerWhiplashManager.HOOKED) {
                Entity target = mc.level.getEntity(data.targetId);
                if (target != null) {
                    data.distance = player.getEyePosition().distanceTo(target.position().add(0, target.getBbHeight() * 0.5, 0));
                }
            }
            if (state.whiplashRenderTicks < 10) {
                state.whiplashRenderTicks++;
            }
        }
    }
}
