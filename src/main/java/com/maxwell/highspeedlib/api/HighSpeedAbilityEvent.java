package com.maxwell.highspeedlib.api;

import com.maxwell.highspeedlib.common.logic.combat.ArmType;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent; // ★ 追加

public abstract class HighSpeedAbilityEvent extends Event {
    private final Player player;

    public HighSpeedAbilityEvent(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    // ★ @Cancelable の代わりに implements ICancellableEvent を付与
    public static class Punch extends HighSpeedAbilityEvent implements ICancellableEvent {
        private final ArmType armType;

        public Punch(Player player, ArmType armType) {
            super(player);
            this.armType = armType;
        }

        public ArmType getArmType() {
            return armType;
        }
    }

    public static class Whiplash extends HighSpeedAbilityEvent implements ICancellableEvent {
        public Whiplash(Player player) {
            super(player);
        }
    }

    public static class CoinToss extends HighSpeedAbilityEvent implements ICancellableEvent {
        public CoinToss(Player player) {
            super(player);
        }
    }

    public static class Sliding extends HighSpeedAbilityEvent implements ICancellableEvent {
        public Sliding(Player player) {
            super(player);
        }
    }

    public static class Slam extends HighSpeedAbilityEvent implements ICancellableEvent {
        public Slam(Player player) {
            super(player);
        }
    }

    public static class Dash extends HighSpeedAbilityEvent implements ICancellableEvent {
        public Dash(Player player) {
            super(player);
        }
    }

    public static class Walljump extends HighSpeedAbilityEvent implements ICancellableEvent {
        public Walljump(Player player) {
            super(player);
        }
    }
}