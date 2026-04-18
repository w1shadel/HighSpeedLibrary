package com.maxwell.highspeedlib.mixin.parry;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(SynchedEntityData.class)
public interface SynchedEntityDataAccessor {
    @Accessor("entity")
    Entity getEntity();

    @Invoker("getItem")
    <T> SynchedEntityData.DataItem<T> invokeGetItem(EntityDataAccessor<T> key);

    @Accessor("isDirty")
    void setDirtyFlag(boolean dirty);
}