package com.maxwell.highspeedlib.api.main.mob;

import com.maxwell.highspeedlib.HighSpeedLib;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = HighSpeedLib.MODID)
public class MobModeEventHandler {
    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof LivingEntity living) {
            MobModeManager.sync(living);
        }
    }
}