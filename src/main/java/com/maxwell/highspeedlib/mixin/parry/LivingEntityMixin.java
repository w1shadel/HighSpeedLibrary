package com.maxwell.highspeedlib.mixin.parry;

import com.maxwell.highspeedlib.common.logic.combat.ServerArmManager;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Inject(method = "setHealth", at = @At("HEAD"), cancellable = true)
    private void highspeedlib$cancelSetHealth(float value, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (self instanceof Player player && ServerArmManager.isPlayerParrying(player)) {
            if (value < player.getHealth()) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
    private void highspeedlib$cancelHurt(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (self instanceof Player player && ServerArmManager.isPlayerParrying(player)) {
            cir.setReturnValue(false);
        }
    }

}