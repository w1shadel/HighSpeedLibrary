package com.maxwell.highspeedlib.mixin.parry;

import com.maxwell.highspeedlib.common.logic.ServerArmManager;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SynchedEntityData.class)
public abstract class DataInvariantsMixin {
    @Shadow
    private boolean isDirty;
    @Shadow
    @Final
    private Entity entity;

    @Inject(method = "set", at = @At("TAIL"))
    private <T> void forceCleanState(EntityDataAccessor<T> key, T value, CallbackInfo ci) {
        if (this.entity instanceof ServerPlayer player && ServerArmManager.isPlayerParrying(player)) {
            this.isDirty = false;
        }
    }
}