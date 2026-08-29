package com.maxwell.highspeedlib.api.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class HighSpeedServerConfig {
    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.BooleanValue ABILITY_PUNCH;
    public static final ModConfigSpec.BooleanValue ABILITY_DASH;
    public static final ModConfigSpec.BooleanValue ABILITY_WHIPLASH;
    public static final ModConfigSpec.BooleanValue ABILITY_WALLJUMP;
    public static final ModConfigSpec.BooleanValue ABILITY_SLIDE;
    public static final ModConfigSpec.BooleanValue ABILITY_SLAM;

    public static final ModConfigSpec.IntValue DASH_INVUL_TICKS;
    public static final ModConfigSpec.IntValue DASH_MAX_COUNT;
    public static final ModConfigSpec.DoubleValue DASH_STAMINA_COST;

    public static final ModConfigSpec.IntValue STAMINA_MAX;
    public static final ModConfigSpec.DoubleValue STAMINA_REGEN_PER_TICK;
    public static final ModConfigSpec.DoubleValue STAMINA_BOOST_ENCHANT_VALUE;

    public static final ModConfigSpec.DoubleValue SLIDE_SPEED;
    public static final ModConfigSpec.IntValue SLIDE_AIR_TIMEOUT_TICKS;
    public static final ModConfigSpec.DoubleValue SLIDE_JUMP_HORIZONTAL_MULT;
    public static final ModConfigSpec.DoubleValue SLIDE_JUMP_VERTICAL_BASE;
    public static final ModConfigSpec.DoubleValue SLIDE_JUMP_VERTICAL_SPEED_MULT;

    public static final ModConfigSpec.IntValue WALLJUMP_MAX_COUNT;
    public static final ModConfigSpec.DoubleValue WALLJUMP_VERTICAL_MULT;
    public static final ModConfigSpec.DoubleValue WALLJUMP_HORIZONTAL_POWER;

    public static final ModConfigSpec.DoubleValue SLAM_DOWNWARD_SPEED;
    public static final ModConfigSpec.DoubleValue SLAM_RADIUS;
    public static final ModConfigSpec.DoubleValue SLAM_KNOCKUP_POWER;
    public static final ModConfigSpec.DoubleValue SLAM_DAMAGE_ATTACK_FACTOR;
    public static final ModConfigSpec.DoubleValue SLAM_ENCHANT_FACTOR;
    public static final ModConfigSpec.DoubleValue SLAM_JUMP_VERTICAL_POWER;
    public static final ModConfigSpec.DoubleValue SLAM_JUMP_HORIZONTAL_MULT;

    public static final ModConfigSpec.DoubleValue PUNCH_DAMAGE_BASE;
    public static final ModConfigSpec.DoubleValue PUNCH_ENERGY_REGEN_PER_TICK;
    public static final ModConfigSpec.DoubleValue PUNCH_AD_FACTOR;
    public static final ModConfigSpec.DoubleValue PUNCH_VELOCITY_FACTOR;
    public static final ModConfigSpec.DoubleValue PUNCH_VELOCITY_MAX_MODIFIER;
    public static final ModConfigSpec.DoubleValue PUNCH_FEEDBACKER_DAMAGE_MULT;
    public static final ModConfigSpec.DoubleValue PUNCH_KNUCKLE_DAMAGE_MULT;
    public static final ModConfigSpec.DoubleValue PUNCH_KNUCKLE_RADIUS;

    public static final ModConfigSpec.DoubleValue PARRY_INVUL_SECONDS;
    public static final ModConfigSpec.DoubleValue PARRY_COUNTER_DAMAGE;
    public static final ModConfigSpec.IntValue PARRY_HITSTOP_TICKS;
    public static final ModConfigSpec.DoubleValue PARRY_SCREEN_SHAKE_POWER;
    public static final ModConfigSpec.IntValue PARRY_SCREEN_SHAKE_TICKS;
    public static final ModConfigSpec.DoubleValue PARRY_EXPLOSION_SIZE;

    public static final ModConfigSpec.DoubleValue WHIPLASH_MAX_RANGE;
    public static final ModConfigSpec.DoubleValue WHIPLASH_FLY_SPEED;
    public static final ModConfigSpec.DoubleValue WHIPLASH_PULL_SPEED;
    public static final ModConfigSpec.IntValue WHIPLASH_OBSTRUCTION_MAX_TICKS;
    public static final ModConfigSpec.DoubleValue WHIPLASH_PULL_PLAYER_MAX_SPEED;
    public static final ModConfigSpec.DoubleValue WHIPLASH_PULL_TARGET_MAX_SPEED;

    public static final ModConfigSpec.IntValue COIN_MAX_COUNT;
    public static final ModConfigSpec.DoubleValue COIN_REGEN_PER_TICK;
    public static final ModConfigSpec.DoubleValue COIN_BASE_DAMAGE;
    public static final ModConfigSpec.DoubleValue COIN_PARRY_DAMAGE_PER_COUNT;

    public static final ModConfigSpec.DoubleValue PROJECTILE_PARRY_SPEED;

    static {
        BUILDER.comment("HighSpeedLib Server Configuration");

        BUILDER.push("abilities");
        ABILITY_PUNCH = BUILDER.comment("Default enabled state for Punch ability").define("punch", true);
        ABILITY_DASH = BUILDER.comment("Default enabled state for Dash ability").define("dash", true);
        ABILITY_WHIPLASH = BUILDER.comment("Default enabled state for Whiplash ability").define("whiplash", true);
        ABILITY_WALLJUMP = BUILDER.comment("Default enabled state for Wall Jump ability").define("walljump", true);
        ABILITY_SLIDE = BUILDER.comment("Default enabled state for Sliding ability").define("slide", true);
        ABILITY_SLAM = BUILDER.comment("Default enabled state for Slam ability").define("slam", true);
        BUILDER.pop();

        BUILDER.push("dash");
        DASH_INVUL_TICKS = BUILDER.comment("Number of invulnerability ticks during dash").defineInRange("invulTicks", 6, 0, 200);
        DASH_MAX_COUNT = BUILDER.comment("Maximum number of dashes (= Max stamina segments)").defineInRange("maxCount", 3, 1, 20);
        DASH_STAMINA_COST = BUILDER.comment("Stamina cost per dash").defineInRange("staminaCost", 1.0, 0.0, 10.0);
        BUILDER.pop();

        BUILDER.push("stamina");
        STAMINA_MAX = BUILDER.comment("Base maximum stamina segments (without enchantment)").defineInRange("maxBase", 3, 1, 20);
        STAMINA_REGEN_PER_TICK = BUILDER.comment("Stamina regeneration amount per tick").defineInRange("regenPerTick", 0.04, 0.0, 1.0);
        STAMINA_BOOST_ENCHANT_VALUE = BUILDER.comment("Stamina increase per level of Stamina Boost enchantment").defineInRange("enchantValue", 1.0, 0.0, 10.0);
        BUILDER.pop();

        BUILDER.push("slide");
        SLIDE_SPEED = BUILDER.comment("Horizontal speed during sliding").defineInRange("speed", 0.75, 0.1, 5.0);
        SLIDE_AIR_TIMEOUT_TICKS = BUILDER.comment("Ticks before slide is canceled after leaving ground").defineInRange("airTimeoutTicks", 20, 1, 200);
        SLIDE_JUMP_HORIZONTAL_MULT = BUILDER.comment("Horizontal speed multiplier for jump during slide").defineInRange("jumpHorizontalMult", 1.8, 1.0, 5.0);
        SLIDE_JUMP_VERTICAL_BASE = BUILDER.comment("Base vertical power for slide jump").defineInRange("jumpVerticalBase", 0.42, 0.1, 2.0);
        SLIDE_JUMP_VERTICAL_SPEED_MULT = BUILDER.comment("Speed-dependent vertical bonus for slide jump").defineInRange("jumpVerticalSpeedMult", 0.25, 0.0, 2.0);
        BUILDER.pop();

        BUILDER.push("walljump");
        WALLJUMP_MAX_COUNT = BUILDER.comment("Maximum number of wall jumps").defineInRange("maxCount", 3, 1, 20);
        WALLJUMP_VERTICAL_MULT = BUILDER.comment("Vertical power multiplier for wall jump").defineInRange("verticalMult", 1.1, 0.5, 5.0);
        WALLJUMP_HORIZONTAL_POWER = BUILDER.comment("Horizontal repulsion power from wall during jump").defineInRange("horizontalPower", 0.5, 0.1, 3.0);
        SLAM_JUMP_VERTICAL_POWER = BUILDER.comment("Vertical power of a slam storage jump (default is 6.2)")
                .defineInRange("jumpVerticalPower", 6.2, 0.5, 20.0);
        SLAM_JUMP_HORIZONTAL_MULT = BUILDER.comment("Horizontal speed multiplier for jumping out of a slam (1.0 preserves normal speed, 0.0 makes it go only straight up)")
                .defineInRange("jumpHorizontalMult", 1.0, 0.0, 5.0);
        BUILDER.pop();

        BUILDER.push("slam");
        SLAM_DOWNWARD_SPEED = BUILDER.comment("Downward speed during slam").defineInRange("downwardSpeed", 3.0, 0.5, 20.0);
        SLAM_RADIUS = BUILDER.comment("Shockwave radius on impact (blocks)").defineInRange("radius", 4.0, 1.0, 20.0);
        SLAM_KNOCKUP_POWER = BUILDER.comment("Knock-up power of the shockwave").defineInRange("knockupPower", 0.8, 0.1, 5.0);
        SLAM_DAMAGE_ATTACK_FACTOR = BUILDER.comment("Scaling factor for slam damage based on attack damage").defineInRange("attackFactor", 0.5, 0.0, 10.0);
        SLAM_ENCHANT_FACTOR = BUILDER.comment("Damage increase per level of Feather Falling").defineInRange("enchantFactor", 0.1, 0.0, 1.0);
        BUILDER.pop();

        BUILDER.push("punch");
        PUNCH_DAMAGE_BASE = BUILDER.comment("Base additional damage for Feedbacker/Knuckleblaster").defineInRange("damageBase", 4.0, 0.0, 100.0);
        PUNCH_ENERGY_REGEN_PER_TICK = BUILDER.comment("Punch energy regeneration per tick").defineInRange("energyRegenPerTick", 0.05, 0.001, 1.0);
        PUNCH_AD_FACTOR = BUILDER.comment("Scaling factor for punch damage based on attack damage (1.0 + (AD-1) * Factor)").defineInRange("adFactor", 0.2, 0.0, 1.0);
        PUNCH_VELOCITY_FACTOR = BUILDER.comment("Scaling factor for punch damage based on velocity (1.0 + Speed * Factor)").defineInRange("velocityFactor", 0.5, 0.0, 2.0);
        PUNCH_VELOCITY_MAX_MODIFIER = BUILDER.comment("Maximum damage multiplier from velocity").defineInRange("velocityMaxModifier", 1.4, 1.0, 5.0);
        PUNCH_FEEDBACKER_DAMAGE_MULT = BUILDER.comment("Damage multiplier for Feedbacker punch").defineInRange("feedbackerMult", 0.4, 0.0, 10.0);
        PUNCH_KNUCKLE_DAMAGE_MULT = BUILDER.comment("Damage multiplier for Knuckleblaster punch").defineInRange("knuckleMult", 1.5, 0.0, 10.0);
        PUNCH_KNUCKLE_RADIUS = BUILDER.comment("Explosion radius for Knuckleblaster").defineInRange("knuckleRadius", 2.5, 0.5, 10.0);
        BUILDER.pop();

        BUILDER.push("parry");
        PARRY_INVUL_SECONDS = BUILDER.comment("Invulnerability duration after a successful parry (seconds)").defineInRange("parryInvulSeconds", 0.6, 0.05, 100.0);
        PARRY_COUNTER_DAMAGE = BUILDER.comment("Counter damage dealt on successful melee parry").defineInRange("parryCounterDamage", 12.0, 0.0, 100.0);
        PARRY_HITSTOP_TICKS = BUILDER.comment("Hitstop duration on successful parry (ticks)").defineInRange("hitstopTicks", 5, 0, 100);
        PARRY_SCREEN_SHAKE_POWER = BUILDER.comment("Screen shake intensity on successful parry").defineInRange("screenShakePower", 2.0, 0.0, 10.0);
        PARRY_SCREEN_SHAKE_TICKS = BUILDER.comment("Screen shake duration on successful parry (ticks)").defineInRange("screenShakeTicks", 5, 0, 100);
        PARRY_EXPLOSION_SIZE = BUILDER.comment("Explosion size for projectile parries").defineInRange("explosionSize", 3.0, 0.0, 20.0);
        BUILDER.pop();

        BUILDER.push("whiplash");
        WHIPLASH_MAX_RANGE = BUILDER.comment("Maximum range of the Whiplash").defineInRange("maxRange", 70.0, 5.0, 200.0);
        WHIPLASH_FLY_SPEED = BUILDER.comment("Flight speed of the Whiplash hook (per tick)").defineInRange("flySpeed", 3.0, 0.5, 10.0);
        WHIPLASH_PULL_SPEED = BUILDER.comment("Retraction speed of the Whiplash hook (per tick)").defineInRange("pullSpeed", 2.0, 0.5, 10.0);
        WHIPLASH_OBSTRUCTION_MAX_TICKS = BUILDER.comment("Ticks before Whiplash is canceled by obstruction").defineInRange("obstructionMaxTicks", 10, 1, 100);
        WHIPLASH_PULL_PLAYER_MAX_SPEED = BUILDER.comment("Maximum speed when player is being pulled").defineInRange("pullPlayerMaxSpeed", 2.5, 0.1, 20.0);
        WHIPLASH_PULL_TARGET_MAX_SPEED = BUILDER.comment("Maximum speed when target is being pulled").defineInRange("pullTargetMaxSpeed", 2.8, 0.1, 20.0);
        BUILDER.pop();

        BUILDER.push("coin");
        COIN_MAX_COUNT = BUILDER.comment("Maximum number of coins").defineInRange("maxCount", 4, 1, 20);
        COIN_REGEN_PER_TICK = BUILDER.comment("Coin regeneration per tick").defineInRange("regenPerTick", 0.016, 0.001, 1.0);
        COIN_BASE_DAMAGE = BUILDER.comment("Base damage on coin hit").defineInRange("baseDamage", 5.0, 0.0, 100.0);
        COIN_PARRY_DAMAGE_PER_COUNT = BUILDER.comment("Damage bonus added per coin parry").defineInRange("parryDamagePerCount", 2.0, 0.0, 50.0);
        BUILDER.pop();

        BUILDER.push("projectile");
        PROJECTILE_PARRY_SPEED = BUILDER.comment("Speed of parried projectiles").defineInRange("parrySpeed", 3.5, 0.1, 20.0);
        BUILDER.pop();

        SPEC = BUILDER.build();
    }
}

