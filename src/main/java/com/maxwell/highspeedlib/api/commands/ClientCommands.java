package com.maxwell.highspeedlib.api.commands;

import com.maxwell.highspeedlib.HighSpeedLib;
import com.maxwell.highspeedlib.api.config.HighSpeedClientConfig;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = HighSpeedLib.MODID, value = Dist.CLIENT)
public class ClientCommands {
    @SubscribeEvent
    public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(Commands.literal("highspeedclient")
                .then(Commands.literal("visible")
                        .then(Commands.literal("ultrahud")
                                .then(Commands.argument("show", BoolArgumentType.bool())
                                        .executes(ctx -> {
                                            boolean show = BoolArgumentType.getBool(ctx, "show");
                                            HighSpeedClientConfig.ULTRAHUD_VISIBLE.set(show);
                                            ctx.getSource().sendSuccess(() -> Component.literal("UltraHUD visibility set to: " + show), false);
                                            return 1;
                                        })))
                        .then(Commands.literal("cheatenabled")
                                .then(Commands.argument("show", BoolArgumentType.bool())
                                        .executes(ctx -> {
                                            boolean show = BoolArgumentType.getBool(ctx, "show");
                                            HighSpeedClientConfig.CHEAT_INDICATOR_VISIBLE.set(show);
                                            ctx.getSource().sendSuccess(() -> Component.literal("Cheat Enabled indicator visibility set to: " + show), false);
                                            return 1;
                                        }))))
                .then(Commands.literal("pos")
                        .then(Commands.argument("x", IntegerArgumentType.integer())
                                .then(Commands.argument("y", IntegerArgumentType.integer())
                                        .executes(ctx -> {
                                            int x = IntegerArgumentType.getInteger(ctx, "x");
                                            int y = IntegerArgumentType.getInteger(ctx, "y");
                                            HighSpeedClientConfig.HUD_OFFSET_X.set(x);
                                            HighSpeedClientConfig.HUD_OFFSET_Y.set(y);
                                            ctx.getSource().sendSuccess(() -> Component.literal("HUD position updated to: " + x + ", " + y), false);
                                            return 1;
                                        }))))
        );
    }
}