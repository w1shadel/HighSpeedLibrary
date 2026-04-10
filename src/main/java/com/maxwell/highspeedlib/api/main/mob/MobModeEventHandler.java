package com.maxwell.highspeedlib.api.main.mob;

import com.maxwell.highspeedlib.HighSpeedLib;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = HighSpeedLib.MODID)
public class MobModeEventHandler {
    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof LivingEntity living) {
            MobModeManager.sync(living);
        }
    }
}