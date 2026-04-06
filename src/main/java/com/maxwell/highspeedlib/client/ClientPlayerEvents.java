package com.maxwell.highspeedlib.client;

import com.maxwell.highspeedlib.common.entity.WhiplashHookEntity;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.client.Minecraft;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class ClientPlayerEvents {
    private static final Map<UUID, Integer> whiplashPoseTicks = new HashMap<>();

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        whiplashPoseTicks.entrySet().removeIf(entry -> {
            int val = entry.getValue();
            if (val <= 0) return true;
            entry.setValue(val - 1);
            return false;
        });
        if (Minecraft.getInstance().level != null) {
            for (Player player : Minecraft.getInstance().level.players()) {
                boolean hasHook = Minecraft.getInstance().level.getEntitiesOfClass(WhiplashHookEntity.class,
                        player.getBoundingBox().inflate(64), hook -> hook.getOwner() == player).size() > 0;
                if (hasHook) whiplashPoseTicks.put(player.getUUID(), 10);
            }
        }
    }

    public static int getWhiplashTicks(UUID uuid) {
        return whiplashPoseTicks.getOrDefault(uuid, 0);
    }
}
