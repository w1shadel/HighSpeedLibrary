package com.maxwell.highspeedlib.client.state;

import com.maxwell.highspeedlib.client.effect.SpeedTrailManager;
import com.maxwell.highspeedlib.common.logic.combat.ArmType;
import com.maxwell.highspeedlib.common.logic.combat.ServerWhiplashManager;

import java.util.HashMap;
import java.util.Map;

public class ClientPlayerState {
    public final Map<String, SpeedTrailManager.TrailInstance> trailInstances = new HashMap<>();
    public ArmType currentArm = null;
    public ServerWhiplashManager.HookData whiplashHookData = new ServerWhiplashManager.HookData();
    public int whiplashRenderTicks = 0;

    public ClientPlayerState() {
    }
}
