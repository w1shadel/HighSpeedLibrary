package com.maxwell.highspeedlib.common.logic.state;

import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerStateManager {
    private static final Map<UUID, PlayerLogicState> STATES = new HashMap<>();

    public static PlayerLogicState getState(UUID uuid) {
        return STATES.computeIfAbsent(uuid, k -> new PlayerLogicState());
    }

    public static PlayerLogicState getState(Player player) {
        return getState(player.getUUID());
    }

    // プレイヤーが退出した際などにメモリリークを防ぐためのメソッド
    public static void removeState(UUID uuid) {
        STATES.remove(uuid);
    }
}
