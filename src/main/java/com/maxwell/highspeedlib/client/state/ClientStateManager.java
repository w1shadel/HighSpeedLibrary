package com.maxwell.highspeedlib.client.state;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClientStateManager {
    private static final Map<UUID, ClientPlayerState> playerStates = new HashMap<>();
    private static final Map<Integer, ClientEntityState> entityStates = new HashMap<>();

    public static ClientPlayerState getPlayerState(UUID uuid) {
        return playerStates.computeIfAbsent(uuid, k -> new ClientPlayerState());
    }

    public static ClientPlayerState getPlayerState(Player player) {
        return getPlayerState(player.getUUID());
    }

    public static ClientEntityState getEntityState(int entityId) {
        return entityStates.computeIfAbsent(entityId, k -> new ClientEntityState());
    }

    public static ClientEntityState getEntityState(Entity entity) {
        return getEntityState(entity.getId());
    }

    public static void clear() {
        playerStates.clear();
        entityStates.clear();
    }
}
