package com.maxwell.highspeedlib.client;

import com.maxwell.highspeedlib.common.logic.ServerWhiplashManager;
import com.maxwell.highspeedlib.common.network.packets.S2CSyncWhiplashPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientWhiplashManager {
    public static final java.util.Map<java.util.UUID, ServerWhiplashManager.HookData> clientHooks = new java.util.HashMap<>();
    public static final java.util.Map<java.util.UUID, Integer> renderTicksMap = new java.util.HashMap<>();

    public static ServerWhiplashManager.HookData getHookData(java.util.UUID uuid) {
        return clientHooks.computeIfAbsent(uuid, k -> new ServerWhiplashManager.HookData());
    }

    public static int getRenderTicks(java.util.UUID uuid) {
        return renderTicksMap.getOrDefault(uuid, 0);
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
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        for (Player player : mc.level.players()) {
            ServerWhiplashManager.HookData data = clientHooks.get(player.getUUID());
            int currentTicks = getRenderTicks(player.getUUID());
            if (data == null || data.state == ServerWhiplashManager.NONE) {
                if (currentTicks > 0) {
                    renderTicksMap.put(player.getUUID(), currentTicks - 1);
                }
                continue;
            }
            if (data.state == ServerWhiplashManager.FLYING) {
                data.distance += ServerWhiplashManager.FLY_SPEED;
            } else if (data.state == ServerWhiplashManager.RETRACTING) {
                data.distance -= ServerWhiplashManager.PULL_SPEED;
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
            if (currentTicks < 10) {
                renderTicksMap.put(player.getUUID(), currentTicks + 1);
            }
        }
    }
}
