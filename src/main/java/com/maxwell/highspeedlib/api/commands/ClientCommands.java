package com.maxwell.highspeedlib.api.commands;

import com.maxwell.highspeedlib.HighSpeedLib;
import com.maxwell.highspeedlib.api.config.HighSpeedClientConfig;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;

@EventBusSubscriber(modid = HighSpeedLib.MODID, value = Dist.CLIENT)
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
                .then(Commands.literal("color")
                        .then(Commands.literal("hp").then(Commands.argument("hex", StringArgumentType.string()).executes(ctx -> setColor("colorHP", StringArgumentType.getString(ctx, "hex"), ctx.getSource()))))
                        .then(Commands.literal("stamina").then(Commands.argument("hex", StringArgumentType.string()).executes(ctx -> setColor("colorStaminaNormal", StringArgumentType.getString(ctx, "hex"), ctx.getSource()))))
                        .then(Commands.literal("stamina_low").then(Commands.argument("hex", StringArgumentType.string()).executes(ctx -> setColor("colorStaminaLow", StringArgumentType.getString(ctx, "hex"), ctx.getSource()))))
                        .then(Commands.literal("coin").then(Commands.argument("hex", StringArgumentType.string()).executes(ctx -> setColor("colorCoin", StringArgumentType.getString(ctx, "hex"), ctx.getSource()))))
                        .then(Commands.literal("food").then(Commands.argument("hex", StringArgumentType.string()).executes(ctx -> setColor("colorFood", StringArgumentType.getString(ctx, "hex"), ctx.getSource()))))
                        .then(Commands.literal("exp").then(Commands.argument("hex", StringArgumentType.string()).executes(ctx -> setColor("colorExp", StringArgumentType.getString(ctx, "hex"), ctx.getSource()))))
                )
                .then(Commands.literal("view")
                        .then(Commands.literal("scale_left").then(Commands.argument("val", DoubleArgumentType.doubleArg(0.1, 5.0)).executes(ctx -> setDouble("scaleLeft", DoubleArgumentType.getDouble(ctx, "val"), ctx.getSource()))))
                        .then(Commands.literal("scale_right").then(Commands.argument("val", DoubleArgumentType.doubleArg(0.1, 5.0)).executes(ctx -> setDouble("scaleRight", DoubleArgumentType.getDouble(ctx, "val"), ctx.getSource()))))
                        .then(Commands.literal("tilt").then(Commands.argument("val", DoubleArgumentType.doubleArg(-90.0, 90.0)).executes(ctx -> setDouble("tiltDegrees", DoubleArgumentType.getDouble(ctx, "val"), ctx.getSource()))))
                )
                .then(Commands.literal("sensitivity")
                        .then(Commands.literal("yaw").then(Commands.argument("val", DoubleArgumentType.doubleArg(0.0, 10.0)).executes(ctx -> setDouble("yawSensitivity", DoubleArgumentType.getDouble(ctx, "val"), ctx.getSource()))))
                        .then(Commands.literal("pitch").then(Commands.argument("val", DoubleArgumentType.doubleArg(0.0, 10.0)).executes(ctx -> setDouble("pitchSensitivity", DoubleArgumentType.getDouble(ctx, "val"), ctx.getSource()))))
                        .then(Commands.literal("max_yaw").then(Commands.argument("val", DoubleArgumentType.doubleArg(0.0, 100.0)).executes(ctx -> setDouble("maxOffsetYaw", DoubleArgumentType.getDouble(ctx, "val"), ctx.getSource()))))
                        .then(Commands.literal("max_pitch").then(Commands.argument("val", DoubleArgumentType.doubleArg(0.0, 100.0)).executes(ctx -> setDouble("maxOffsetPitch", DoubleArgumentType.getDouble(ctx, "val"), ctx.getSource()))))
                )
                .then(Commands.literal("options")
                        .then(Commands.literal("points_per_hp").then(Commands.argument("val", DoubleArgumentType.doubleArg(1.0, 100.0)).executes(ctx -> setDouble("pointsPerHP", DoubleArgumentType.getDouble(ctx, "val"), ctx.getSource()))))
                        .then(Commands.literal("slots").then(Commands.argument("val", IntegerArgumentType.integer(0, 8)).executes(ctx -> setInt("slotsToShow", IntegerArgumentType.getInteger(ctx, "val"), ctx.getSource()))))
                )
        );
    }

    private static int setColor(String key, String hex, CommandSourceStack source) {
        if (hex.length() != 8) {
            source.sendFailure(Component.literal("Invalid hex color. Use ARGB format (e.g. FFFF2222)"));
            return 0;
        }
        switch (key) {
            case "colorHP" -> HighSpeedClientConfig.HUD_COLOR_HP.set(hex);
            case "colorStaminaNormal" -> HighSpeedClientConfig.HUD_COLOR_STAMINA_NORMAL.set(hex);
            case "colorStaminaLow" -> HighSpeedClientConfig.HUD_COLOR_STAMINA_LOW.set(hex);
            case "colorCoin" -> HighSpeedClientConfig.HUD_COLOR_COIN.set(hex);
            case "colorFood" -> HighSpeedClientConfig.HUD_COLOR_FOOD.set(hex);
            case "colorExp" -> HighSpeedClientConfig.HUD_COLOR_EXP.set(hex);
        }
        HighSpeedClientConfig.SPEC.save();
        source.sendSuccess(() -> Component.literal("[Client] §a" + key + " §f= §b#" + hex), false);
        return 1;
    }

    private static int setDouble(String key, double val, CommandSourceStack source) {
        switch (key) {
            case "scaleLeft" -> HighSpeedClientConfig.HUD_SCALE_LEFT.set(val);
            case "scaleRight" -> HighSpeedClientConfig.HUD_SCALE_RIGHT.set(val);
            case "tiltDegrees" -> HighSpeedClientConfig.HUD_TILT.set(val);
            case "yawSensitivity" -> HighSpeedClientConfig.HUD_YAW_SENSITIVITY.set(val);
            case "pitchSensitivity" -> HighSpeedClientConfig.HUD_PITCH_SENSITIVITY.set(val);
            case "maxOffsetYaw" -> HighSpeedClientConfig.HUD_MAX_OFFSET_YAW.set(val);
            case "maxOffsetPitch" -> HighSpeedClientConfig.HUD_MAX_OFFSET_PITCH.set(val);
            case "pointsPerHP" -> HighSpeedClientConfig.HUD_POINTS_PER_HP.set(val);
        }
        HighSpeedClientConfig.SPEC.save();
        source.sendSuccess(() -> Component.literal("[Client] §a" + key + " §f= §b" + val), false);
        return 1;
    }

    private static int setInt(String key, int val, CommandSourceStack source) {
        switch (key) {
            case "slotsToShow" -> HighSpeedClientConfig.HUD_SLOTS_TO_SHOW.set(val);
        }
        HighSpeedClientConfig.SPEC.save();
        source.sendSuccess(() -> Component.literal("[Client] §a" + key + " §f= §b" + val), false);
        return 1;
    }
}
