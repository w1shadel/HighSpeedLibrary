package com.maxwell.highspeedlib.common.logic.movement;

import com.maxwell.highspeedlib.common.logic.state.PlayerMovementState;
import com.maxwell.highspeedlib.common.logic.state.PlayerStateManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class DashManager {
    public static void startDashInvulnerability(ServerPlayer player, int ticks) {
        PlayerMovementState state = PlayerStateManager.getState(player).getMovement();
        state.dashInvulTicks = ticks;
    }

    public static boolean hasDashInvulnerability(Player player) {
        PlayerMovementState state = PlayerStateManager.getState(player).getMovement();
        return state.dashInvulTicks > 0;
    }

    public static void tick(Player player, PlayerMovementState state) {
        if (state.dashInvulTicks > 0) {
            state.dashInvulTicks--;
        }
    }
}
