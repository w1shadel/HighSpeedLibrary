package com.maxwell.highspeedlib.api.main;

import net.minecraft.world.entity.player.Player;

public interface IParryable {
    default boolean canBeParried(Player player) {
        return true;
    }

    default void onParried(Player player) {
    }
}