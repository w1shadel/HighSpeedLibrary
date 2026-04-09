package com.maxwell.highspeedlib.api.commands;

import com.maxwell.highspeedlib.common.logic.AbilityAuthority;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;

public class HighSpeedCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("highspeed")
                .requires(source -> source.hasPermission(3))
                .then(Commands.literal("ability")
                        .then(Commands.argument("target", EntityArgument.player())
                                .then(Commands.literal("punch")
                                        .then(Commands.argument("value", BoolArgumentType.bool())
                                                .executes(ctx -> updateAbility(EntityArgument.getPlayer(ctx, "target"), "punch", BoolArgumentType.getBool(ctx, "value")))))
                                .then(Commands.literal("dash")
                                        .then(Commands.argument("value", BoolArgumentType.bool())
                                                .executes(ctx -> updateAbility(EntityArgument.getPlayer(ctx, "target"), "dash", BoolArgumentType.getBool(ctx, "value")))))
                                .then(Commands.literal("whiplash")
                                        .then(Commands.argument("value", BoolArgumentType.bool())
                                                .executes(ctx -> updateAbility(EntityArgument.getPlayer(ctx, "target"), "whiplash", BoolArgumentType.getBool(ctx, "value")))))
                                .then(Commands.literal("walljump")
                                        .then(Commands.argument("value", BoolArgumentType.bool())
                                                .executes(ctx -> updateAbility(EntityArgument.getPlayer(ctx, "target"), "walljump", BoolArgumentType.getBool(ctx, "value")))))
                                .then(Commands.literal("slam").then(Commands.argument("value", BoolArgumentType.bool()).executes(ctx -> updateAbility(EntityArgument.getPlayer(ctx, "target"), "slam", BoolArgumentType.getBool(ctx, "value")))))
                                .then(Commands.literal("slide").then(Commands.argument("value", BoolArgumentType.bool()).executes(ctx -> updateAbility(EntityArgument.getPlayer(ctx, "target"), "slide", BoolArgumentType.getBool(ctx, "value")))))
                        )
                )
                .then(Commands.literal("config")
                        .then(Commands.argument("target", EntityArgument.player())
                                .then(Commands.literal("damage")
                                        .then(Commands.argument("value", DoubleArgumentType.doubleArg(0.0))
                                                .executes(ctx -> updateConfig(EntityArgument.getPlayer(ctx, "target"), "damage", DoubleArgumentType.getDouble(ctx, "value")))))
                                .then(Commands.literal("dash_count")
                                        .then(Commands.argument("value", IntegerArgumentType.integer(0))
                                                .executes(ctx -> updateConfig(EntityArgument.getPlayer(ctx, "target"), "dash_count", (double) IntegerArgumentType.getInteger(ctx, "value")))))
                                .then(Commands.literal("walljump_count")
                                        .then(Commands.argument("value", IntegerArgumentType.integer(0))
                                                .executes(ctx -> updateConfig(EntityArgument.getPlayer(ctx, "target"), "walljump_count", (double) IntegerArgumentType.getInteger(ctx, "value")))))
                                .then(Commands.literal("parry_invtime")
                                        .then(Commands.argument("value", IntegerArgumentType.integer(0))
                                                .executes(ctx -> updateConfig(EntityArgument.getPlayer(ctx, "target"), "parry_invtime", (double) IntegerArgumentType.getInteger(ctx, "value")))))
                        )
                )
        );
    }

    private static int updateAbility(ServerPlayer player, String type, boolean val) {
        AbilityAuthority.PlayerSettings s = AbilityAuthority.get(player.getUUID());
        switch (type) {
            case "punch" -> s.punch = val;
            case "dash" -> s.dash = val;
            case "whiplash" -> s.whiplash = val;
            case "walljump" -> s.wallJump = val;
            case "slide" -> s.sliding = val;
            case "slam" -> s.slam = val;
        }
        AbilityAuthority.sync(player);
        return 1;
    }

    private static int updateConfig(ServerPlayer player, String type, double val) {
        AbilityAuthority.PlayerSettings s = AbilityAuthority.get(player.getUUID());
        switch (type) {
            case "damage" -> s.punchDamageBase = val;
            case "dash_count" -> s.maxDashCount = (int) val;
            case "walljump_count" -> s.maxWallJumpCount = (int) val;
            case "parry_invtime" -> s.parry_invtime = (int) val;
        }
        AbilityAuthority.sync(player);
        return 1;
    }
}