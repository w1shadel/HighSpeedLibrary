package com.maxwell.highspeedlib.api.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class HighSpeedServerConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue ABILITY_PUNCH;
    public static final ForgeConfigSpec.BooleanValue ABILITY_DASH;
    public static final ForgeConfigSpec.BooleanValue ABILITY_WHIPLASH;
    public static final ForgeConfigSpec.BooleanValue ABILITY_WALLJUMP;
    public static final ForgeConfigSpec.BooleanValue ABILITY_SLIDE;
    public static final ForgeConfigSpec.BooleanValue ABILITY_SLAM;
    public static final ForgeConfigSpec.IntValue DASH_INVUL_TICKS;
    public static final ForgeConfigSpec.IntValue DASH_MAX_COUNT;
    public static final ForgeConfigSpec.IntValue STAMINA_MAX;
    public static final ForgeConfigSpec.DoubleValue STAMINA_REGEN_PER_TICK;
    public static final ForgeConfigSpec.DoubleValue SLIDE_SPEED;
    public static final ForgeConfigSpec.IntValue SLIDE_AIR_TIMEOUT_TICKS;
    public static final ForgeConfigSpec.DoubleValue SLIDE_JUMP_HORIZONTAL_MULT;
    public static final ForgeConfigSpec.DoubleValue SLIDE_JUMP_VERTICAL_BASE;
    public static final ForgeConfigSpec.DoubleValue SLIDE_JUMP_VERTICAL_SPEED_MULT;
    public static final ForgeConfigSpec.IntValue WALLJUMP_MAX_COUNT;
    public static final ForgeConfigSpec.DoubleValue WALLJUMP_VERTICAL_MULT;
    public static final ForgeConfigSpec.DoubleValue WALLJUMP_HORIZONTAL_POWER;
    public static final ForgeConfigSpec.DoubleValue SLAM_DOWNWARD_SPEED;
    public static final ForgeConfigSpec.DoubleValue SLAM_RADIUS;
    public static final ForgeConfigSpec.DoubleValue SLAM_KNOCKUP_POWER;
    public static final ForgeConfigSpec.DoubleValue PUNCH_DAMAGE_BASE;
    public static final ForgeConfigSpec.DoubleValue PUNCH_ENERGY_REGEN_PER_TICK;
    public static final ForgeConfigSpec.DoubleValue PARRY_INVUL_SECONDS;
    public static final ForgeConfigSpec.DoubleValue PARRY_COUNTER_DAMAGE;
    public static final ForgeConfigSpec.IntValue COIN_MAX_COUNT;
    public static final ForgeConfigSpec.DoubleValue COIN_REGEN_PER_TICK;
    public static final ForgeConfigSpec.DoubleValue COIN_BASE_DAMAGE;
    public static final ForgeConfigSpec.DoubleValue COIN_PARRY_DAMAGE_PER_COUNT;

    static {
        BUILDER.comment("HighSpeedLib Server Configuration");
        BUILDER.push("abilities");
        ABILITY_PUNCH = BUILDER.comment("パンチ能力のデフォルト有効状態").define("punch", true);
        ABILITY_DASH = BUILDER.comment("ダッシュ能力のデフォルト有効状態").define("dash", true);
        ABILITY_WHIPLASH = BUILDER.comment("ウィップラッシュ能力のデフォルト有効状態").define("whiplash", true);
        ABILITY_WALLJUMP = BUILDER.comment("壁キック能力のデフォルト有効状態").define("walljump", true);
        ABILITY_SLIDE = BUILDER.comment("スライディング能力のデフォルト有効状態").define("slide", true);
        ABILITY_SLAM = BUILDER.comment("スラム能力のデフォルト有効状態").define("slam", true);
        BUILDER.pop();
        BUILDER.push("dash");
        DASH_INVUL_TICKS = BUILDER.comment("ダッシュ時の無敵フレーム数").defineInRange("invulTicks", 6, 0, 200);
        DASH_MAX_COUNT = BUILDER.comment("最大ダッシュ回数 (= 最大スタミナ)").defineInRange("maxCount", 3, 1, 20);
        BUILDER.pop();
        BUILDER.push("stamina");
        STAMINA_MAX = BUILDER.comment("基本最大スタミナ量 (enchantmentなし)").defineInRange("maxBase", 3, 1, 20);
        STAMINA_REGEN_PER_TICK = BUILDER.comment("1Tickごとのスタミナ回復量 (1/20秒ごと)").defineInRange("regenPerTick", 0.04, 0.0, 1.0);
        BUILDER.pop();
        BUILDER.push("slide");
        SLIDE_SPEED = BUILDER.comment("スライディング中の水平速度").defineInRange("speed", 0.75, 0.1, 5.0);
        SLIDE_AIR_TIMEOUT_TICKS = BUILDER.comment("地面を離れた後にスライドが解除されるまでのTick数").defineInRange("airTimeoutTicks", 20, 1, 200);
        SLIDE_JUMP_HORIZONTAL_MULT = BUILDER.comment("スライディング中ジャンプ時の水平速度倍率").defineInRange("jumpHorizontalMult", 1.8, 1.0, 5.0);
        SLIDE_JUMP_VERTICAL_BASE = BUILDER.comment("スライディングジャンプの基本上昇力").defineInRange("jumpVerticalBase", 0.42, 0.1, 2.0);
        SLIDE_JUMP_VERTICAL_SPEED_MULT = BUILDER.comment("スライディングジャンプの速度依存上昇ボーナス")
                .defineInRange("jumpVerticalSpeedMult", 0.25, 0.0, 2.0);
        BUILDER.pop();
        BUILDER.push("walljump");
        WALLJUMP_MAX_COUNT = BUILDER.comment("最大壁キック回数").defineInRange("maxCount", 3, 1, 20);
        WALLJUMP_VERTICAL_MULT = BUILDER.comment("壁キック時のジャンプ力倍率").defineInRange("verticalMult", 1.4, 0.5, 5.0);
        WALLJUMP_HORIZONTAL_POWER = BUILDER.comment("壁キック時の壁からの水平反発力").defineInRange("horizontalPower", 0.75, 0.1, 3.0);
        BUILDER.pop();
        BUILDER.push("slam");
        SLAM_DOWNWARD_SPEED = BUILDER.comment("スラム時の下向き速度").defineInRange("downwardSpeed", 3.0, 0.5, 20.0);
        SLAM_RADIUS = BUILDER.comment("着地時の衝撃波の半径 (ブロック)").defineInRange("radius", 4.0, 1.0, 20.0);
        SLAM_KNOCKUP_POWER = BUILDER.comment("衝撃波のノックアップ力").defineInRange("knockupPower", 0.8, 0.1, 5.0);
        BUILDER.pop();
        BUILDER.push("punch");
        PUNCH_DAMAGE_BASE = BUILDER.comment("フィードバッカー/ナックルブラスターの基本追加ダメージ").defineInRange("damageBase", 4.0, 0.0, 100.0);
        PUNCH_ENERGY_REGEN_PER_TICK = BUILDER.comment("パンチゲージの1Tickごとの回復量").defineInRange("energyRegenPerTick", 0.05, 0.001, 1.0);
        PARRY_INVUL_SECONDS = BUILDER.comment("パリィ成功後の無敵時間 (秒)").defineInRange("parryInvulSeconds", 0.6, 0.05, 100.0);
        PARRY_COUNTER_DAMAGE = BUILDER.comment("近接攻撃パリィ時のカウンターダメージ量").defineInRange("parryCounterDamage", 12.0, 0.0, 100.0);
        BUILDER.pop();
        BUILDER.push("coin");
        COIN_MAX_COUNT = BUILDER.comment("コインの最大所持数").defineInRange("maxCount", 4, 1, 20);
        COIN_REGEN_PER_TICK = BUILDER.comment("コインの1Tickごとの回復量").defineInRange("regenPerTick", 0.016, 0.001, 1.0);
        COIN_BASE_DAMAGE = BUILDER.comment("コインヒット時の基本ダメージ").defineInRange("baseDamage", 5.0, 0.0, 100.0);
        COIN_PARRY_DAMAGE_PER_COUNT = BUILDER.comment("コインをパリィするたびに増えるダメージボーナス").defineInRange("parryDamagePerCount", 2.0, 0.0, 50.0);
        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}
