package com.maxwell.highspeedlib.client.logic;

import com.maxwell.highspeedlib.HighSpeedLib;
import com.maxwell.highspeedlib.client.state.ClientStateManager;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = HighSpeedLib.MODID, value = Dist.CLIENT)
public class ThirdPersonCoinTossManager {

    public static void startAnimation(int entityId) {
        ClientStateManager.getEntityState(entityId).tossAnimationProgress = 0f;
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
            if (mc.level != null) {
                for (Player player : mc.level.players()) {
                    float current = ClientStateManager.getEntityState(player.getId()).tossAnimationProgress;
                    if (current >= 0 && current < 1.0f) {
                        ClientStateManager.getEntityState(player.getId()).tossAnimationProgress += 0.08f;
                    }
                }
            }
        }
    }

    public static float getProgress(int entityId) {
        float progress = ClientStateManager.getEntityState(entityId).tossAnimationProgress;
        return progress >= 1.0f ? -1f : progress;
    }

    public static float getTossCurve(float progress) {
        if (progress < 0.2f) {
            return (float) Math.sin((progress / 0.2f) * Math.PI / 2);
        } else {
            return 1.0f - (float) Math.pow((progress - 0.2f) / 0.8f, 2);
        }
    }
}

