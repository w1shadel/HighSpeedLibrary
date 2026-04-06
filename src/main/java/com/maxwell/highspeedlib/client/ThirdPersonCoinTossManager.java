package com.maxwell.highspeedlib.client;

import com.maxwell.highspeedlib.HighSpeedLib;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = HighSpeedLib.MODID, value = Dist.CLIENT)
public class ThirdPersonCoinTossManager {
    private static final Map<Integer, Float> animationMap = new HashMap<>();

    public static void startAnimation(int entityId) {
        animationMap.put(entityId, 0f);
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            animationMap.entrySet().removeIf(entry -> {
                float next = entry.getValue() + 0.08f;
                entry.setValue(next);
                return next >= 1.0f;
            });
        }
    }

    public static float getProgress(int entityId) {
        return animationMap.getOrDefault(entityId, -1f);
    }

    public static float getTossCurve(float progress) {
        if (progress < 0.2f) {
            return (float) Math.sin((progress / 0.2f) * Math.PI / 2);
        } else {
            return 1.0f - (float) Math.pow((progress - 0.2f) / 0.8f, 2);
        }
    }
}
