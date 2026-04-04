package com.maxwell.highspeedlib.client;

import com.maxwell.highspeedlib.HighSpeedLib;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderBlockScreenEffectEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashSet;
import java.util.Set;

@Mod.EventBusSubscriber(modid = HighSpeedLib.MODID, value = Dist.CLIENT)
public class ClientSlideHandler {
    private static final Set<Integer> otherSlidingPlayers = new HashSet<>();
    private static final float SLIDE_FOV_TARGET = 1.2f;
    private static final float NORMAL_FOV_TARGET = 1.0f;
    private static final float FOV_LERP_SPEED = 0.1f;
    private static boolean clientIsSliding = false;
    private static float currentFovModifier = 1.0f;

    public static void setSliding(boolean sliding) {
        clientIsSliding = sliding;
    }

    public static void updateOtherPlayerSliding(int entityId, boolean sliding) {
        if (Minecraft.getInstance().player != null && Minecraft.getInstance().player.getId() == entityId) {
            setSliding(sliding);
        } else {
            if (sliding) otherSlidingPlayers.add(entityId);
            else otherSlidingPlayers.remove(entityId);
        }
    }

    public static boolean isPlayerSliding(int entityId) {
        if (Minecraft.getInstance().player != null && Minecraft.getInstance().player.getId() == entityId) {
            return clientIsSliding;
        }
        return otherSlidingPlayers.contains(entityId);
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            float target = clientIsSliding ? SLIDE_FOV_TARGET : NORMAL_FOV_TARGET;
            currentFovModifier = Mth.lerp(FOV_LERP_SPEED, currentFovModifier, target);
        }
    }

    @SubscribeEvent
    public static void onComputeFov(ViewportEvent.ComputeFov event) {
        float newFov = (float) (event.getFOV() * currentFovModifier);
        if (newFov > 130.0f) newFov = 130.0f;
        event.setFOV(newFov);
    }
    @SubscribeEvent
    public static void onCameraSetup(ViewportEvent.ComputeCameraAngles event) {

        if (clientIsSliding) {
            float shake = (Minecraft.getInstance().level.random.nextFloat() - 0.5f) * 0.1f;
            event.setPitch(event.getPitch() + shake);
        }
    }
    public static float getSlideProgress() {
        return Mth.clamp((currentFovModifier - 1.0f) / 0.2f, 0.0f, 1.0f);
    }
    @SubscribeEvent
    public static void onRenderBlockOverlay(RenderBlockScreenEffectEvent event) {
        if (clientIsSliding) {
            event.setCanceled(true);
        }
    }
}