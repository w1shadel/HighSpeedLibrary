package com.maxwell.highspeedlib.client;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Math;

import java.util.Objects;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class ClientEffectManager {
    private static float parryAlpha = 0f;
    private static boolean isSpeeding = false;
    private static float fovModifier = 0f;
    private static float shakeIntensity = 0f;
    private static int shakeTicks = 0;

    public static void triggerParryFlash() {
        parryAlpha = 0.5f;
    }

    public static void setSpeeding(boolean speeding) {
        isSpeeding = speeding;
    }

    @SubscribeEvent
    public static void onRenderTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            if (parryAlpha > 0) parryAlpha -= 0.05f;
            float targetFov = isSpeeding ? 1.2f : 1.0f;
            fovModifier = Math.lerp(fovModifier, targetFov, 0.1f);
        }
    }

    @SubscribeEvent
    public static void onComputeFov(ViewportEvent.ComputeFov event) {
        event.setFOV(event.getFOV() * fovModifier);
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiOverlayEvent.Post event) {
        if (parryAlpha > 0) {
        }
    }

    public static void startShake(float intensity, int duration) {
        shakeIntensity = intensity;
        shakeTicks = duration;
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END && shakeTicks > 0) {
            shakeTicks--;
            if (shakeTicks <= 0) shakeIntensity = 0;
        }
    }

    @SubscribeEvent
    public static void onCameraSetup(ViewportEvent.ComputeCameraAngles event) {
        if (shakeTicks > 0) {
            float f = (Objects.requireNonNull(Minecraft.getInstance().level).random.nextFloat() - 0.5f) * shakeIntensity;
            float g = (Minecraft.getInstance().level.random.nextFloat() - 0.5f) * shakeIntensity;
            event.setPitch(event.getPitch() + f);
            event.setYaw(event.getYaw() + g);
            event.setRoll(event.getRoll() + f * 0.5f);
        }

    }
}