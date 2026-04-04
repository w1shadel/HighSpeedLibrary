package com.maxwell.highspeedlib.client;

import com.maxwell.highspeedlib.HighSpeedLib;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = HighSpeedLib.MODID ,value = Dist.CLIENT)
public class ThirdPersonPunchManager {

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
    public static float getPunchCurve(float progress) {
        if (progress < 0.1f) return (float) Math.sin((progress / 0.1f) * Math.PI / 2);
        else if (progress < 0.25f) return 1.0f;
        else {
            float backProgress = (progress - 0.25f) / 0.75f;
            return 1.0f - (float) (1.0 - Math.pow(1.0 - backProgress, 3));
        }
    }
}