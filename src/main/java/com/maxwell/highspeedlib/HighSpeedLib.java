package com.maxwell.highspeedlib;

import com.maxwell.highspeedlib.network.PacketHandler;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(HighSpeedLib.MODID)
public class HighSpeedLib {
    public static final String MODID = "highspeedlib";

    public HighSpeedLib(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();
        PacketHandler.register();
        ModEntities.ENTITIES.register(modEventBus);
        modEventBus.addListener(this::clientSetup);
    }
    private void clientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            EntityRenderers.register(ModEntities.TCOIN.get(), ThrownItemRenderer::new);
        });
    }
}
