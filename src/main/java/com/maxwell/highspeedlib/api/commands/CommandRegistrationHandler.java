package com.maxwell.highspeedlib.api.commands;

import com.maxwell.highspeedlib.HighSpeedLib;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = HighSpeedLib.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CommandRegistrationHandler {
    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        HighSpeedCommand.register(event.getDispatcher());
    }
}