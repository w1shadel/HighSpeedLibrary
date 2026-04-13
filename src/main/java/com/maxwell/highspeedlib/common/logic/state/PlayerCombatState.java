package com.maxwell.highspeedlib.common.logic.state;

// Hooksのデータなどは、後でロジッククラスに移動・統合する際に調整します。
// 現時点ではObjectとして保持するか、ServerWhiplashManager.HookDataのパブリック化を行います。
import com.maxwell.highspeedlib.common.logic.combat.ServerWhiplashManager;

public class PlayerCombatState {
    public double punchEnergy = 0;
    public int activeParryWindow = 0;
    public double coinStocks = 0;
    
    // HookDataは一旦旧クラスのものを参照
    public ServerWhiplashManager.HookData hookData = null; 
}
