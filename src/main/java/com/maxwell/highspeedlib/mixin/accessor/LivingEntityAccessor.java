package com.maxwell.highspeedlib.mixin.accessor;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LivingEntity.class)
public interface LivingEntityAccessor {
    // 1.20.1におけるDATA_HEALTH_IDフィールド（難読化名であれば自動で解決されます）
    @Accessor("DATA_HEALTH_ID")
    static EntityDataAccessor<Float> getHealthDataId() {
        throw new AssertionError();
    }
}