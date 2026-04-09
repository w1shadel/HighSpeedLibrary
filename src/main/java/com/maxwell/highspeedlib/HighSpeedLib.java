package com.maxwell.highspeedlib;

import com.maxwell.highspeedlib.api.config.HighSpeedClientConfig;
import com.maxwell.highspeedlib.client.renderer.V1WingRenderer;
import com.maxwell.highspeedlib.common.network.PacketHandler;
import com.maxwell.highspeedlib.init.ModAttributes;
import com.maxwell.highspeedlib.init.ModEnchantments;
import com.maxwell.highspeedlib.init.ModEntities;
import com.maxwell.highspeedlib.init.ModItems;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;

@Mod(HighSpeedLib.MODID)
public class HighSpeedLib {
    public static final String MODID = "highspeedlib";
    public static final org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(MODID);

    public HighSpeedLib(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();
        PacketHandler.register();
        ModItems.ITEMS.register(modEventBus);
        ModEntities.ENTITIES.register(modEventBus);
        ModEnchantments.ENCHANTMENTS.register(modEventBus);
        ModAttributes.ATTRIBUTES.register(modEventBus);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, HighSpeedClientConfig.SPEC);
        modEventBus.addListener(this::clientSetup);
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            EntityRenderers.register(ModEntities.TCOIN.get(), ThrownItemRenderer::new);
            CuriosRendererRegistry.register(
                    ModItems.V1_WINGS.get(),
                    V1WingRenderer::new
            );
        });
    }
}
