package com.maxwell.highspeedlib.mixin.parry;

import com.maxwell.highspeedlib.common.logic.ServerArmManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityGeometricGhostMixin {
    @Inject(method = "getBoundingBox", at = @At("HEAD"), cancellable = true)
    private void returnPhantomBox(CallbackInfoReturnable<AABB> cir) {
        if ((Object) this instanceof ServerPlayer player && ServerArmManager.isPlayerParrying(player)) {
            double farAway = 1.0E30;
            cir.setReturnValue(new AABB(farAway, farAway, farAway, farAway, farAway, farAway));
        }
    }

    @Inject(method = "isRemoved", at = @At("HEAD"), cancellable = true)
    private void fakeRemovalDuringParry(CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof ServerPlayer player && ServerArmManager.isPlayerParrying(player)) {
            cir.setReturnValue(true);
        }
    }
}