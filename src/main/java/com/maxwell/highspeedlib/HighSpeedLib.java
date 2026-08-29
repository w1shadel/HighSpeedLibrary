package com.maxwell.highspeedlib;

import com.maxwell.highspeedlib.api.config.HighSpeedClientConfig;
import com.maxwell.highspeedlib.api.config.HighSpeedServerConfig;
import com.maxwell.highspeedlib.client.renderer.V1WingRenderer;
import com.maxwell.highspeedlib.common.network.PacketHandler;
import com.maxwell.highspeedlib.init.ModAttributes;
import com.maxwell.highspeedlib.init.ModEntities;
import com.maxwell.highspeedlib.init.ModItems;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;

@Mod(HighSpeedLib.MODID)
public class HighSpeedLib {
    public static final String MODID = "highspeedlib";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    public HighSpeedLib(IEventBus modEventBus, ModContainer modContainer) {
        PacketHandler.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModEntities.ENTITIES.register(modEventBus);

        ModAttributes.ATTRIBUTES.register(modEventBus);

        modContainer.registerConfig(ModConfig.Type.CLIENT, HighSpeedClientConfig.SPEC);
        modContainer.registerConfig(ModConfig.Type.SERVER, HighSpeedServerConfig.SPEC);

        modEventBus.addListener(this::clientSetup);
        modEventBus.addListener(this::registerRenderers);
    }

    public static ResourceLocation getResourceLocation(String location) {
        return getResourceLocation(MODID, location);
    }

    public static ResourceLocation getResourceLocation(String nameSpace, String location) {
        return ResourceLocation.fromNamespaceAndPath(nameSpace, location);
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            CuriosRendererRegistry.register(
                    ModItems.V1_WINGS.get(),
                    V1WingRenderer::new
            );
        });
    }

    private void registerRenderers(final EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.TCOIN.get(), ThrownItemRenderer::new);
    }
}