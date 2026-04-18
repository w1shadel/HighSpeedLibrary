package com.maxwell.highspeedlib.common.logic.util;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class AbsoluteDamageManager {
    public static void dealAbsoluteDamage(LivingEntity target, float damage) {
        if (target.level().isClientSide()) return;
        float next = Math.max(0, target.getHealth() - damage);
        target.setHealth(next);
        if (next <= 0) {
            target.die(target.damageSources().genericKill());
            target.setRemoved(Entity.RemovalReason.KILLED);
        }
    }
}