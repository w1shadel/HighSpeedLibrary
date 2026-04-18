package com.maxwell.highspeedlib.mixin.parry;

import net.minecraft.network.syncher.SynchedEntityData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SynchedEntityData.DataItem.class)
public interface SynchedEntityDataItemAccessor {
    @Accessor("value")
    void setValue(Object value);
}