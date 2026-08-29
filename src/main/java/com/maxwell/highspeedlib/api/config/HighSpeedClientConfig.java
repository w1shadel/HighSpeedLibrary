package com.maxwell.highspeedlib.api.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class HighSpeedClientConfig {
    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue ULTRAHUD_VISIBLE;
    public static final ModConfigSpec.BooleanValue CHEAT_INDICATOR_VISIBLE;
    public static final ModConfigSpec.IntValue HUD_OFFSET_X;
    public static final ModConfigSpec.IntValue HUD_OFFSET_Y;
    public static final ModConfigSpec.ConfigValue<String> HUD_COLOR_HP;
    public static final ModConfigSpec.ConfigValue<String> HUD_COLOR_ABSORPTION;
    public static final ModConfigSpec.ConfigValue<String> HUD_COLOR_STAMINA_NORMAL;
    public static final ModConfigSpec.ConfigValue<String> HUD_COLOR_STAMINA_LOW;
    public static final ModConfigSpec.ConfigValue<String> HUD_COLOR_ENERGY;
    public static final ModConfigSpec.ConfigValue<String> HUD_COLOR_COIN;
    public static final ModConfigSpec.ConfigValue<String> HUD_COLOR_FOOD;
    public static final ModConfigSpec.ConfigValue<String> HUD_COLOR_EXP;
    public static final ModConfigSpec.DoubleValue HUD_SCALE_LEFT;
    public static final ModConfigSpec.DoubleValue HUD_SCALE_RIGHT;
    public static final ModConfigSpec.DoubleValue HUD_TILT;
    public static final ModConfigSpec.DoubleValue HUD_YAW_SENSITIVITY;
    public static final ModConfigSpec.DoubleValue HUD_PITCH_SENSITIVITY;
    public static final ModConfigSpec.DoubleValue HUD_MAX_OFFSET_YAW;
    public static final ModConfigSpec.DoubleValue HUD_MAX_OFFSET_PITCH;
    public static final ModConfigSpec.DoubleValue HUD_POINTS_PER_HP;
    public static final ModConfigSpec.IntValue HUD_SLOTS_TO_SHOW;

    static {
        BUILDER.push("General Settings");
        ULTRAHUD_VISIBLE = BUILDER.comment("Whether to show the UltraHUD").define("ultrahudVisible", true);
        CHEAT_INDICATOR_VISIBLE = BUILDER.comment("Whether to show the Cheat Enabled indicator").define("cheatIndicatorVisible", true);
        HUD_OFFSET_X = BUILDER.comment("Horizontal offset of the HUD").defineInRange("hudOffsetX", 30, -1000, 1000);
        HUD_OFFSET_Y = BUILDER.comment("Vertical offset of the HUD").defineInRange("hudOffsetY", 140, 0, 1000);
        BUILDER.pop();
        BUILDER.push("HUD Colors");
        HUD_COLOR_HP = BUILDER.comment("Color of the HP bar (ARGB Hex)").define("colorHP", "FFFF2222");
        HUD_COLOR_ABSORPTION = BUILDER.comment("Color of the Absorption HP bar (ARGB Hex)").define("colorAbsorption", "FF44FF44");
        HUD_COLOR_STAMINA_NORMAL = BUILDER.comment("Color of the Stamina bar (Normal state, ARGB Hex)").define("colorStaminaNormal", "FF66EEFF");
        HUD_COLOR_STAMINA_LOW = BUILDER.comment("Color of the Stamina bar (Low state, ARGB Hex)").define("colorStaminaLow", "FFFF4444");
        HUD_COLOR_ENERGY = BUILDER.comment("Color of the Punch Energy bar (ARGB Hex)").define("colorEnergy", "FF444444");
        HUD_COLOR_COIN = BUILDER.comment("Color of the Coin icons (ARGB Hex)").define("colorCoin", "FFFFFF00");
        HUD_COLOR_FOOD = BUILDER.comment("Color of the Food bar (ARGB Hex)").define("colorFood", "FFFF8800");
        HUD_COLOR_EXP = BUILDER.comment("Color of the Experience bar (ARGB Hex)").define("colorExp", "FF00FF00");
        BUILDER.pop();
        BUILDER.push("HUD Perspective & Movement");
        HUD_SCALE_LEFT = BUILDER.comment("Scale factor for the left side of the HUD").defineInRange("scaleLeft", 1.0, 0.1, 5.0);
        HUD_SCALE_RIGHT = BUILDER.comment("Scale factor for the right side of the HUD").defineInRange("scaleRight", 0.85, 0.1, 5.0);
        HUD_TILT = BUILDER.comment("Tilt angle of the HUD (degrees)").defineInRange("tiltDegrees", -22.0, -90.0, 90.0);
        HUD_YAW_SENSITIVITY = BUILDER.comment("HUD follow sensitivity for Yaw rotation").defineInRange("yawSensitivity", 1.5, 0.0, 10.0);
        HUD_PITCH_SENSITIVITY = BUILDER.comment("HUD follow sensitivity for Pitch rotation").defineInRange("pitchSensitivity", 1.5, 0.0, 10.0);
        HUD_MAX_OFFSET_YAW = BUILDER.comment("Maximum HUD horizontal offset").defineInRange("maxOffsetYaw", 15.0, 0.0, 100.0);
        HUD_MAX_OFFSET_PITCH = BUILDER.comment("Maximum HUD vertical offset").defineInRange("maxOffsetPitch", 10.0, 0.0, 100.0);
        BUILDER.pop();
        BUILDER.push("HUD Display Options");
        HUD_POINTS_PER_HP = BUILDER.comment("Display value multiplier per 1 HP (half-heart)").defineInRange("pointsPerHP", 5.0, 1.0, 100.0);
        HUD_SLOTS_TO_SHOW = BUILDER.comment("Number of upcoming inventory slots to show on HUD").defineInRange("slotsToShow", 3, 0, 8);
        BUILDER.pop();
        SPEC = BUILDER.build();
    }

    public static int getColor(ModConfigSpec.ConfigValue<String> colorValue) {
        try {
            return (int) Long.parseLong(colorValue.get(), 16);
        } catch (NumberFormatException e) {
            return 0xFFFFFFFF;
        }
    }
}
