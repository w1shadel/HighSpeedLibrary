package com.maxwell.highspeedlib.api.commands;

import com.maxwell.highspeedlib.api.config.HighSpeedServerConfig;
import com.maxwell.highspeedlib.common.logic.ability.AbilityManager;
import com.maxwell.highspeedlib.common.logic.state.PlayerAbilityState;
import com.maxwell.highspeedlib.common.logic.state.PlayerStateManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
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
                .then(Commands.literal("globalConfig")
                        .then(Commands.literal("ability_punch").then(Commands.argument("value", BoolArgumentType.bool())
                                .executes(ctx -> setGlobalBool("ability_punch", BoolArgumentType.getBool(ctx, "value"), ctx.getSource()))))
                        .then(Commands.literal("ability_dash").then(Commands.argument("value", BoolArgumentType.bool())
                                .executes(ctx -> setGlobalBool("ability_dash", BoolArgumentType.getBool(ctx, "value"), ctx.getSource()))))
                        .then(Commands.literal("ability_whiplash").then(Commands.argument("value", BoolArgumentType.bool())
                                .executes(ctx -> setGlobalBool("ability_whiplash", BoolArgumentType.getBool(ctx, "value"), ctx.getSource()))))
                        .then(Commands.literal("ability_walljump").then(Commands.argument("value", BoolArgumentType.bool())
                                .executes(ctx -> setGlobalBool("ability_walljump", BoolArgumentType.getBool(ctx, "value"), ctx.getSource()))))
                        .then(Commands.literal("ability_slide").then(Commands.argument("value", BoolArgumentType.bool())
                                .executes(ctx -> setGlobalBool("ability_slide", BoolArgumentType.getBool(ctx, "value"), ctx.getSource()))))
                        .then(Commands.literal("ability_slam").then(Commands.argument("value", BoolArgumentType.bool())
                                .executes(ctx -> setGlobalBool("ability_slam", BoolArgumentType.getBool(ctx, "value"), ctx.getSource()))))
                        .then(Commands.literal("dash_invul_ticks").then(Commands.argument("value", IntegerArgumentType.integer(0, 200))
                                .executes(ctx -> setGlobalInt("dash_invul_ticks", IntegerArgumentType.getInteger(ctx, "value"), ctx.getSource()))))
                        .then(Commands.literal("dash_max_count").then(Commands.argument("value", IntegerArgumentType.integer(1, 20))
                                .executes(ctx -> setGlobalInt("dash_max_count", IntegerArgumentType.getInteger(ctx, "value"), ctx.getSource()))))
                        .then(Commands.literal("stamina_regen").then(Commands.argument("value", DoubleArgumentType.doubleArg(0.0, 1.0))
                                .executes(ctx -> setGlobalDouble("stamina_regen", DoubleArgumentType.getDouble(ctx, "value"), ctx.getSource()))))
                        .then(Commands.literal("slide_speed").then(Commands.argument("value", DoubleArgumentType.doubleArg(0.1, 5.0))
                                .executes(ctx -> setGlobalDouble("slide_speed", DoubleArgumentType.getDouble(ctx, "value"), ctx.getSource()))))
                        .then(Commands.literal("slide_air_timeout").then(Commands.argument("value", IntegerArgumentType.integer(1, 200))
                                .executes(ctx -> setGlobalInt("slide_air_timeout", IntegerArgumentType.getInteger(ctx, "value"), ctx.getSource()))))
                        .then(Commands.literal("slide_jump_h_mult").then(Commands.argument("value", DoubleArgumentType.doubleArg(1.0, 5.0))
                                .executes(ctx -> setGlobalDouble("slide_jump_h_mult", DoubleArgumentType.getDouble(ctx, "value"), ctx.getSource()))))
                        .then(Commands.literal("slide_jump_v_base").then(Commands.argument("value", DoubleArgumentType.doubleArg(0.1, 2.0))
                                .executes(ctx -> setGlobalDouble("slide_jump_v_base", DoubleArgumentType.getDouble(ctx, "value"), ctx.getSource()))))
                        .then(Commands.literal("slide_jump_v_speed_mult").then(Commands.argument("value", DoubleArgumentType.doubleArg(0.0, 2.0))
                                .executes(ctx -> setGlobalDouble("slide_jump_v_speed_mult", DoubleArgumentType.getDouble(ctx, "value"), ctx.getSource()))))
                        .then(Commands.literal("walljump_max_count").then(Commands.argument("value", IntegerArgumentType.integer(1, 20))
                                .executes(ctx -> setGlobalInt("walljump_max_count", IntegerArgumentType.getInteger(ctx, "value"), ctx.getSource()))))
                        .then(Commands.literal("walljump_vertical_mult").then(Commands.argument("value", DoubleArgumentType.doubleArg(0.5, 5.0))
                                .executes(ctx -> setGlobalDouble("walljump_vertical_mult", DoubleArgumentType.getDouble(ctx, "value"), ctx.getSource()))))
                        .then(Commands.literal("walljump_horizontal_power").then(Commands.argument("value", DoubleArgumentType.doubleArg(0.1, 3.0))
                                .executes(ctx -> setGlobalDouble("walljump_horizontal_power", DoubleArgumentType.getDouble(ctx, "value"), ctx.getSource()))))
                        .then(Commands.literal("slam_downward_speed").then(Commands.argument("value", DoubleArgumentType.doubleArg(0.5, 20.0))
                                .executes(ctx -> setGlobalDouble("slam_downward_speed", DoubleArgumentType.getDouble(ctx, "value"), ctx.getSource()))))
                        .then(Commands.literal("slam_radius").then(Commands.argument("value", DoubleArgumentType.doubleArg(1.0, 20.0))
                                .executes(ctx -> setGlobalDouble("slam_radius", DoubleArgumentType.getDouble(ctx, "value"), ctx.getSource()))))
                        .then(Commands.literal("slam_knockup_power").then(Commands.argument("value", DoubleArgumentType.doubleArg(0.1, 5.0))
                                .executes(ctx -> setGlobalDouble("slam_knockup_power", DoubleArgumentType.getDouble(ctx, "value"), ctx.getSource()))))
                        .then(Commands.literal("punch_damage_base").then(Commands.argument("value", DoubleArgumentType.doubleArg(0.0, 100.0))
                                .executes(ctx -> setGlobalDouble("punch_damage_base", DoubleArgumentType.getDouble(ctx, "value"), ctx.getSource()))))
                        .then(Commands.literal("punch_energy_regen").then(Commands.argument("value", DoubleArgumentType.doubleArg(0.001, 1.0))
                                .executes(ctx -> setGlobalDouble("punch_energy_regen", DoubleArgumentType.getDouble(ctx, "value"), ctx.getSource()))))
                        .then(Commands.literal("parry_invul_seconds").then(Commands.argument("value", DoubleArgumentType.doubleArg(0.05, 100.0))
                                .executes(ctx -> setGlobalDouble("parry_invul_seconds", DoubleArgumentType.getDouble(ctx, "value"), ctx.getSource()))))
                        .then(Commands.literal("parry_counter_damage").then(Commands.argument("value", DoubleArgumentType.doubleArg(0.0, 100.0))
                                .executes(ctx -> setGlobalDouble("parry_counter_damage", DoubleArgumentType.getDouble(ctx, "value"), ctx.getSource()))))
                        .then(Commands.literal("coin_max_count").then(Commands.argument("value", IntegerArgumentType.integer(1, 20))
                                .executes(ctx -> setGlobalInt("coin_max_count", IntegerArgumentType.getInteger(ctx, "value"), ctx.getSource()))))
                        .then(Commands.literal("coin_regen_per_tick").then(Commands.argument("value", DoubleArgumentType.doubleArg(0.001, 1.0))
                                .executes(ctx -> setGlobalDouble("coin_regen_per_tick", DoubleArgumentType.getDouble(ctx, "value"), ctx.getSource()))))
                        .then(Commands.literal("coin_base_damage").then(Commands.argument("value", DoubleArgumentType.doubleArg(0.0, 100.0))
                                .executes(ctx -> setGlobalDouble("coin_base_damage", DoubleArgumentType.getDouble(ctx, "value"), ctx.getSource()))))
                        .then(Commands.literal("coin_parry_damage_per_count").then(Commands.argument("value", DoubleArgumentType.doubleArg(0.0, 50.0))
                                .executes(ctx -> setGlobalDouble("coin_parry_damage_per_count", DoubleArgumentType.getDouble(ctx, "value"), ctx.getSource()))))
                        .then(Commands.literal("list")
                                .executes(ctx -> printGlobalConfig(ctx.getSource())))
                )
        );
    }

    private static int updateAbility(ServerPlayer player, String type, boolean val) {
        PlayerAbilityState s = PlayerStateManager.getState(player).getAbility();
        switch (type) {
            case "punch" -> s.punch = val;
            case "dash" -> s.dash = val;
            case "whiplash" -> s.whiplash = val;
            case "walljump" -> s.wallJump = val;
            case "slide" -> s.sliding = val;
            case "slam" -> s.slam = val;
        }
        AbilityManager.sync(player);
        return 1;
    }

    private static int updateConfig(ServerPlayer player, String type, double val) {
        PlayerAbilityState s = PlayerStateManager.getState(player).getAbility();
        switch (type) {
            case "damage" -> s.punchDamageBase = val;
            case "dash_count" -> s.maxDashCount = (int) val;
            case "walljump_count" -> s.maxWallJumpCount = (int) val;
            case "parry_invtime" -> s.parry_invtime = (int) val;
        }
        AbilityManager.sync(player);
        return 1;
    }

    private static int setGlobalBool(String key, boolean value, CommandSourceStack source) {
        switch (key) {
            case "ability_punch" -> HighSpeedServerConfig.ABILITY_PUNCH.set(value);
            case "ability_dash" -> HighSpeedServerConfig.ABILITY_DASH.set(value);
            case "ability_whiplash" -> HighSpeedServerConfig.ABILITY_WHIPLASH.set(value);
            case "ability_walljump" -> HighSpeedServerConfig.ABILITY_WALLJUMP.set(value);
            case "ability_slide" -> HighSpeedServerConfig.ABILITY_SLIDE.set(value);
            case "ability_slam" -> HighSpeedServerConfig.ABILITY_SLAM.set(value);
        }
        HighSpeedServerConfig.SPEC.save();
        source.sendSuccess(() -> Component.literal("[HighSpeed] §a" + key + " §f= §b" + value + " §7(保存済み)"), true);
        return 1;
    }

    private static int setGlobalDouble(String key, double value, CommandSourceStack source) {
        switch (key) {
            case "stamina_regen" -> HighSpeedServerConfig.STAMINA_REGEN_PER_TICK.set(value);
            case "slide_speed" -> HighSpeedServerConfig.SLIDE_SPEED.set(value);
            case "slide_jump_h_mult" -> HighSpeedServerConfig.SLIDE_JUMP_HORIZONTAL_MULT.set(value);
            case "slide_jump_v_base" -> HighSpeedServerConfig.SLIDE_JUMP_VERTICAL_BASE.set(value);
            case "slide_jump_v_speed_mult" -> HighSpeedServerConfig.SLIDE_JUMP_VERTICAL_SPEED_MULT.set(value);
            case "walljump_vertical_mult" -> HighSpeedServerConfig.WALLJUMP_VERTICAL_MULT.set(value);
            case "walljump_horizontal_power" -> HighSpeedServerConfig.WALLJUMP_HORIZONTAL_POWER.set(value);
            case "slam_downward_speed" -> HighSpeedServerConfig.SLAM_DOWNWARD_SPEED.set(value);
            case "slam_radius" -> HighSpeedServerConfig.SLAM_RADIUS.set(value);
            case "slam_knockup_power" -> HighSpeedServerConfig.SLAM_KNOCKUP_POWER.set(value);
            case "punch_damage_base" -> HighSpeedServerConfig.PUNCH_DAMAGE_BASE.set(value);
            case "punch_energy_regen" -> HighSpeedServerConfig.PUNCH_ENERGY_REGEN_PER_TICK.set(value);
            case "parry_invul_seconds" -> HighSpeedServerConfig.PARRY_INVUL_SECONDS.set(value);
            case "parry_counter_damage" -> HighSpeedServerConfig.PARRY_COUNTER_DAMAGE.set(value);
            case "coin_regen_per_tick" -> HighSpeedServerConfig.COIN_REGEN_PER_TICK.set(value);
            case "coin_base_damage" -> HighSpeedServerConfig.COIN_BASE_DAMAGE.set(value);
            case "coin_parry_damage_per_count" -> HighSpeedServerConfig.COIN_PARRY_DAMAGE_PER_COUNT.set(value);
        }
        HighSpeedServerConfig.SPEC.save();
        source.sendSuccess(() -> Component.literal("[HighSpeed] §a" + key + " §f= §b" + value + " §7(保存済み)"), true);
        return 1;
    }

    private static int setGlobalInt(String key, int value, CommandSourceStack source) {
        switch (key) {
            case "dash_invul_ticks" -> HighSpeedServerConfig.DASH_INVUL_TICKS.set(value);
            case "dash_max_count" -> HighSpeedServerConfig.DASH_MAX_COUNT.set(value);
            case "slide_air_timeout" -> HighSpeedServerConfig.SLIDE_AIR_TIMEOUT_TICKS.set(value);
            case "walljump_max_count" -> HighSpeedServerConfig.WALLJUMP_MAX_COUNT.set(value);
            case "coin_max_count" -> HighSpeedServerConfig.COIN_MAX_COUNT.set(value);
        }
        HighSpeedServerConfig.SPEC.save();
        source.sendSuccess(() -> Component.literal("[HighSpeed] §a" + key + " §f= §b" + value + " §7(保存済み)"), true);
        return 1;
    }

    private static int printGlobalConfig(CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal("§e=== HighSpeed グローバルコンフィグ ==="), false);
        source.sendSuccess(() -> Component.literal("§7[Abilities]"), false);
        source.sendSuccess(() -> Component.literal("  ability_punch=" + HighSpeedServerConfig.ABILITY_PUNCH.get()
                + "  ability_dash=" + HighSpeedServerConfig.ABILITY_DASH.get()
                + "  ability_whiplash=" + HighSpeedServerConfig.ABILITY_WHIPLASH.get()), false);
        source.sendSuccess(() -> Component.literal("  ability_walljump=" + HighSpeedServerConfig.ABILITY_WALLJUMP.get()
                + "  ability_slide=" + HighSpeedServerConfig.ABILITY_SLIDE.get()
                + "  ability_slam=" + HighSpeedServerConfig.ABILITY_SLAM.get()), false);
        source.sendSuccess(() -> Component.literal("§7[Dash]"), false);
        source.sendSuccess(() -> Component.literal("  dash_invul_ticks=" + HighSpeedServerConfig.DASH_INVUL_TICKS.get()
                + "  dash_max_count=" + HighSpeedServerConfig.DASH_MAX_COUNT.get()), false);
        source.sendSuccess(() -> Component.literal("§7[Stamina]"), false);
        source.sendSuccess(() -> Component.literal("  stamina_regen=" + HighSpeedServerConfig.STAMINA_REGEN_PER_TICK.get()), false);
        source.sendSuccess(() -> Component.literal("§7[Slide]"), false);
        source.sendSuccess(() -> Component.literal("  slide_speed=" + HighSpeedServerConfig.SLIDE_SPEED.get()
                + "  air_timeout=" + HighSpeedServerConfig.SLIDE_AIR_TIMEOUT_TICKS.get()
                + "  jump_h_mult=" + HighSpeedServerConfig.SLIDE_JUMP_HORIZONTAL_MULT.get()), false);
        source.sendSuccess(() -> Component.literal("  jump_v_base=" + HighSpeedServerConfig.SLIDE_JUMP_VERTICAL_BASE.get()
                + "  jump_v_speed_mult=" + HighSpeedServerConfig.SLIDE_JUMP_VERTICAL_SPEED_MULT.get()), false);
        source.sendSuccess(() -> Component.literal("§7[WallJump]"), false);
        source.sendSuccess(() -> Component.literal("  walljump_max_count=" + HighSpeedServerConfig.WALLJUMP_MAX_COUNT.get()
                + "  vertical_mult=" + HighSpeedServerConfig.WALLJUMP_VERTICAL_MULT.get()
                + "  horizontal_power=" + HighSpeedServerConfig.WALLJUMP_HORIZONTAL_POWER.get()), false);
        source.sendSuccess(() -> Component.literal("§7[Slam]"), false);
        source.sendSuccess(() -> Component.literal("  downward_speed=" + HighSpeedServerConfig.SLAM_DOWNWARD_SPEED.get()
                + "  radius=" + HighSpeedServerConfig.SLAM_RADIUS.get()
                + "  knockup_power=" + HighSpeedServerConfig.SLAM_KNOCKUP_POWER.get()), false);
        source.sendSuccess(() -> Component.literal("§7[Punch]"), false);
        source.sendSuccess(() -> Component.literal("  damage_base=" + HighSpeedServerConfig.PUNCH_DAMAGE_BASE.get()
                + "  energy_regen=" + HighSpeedServerConfig.PUNCH_ENERGY_REGEN_PER_TICK.get()
                + "  parry_invul_seconds=" + HighSpeedServerConfig.PARRY_INVUL_SECONDS.get()
                + "  parry_counter_damage=" + HighSpeedServerConfig.PARRY_COUNTER_DAMAGE.get()), false);
        source.sendSuccess(() -> Component.literal("§7[Coin]"), false);
        source.sendSuccess(() -> Component.literal("  coin_max_count=" + HighSpeedServerConfig.COIN_MAX_COUNT.get()
                + "  regen=" + HighSpeedServerConfig.COIN_REGEN_PER_TICK.get()
                + "  base_damage=" + HighSpeedServerConfig.COIN_BASE_DAMAGE.get()
                + "  parry_bonus=" + HighSpeedServerConfig.COIN_PARRY_DAMAGE_PER_COUNT.get()), false);
        return 1;
    }
}