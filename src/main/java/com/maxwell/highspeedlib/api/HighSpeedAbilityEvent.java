package com.maxwell.highspeedlib.api;

import com.maxwell.highspeedlib.common.logic.combat.ArmType;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

public class HighSpeedAbilityEvent extends Event {
    private final Player player;

    public HighSpeedAbilityEvent(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    @Cancelable
    public static class Punch extends HighSpeedAbilityEvent {
        private final ArmType armType;

        public Punch(Player player, ArmType armType) {
            super(player);
            this.armType = armType;
        }

        public ArmType getArmType() {
            return armType;
        }
    }

    @Cancelable
    public static class Whiplash extends HighSpeedAbilityEvent {
        public Whiplash(Player player) {
            super(player);
        }
    }

    @Cancelable
    public static class CoinToss extends HighSpeedAbilityEvent {
        public CoinToss(Player player) {
            super(player);
        }
    }

    @Cancelable
    public static class Sliding extends HighSpeedAbilityEvent {
        public Sliding(Player player) {
            super(player);
        }
    }

    @Cancelable
    public static class Slam extends HighSpeedAbilityEvent {
        public Slam(Player player) {
            super(player);
        }
    }

    @Cancelable
    public static class Dash extends HighSpeedAbilityEvent {
        public Dash(Player player) {
            super(player);
        }
    }

    @Cancelable
    public static class Walljump extends HighSpeedAbilityEvent {
        public Walljump(Player player) {
            super(player);
        }
    }
}