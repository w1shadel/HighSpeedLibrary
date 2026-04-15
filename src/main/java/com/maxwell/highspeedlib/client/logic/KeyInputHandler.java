package com.maxwell.highspeedlib.client.logic;

import com.maxwell.highspeedlib.client.V1WingModel;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class KeyInputHandler {
    public static KeyMapping DASH_KEY;
    public static KeyMapping PARRY_KEY;
    public static KeyMapping SLIDING_KEY;
    public static KeyMapping COIN_KEY;
    public static KeyMapping CHANGEARM_KEY;
    public static KeyMapping WHIPLASH_KEY;

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        DASH_KEY = new KeyMapping(
                "key.highspeedlib.dash",
                InputConstants.KEY_LSHIFT,
                "category.highspeedlib"
        );
        PARRY_KEY = new KeyMapping(
                "key.highspeedlib.parry",
                InputConstants.KEY_F,
                "category.highspeedlib"
        );
        SLIDING_KEY = new KeyMapping(
                "key.highspeedlib.sliding",
                InputConstants.KEY_LCONTROL,
                "category.highspeedlib"
        );
        COIN_KEY = new KeyMapping(
                "key.highspeedlib.coin",
                InputConstants.KEY_Z,
                "category.highspeedlib"
        );
        CHANGEARM_KEY = new KeyMapping(
                "key.highspeedlib.changearm",
                InputConstants.KEY_G,
                "category.highspeedlib"
        );
        WHIPLASH_KEY = new KeyMapping(
                "key.highspeedlib.whiplash",
                InputConstants.KEY_R,
                "category.highspeedlib"
        );
        event.register(DASH_KEY);
        event.register(PARRY_KEY);
        event.register(SLIDING_KEY);
        event.register(COIN_KEY);
        event.register(CHANGEARM_KEY);
        event.register(WHIPLASH_KEY);
    }

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(V1WingModel.LAYER_LOCATION, V1WingModel::createBodyLayer);
    }
}
