package com.maxwell.highspeedlib.client;

import com.maxwell.highspeedlib.HighSpeedLib;
import com.maxwell.highspeedlib.client.renderer.ShockwaveRenderer;
import com.maxwell.highspeedlib.init.ModEntities;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
@SuppressWarnings("removal")
@EventBusSubscriber(modid = HighSpeedLib.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class ClientModEvents {
    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.SHOCKWAVE.get(), ShockwaveRenderer::new);
    }
}
