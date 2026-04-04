package com.maxwell.highspeedlib.client;

import com.maxwell.highspeedlib.HighSpeedLib;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = HighSpeedLib.MODID,value = Dist.CLIENT)
public class ClientSlideHandler {
    private static boolean clientIsSliding = false;

    public static void setSliding(boolean sliding) { clientIsSliding = sliding; }

    @SubscribeEvent
    public static void onComputeFov(ViewportEvent.ComputeFov event) {
        if (clientIsSliding) {

            float newFov = (float) (event.getFOV() * 1.2);
            if (newFov > 130.0f) newFov = 130.0f;
            event.setFOV(newFov);
        }
    }

    @SubscribeEvent
    public static void onCameraSetup(ViewportEvent.ComputeCameraAngles event) {
        if (clientIsSliding) {

            float shake = (Minecraft.getInstance().level.random.nextFloat() - 0.5f) * 0.1f;
            event.setPitch(event.getPitch() + shake);
        }
    }
}