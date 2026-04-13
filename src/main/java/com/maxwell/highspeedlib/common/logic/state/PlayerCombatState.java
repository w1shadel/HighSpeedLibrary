package com.maxwell.highspeedlib.common.logic.state;

import com.maxwell.highspeedlib.common.logic.combat.ServerWhiplashManager;

public class PlayerCombatState {
    public double punchEnergy = 0;
    public int activeParryWindow = 0;
    public double coinStocks = 0;
    public ServerWhiplashManager.HookData hookData = null;
}
