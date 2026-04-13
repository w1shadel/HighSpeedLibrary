package com.maxwell.highspeedlib.common.logic.state;

import net.minecraft.nbt.CompoundTag;

public class PlayerLogicState {
    private final PlayerMovementState movement = new PlayerMovementState();
    private final PlayerCombatState combat = new PlayerCombatState();
    private final PlayerAbilityState ability = new PlayerAbilityState();

    public PlayerMovementState getMovement() {
        return movement;
    }

    public PlayerCombatState getCombat() {
        return combat;
    }

    public PlayerAbilityState getAbility() {
        return ability;
    }

    public CompoundTag save() {
        return ability.save();
    }

    public void load(CompoundTag nbt) {
        ability.load(nbt);
    }
}
