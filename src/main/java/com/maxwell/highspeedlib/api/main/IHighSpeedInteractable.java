package com.maxwell.highspeedlib.api.main;

import net.minecraft.server.level.ServerPlayer;

public interface IHighSpeedInteractable {
    default boolean onHandPunch(ServerPlayer player, boolean isRedArm) {
        return false;
    }

    default WhiplashReaction onWhiplash(ServerPlayer player) {
        return WhiplashReaction.DEFAULT;
    }

    enum WhiplashReaction {
        DEFAULT,
        PULL_PLAYER,
        PULL_TARGET,
        IGNORE
    }
}