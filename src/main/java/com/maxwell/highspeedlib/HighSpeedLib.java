package com.maxwell.highspeedlib;

import com.maxwell.highspeedlib.common.network.PacketHandler;
import com.maxwell.highspeedlib.init.ModEnchantments;
import com.maxwell.highspeedlib.init.ModEntities;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(HighSpeedLib.MODID)
public class HighSpeedLib {
    public static final String MODID = "highspeedlib";
    public static final org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(MODID);

    public HighSpeedLib(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();
        PacketHandler.register();
        ModEntities.ENTITIES.register(modEventBus);
        ModEnchantments.ENCHANTMENTS.register(modEventBus);
        com.maxwell.highspeedlib.init.ModAttributes.ATTRIBUTES.register(modEventBus);
        modEventBus.addListener(this::clientSetup);
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            EntityRenderers.register(ModEntities.TCOIN.get(), ThrownItemRenderer::new);
            EntityRenderers.register(ModEntities.WHIPLASH_HOOK.get(), com.maxwell.highspeedlib.client.renderer.WhiplashHookRenderer::new);
        });
    }
}
