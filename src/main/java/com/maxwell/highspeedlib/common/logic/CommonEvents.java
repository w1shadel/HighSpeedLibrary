package com.maxwell.highspeedlib.common.logic;

import com.maxwell.highspeedlib.HighSpeedLib;
import com.maxwell.highspeedlib.agent.AbsoluteHook;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = HighSpeedLib.MODID)
public class CommonEvents {
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            TimeManager.tick();
        }
    }
}
