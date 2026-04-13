package com.maxwell.highspeedlib.common.logic.state;

import com.maxwell.highspeedlib.api.config.HighSpeedServerConfig;
import net.minecraft.nbt.CompoundTag;

public class PlayerAbilityState {
    public boolean punch;
    public boolean whiplash;
    public boolean dash;
    public boolean wallJump;
    public boolean sliding;
    public boolean slam;
    public double punchDamageBase;
    public int maxDashCount;
    public int maxWallJumpCount;
    public int maxCoinCount;
    public int parry_invtime;

    public PlayerAbilityState() {
        punch = HighSpeedServerConfig.ABILITY_PUNCH.get();
        whiplash = HighSpeedServerConfig.ABILITY_WHIPLASH.get();
        dash = HighSpeedServerConfig.ABILITY_DASH.get();
        wallJump = HighSpeedServerConfig.ABILITY_WALLJUMP.get();
        sliding = HighSpeedServerConfig.ABILITY_SLIDE.get();
        slam = HighSpeedServerConfig.ABILITY_SLAM.get();
        punchDamageBase = HighSpeedServerConfig.PUNCH_DAMAGE_BASE.get();
        maxDashCount = HighSpeedServerConfig.DASH_MAX_COUNT.get();
        maxWallJumpCount = HighSpeedServerConfig.WALLJUMP_MAX_COUNT.get();
        maxCoinCount = HighSpeedServerConfig.COIN_MAX_COUNT.get();
        parry_invtime = (int) Math.round(HighSpeedServerConfig.PARRY_INVUL_SECONDS.get() * 20.0);
    }

    public CompoundTag save() {
        CompoundTag nbt = new CompoundTag();
        nbt.putBoolean("punch", punch);
        nbt.putBoolean("whiplash", whiplash);
        nbt.putBoolean("dash", dash);
        nbt.putBoolean("wallJump", wallJump);
        nbt.putBoolean("sliding", sliding);
        nbt.putBoolean("slam", slam);
        nbt.putDouble("punchDamageBase", punchDamageBase);
        nbt.putInt("maxDashCount", maxDashCount);
        nbt.putInt("maxWallJumpCount", maxWallJumpCount);
        nbt.putInt("maxCoinCount", maxCoinCount);
        nbt.putInt("parry_invtime", parry_invtime);
        return nbt;
    }

    public void load(CompoundTag nbt) {
        if (nbt.contains("punch")) punch = nbt.getBoolean("punch");
        if (nbt.contains("whiplash")) whiplash = nbt.getBoolean("whiplash");
        if (nbt.contains("dash")) dash = nbt.getBoolean("dash");
        if (nbt.contains("wallJump")) wallJump = nbt.getBoolean("wallJump");
        if (nbt.contains("sliding")) sliding = nbt.getBoolean("sliding");
        if (nbt.contains("slam")) slam = nbt.getBoolean("slam");
        if (nbt.contains("punchDamageBase")) punchDamageBase = nbt.getDouble("punchDamageBase");
        if (nbt.contains("maxDashCount")) maxDashCount = nbt.getInt("maxDashCount");
        if (nbt.contains("maxWallJumpCount")) maxWallJumpCount = nbt.getInt("maxWallJumpCount");
        if (nbt.contains("maxCoinCount")) maxCoinCount = nbt.getInt("maxCoinCount");
        if (nbt.contains("parry_invtime")) parry_invtime = nbt.getInt("parry_invtime");
    }
}
