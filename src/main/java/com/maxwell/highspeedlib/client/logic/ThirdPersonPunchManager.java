package com.maxwell.highspeedlib.client.logic;

import com.maxwell.highspeedlib.HighSpeedLib;
import com.maxwell.highspeedlib.client.state.ClientStateManager;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = HighSpeedLib.MODID, value = Dist.CLIENT)
public class ThirdPersonPunchManager {
    public static void startAnimation(int entityId) {
        ClientStateManager.getEntityState(entityId).punchAnimationProgress = 0f;
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
            if (mc.level != null) {
                for (Player player : mc.level.players()) {
                    float current = ClientStateManager.getEntityState(player.getId()).punchAnimationProgress;
                    if (current >= 0 && current < 1.0f) {
                        ClientStateManager.getEntityState(player.getId()).punchAnimationProgress += 0.08f;
                    }
                }
            }
        }
    }

    public static float getProgress(int entityId) {
        float progress = ClientStateManager.getEntityState(entityId).punchAnimationProgress;
        return progress >= 1.0f ? -1f : progress;
    }

    public static float getPunchCurve(float progress) {
        if (progress < 0.1f) return (float) Math.sin((progress / 0.1f) * Math.PI / 2);
        else if (progress < 0.25f) return 1.0f;
        else {
            float backProgress = (progress - 0.25f) / 0.75f;
            return 1.0f - (float) (1.0 - Math.pow(1.0 - backProgress, 3));
        }
    }
}
