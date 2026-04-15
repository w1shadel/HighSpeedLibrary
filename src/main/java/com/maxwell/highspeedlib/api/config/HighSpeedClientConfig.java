package com.maxwell.highspeedlib.api.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class HighSpeedClientConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue ULTRAHUD_VISIBLE;
    public static final ForgeConfigSpec.BooleanValue CHEAT_INDICATOR_VISIBLE;
    public static final ForgeConfigSpec.IntValue HUD_OFFSET_X;
    public static final ForgeConfigSpec.IntValue HUD_OFFSET_Y;

    static {
        BUILDER.push("HighSpeedClient Settings");
        ULTRAHUD_VISIBLE = BUILDER.comment("Show or hide the UltraHUD").define("ultrahudVisible", true);
        CHEAT_INDICATOR_VISIBLE = BUILDER.comment("Show or hide the Cheat Enabled indicator").define("cheatIndicatorVisible", true);
        HUD_OFFSET_X = BUILDER.comment("Horizontal offset from left").defineInRange("hudOffsetX", 30, -1000, 1000);
        HUD_OFFSET_Y = BUILDER.comment("Vertical offset from bottom (negative is up)").defineInRange("hudOffsetY", 140, 0, 1000);
        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}