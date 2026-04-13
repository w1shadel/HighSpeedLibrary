package com.maxwell.highspeedlib.client.state;

import com.maxwell.highspeedlib.common.logic.combat.ArmType;
import com.maxwell.highspeedlib.common.logic.combat.ServerWhiplashManager;
import com.maxwell.highspeedlib.client.effect.SpeedTrailManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClientPlayerState {
    public ArmType currentArm = null;
    public ServerWhiplashManager.HookData whiplashHookData = new ServerWhiplashManager.HookData();
    public int whiplashRenderTicks = 0;
    public final Map<String, SpeedTrailManager.TrailInstance> trailInstances = new HashMap<>();

    public ClientPlayerState() {
    }
}
