package com.maxwell.highspeedlib.mixin.parry;

import com.maxwell.highspeedlib.common.logic.ServerArmManager;
import com.maxwell.highspeedlib.mixin.accessor.LivingEntityAccessor;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SynchedEntityData.class)
public abstract class DataWatcherSafetyMixin {

    @Shadow @Final private Entity entity;

    @Inject(method = "set", at = @At("HEAD"), cancellable = true)
    private <T> void blockHealthUpdate(EntityDataAccessor<T> key, T value, CallbackInfo ci) {
        if (this.entity instanceof ServerPlayer player && ServerArmManager.isPlayerParrying(player)) {

            if (isHealthOrShieldAccessor(key)) {
                if (value instanceof Float newValue) {
                    float currentValue = player.getHealth();
                    if (newValue < currentValue) {
                        ci.cancel();
                    }
                }
            }
        }
    }

    @Unique
    private boolean isHealthOrShieldAccessor(EntityDataAccessor<?> key) {
        // LivingEntityが持つ本物のアクセッサーと比較
        // IDの数値を見ないので、どんな環境でも100%正確です
        return key.equals(LivingEntityAccessor.getHealthDataId());
    }
}