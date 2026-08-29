package com.maxwell.highspeedlib.api.commands;

import com.maxwell.highspeedlib.HighSpeedLib;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@SuppressWarnings("removal")
@EventBusSubscriber(modid = HighSpeedLib.MODID, bus = EventBusSubscriber.Bus.GAME)
public class CommandRegistrationHandler {
    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        HighSpeedCommand.register(event.getDispatcher());
    }
}