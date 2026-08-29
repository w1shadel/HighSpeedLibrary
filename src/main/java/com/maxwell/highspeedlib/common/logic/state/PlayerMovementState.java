package com.maxwell.highspeedlib.common.logic.state;

import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public class PlayerMovementState {
    public int dashInvulTicks = 0;
    public double stamina = 0;
    public int airTicks = 0;
    @Nullable
    public Vec3 slideDir = null;
    public int slamBuffer = 0;
    public int wallJumpCount = 0;
    public boolean isSliding = false;
    public boolean isSlamming = false;
    public boolean fallImmunity = false;
    public boolean slamStorageActive = false;
    public int slamStorageTimer = 0;
    public int slamImpactTimer = 0;
    public float slamXInput = 0f;
    public float slamZInput = 0f;
}
