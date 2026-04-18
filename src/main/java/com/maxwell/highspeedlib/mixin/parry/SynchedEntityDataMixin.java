package com.maxwell.highspeedlib.mixin.parry;

import com.maxwell.highspeedlib.common.logic.combat.ServerArmManager;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SynchedEntityData.class)
public abstract class SynchedEntityDataMixin {
    @Inject(method = "set(Lnet/minecraft/network/syncher/EntityDataAccessor;Ljava/lang/Object;)V", at = @At("HEAD"), cancellable = true)
    private <T> void absolute$lockHealth(EntityDataAccessor<T> accessor, T value, CallbackInfo ci) {
        SynchedEntityDataAccessor acc = (SynchedEntityDataAccessor) this;
        Entity entity = acc.getEntity();
        if (!(entity instanceof LivingEntity living)) return;
        if (ServerArmManager.isPlayerParrying(living)) {
            ci.cancel();
        }
    }
}