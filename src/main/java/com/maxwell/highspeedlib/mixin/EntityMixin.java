package com.maxwell.highspeedlib.mixin;

import com.maxwell.highspeedlib.api.main.mob.MobModeManager;
import com.maxwell.highspeedlib.common.logic.TimeManager;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityMixin {
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void highspeedlib$hitstopEntityTick(CallbackInfo ci) {
        if (!TimeManager.shouldTick()) {
            ci.cancel();
        }
    }

    @Inject(method = "isCurrentlyGlowing", at = @At("RETURN"), cancellable = true)
    private void highspeedlib$forceEnragedGlow(CallbackInfoReturnable<Boolean> cir) {
        Entity self = (Entity) (Object) this;
        if (self instanceof LivingEntity living) {
            boolean enraged = MobModeManager.isEnraged(living);
            boolean radiant = MobModeManager.getRadianceTier(living) > 0;
            if (enraged || radiant) {
                cir.setReturnValue(true);
            } else if (MobModeManager.isBoss(living)) {
                cir.setReturnValue(false);
            }
        }
    }

    @Inject(method = "getTeamColor", at = @At("HEAD"), cancellable = true)
    private void highspeedlib$setEnragedGlowColor(CallbackInfoReturnable<Integer> cir) {
        Entity self = (Entity) (Object) this;
        if (self.level().isClientSide && self instanceof LivingEntity living) {
            int tier = MobModeManager.getRadianceTier(living);
            if (tier > 0) {
                float hue = (living.tickCount + Minecraft.getInstance().getPartialTick()) * 0.05f;
                cir.setReturnValue(Mth.hsvToRgb(hue % 1.0f, 0.7f, 1.0f));
            } else if (MobModeManager.isEnraged(living)) {
                cir.setReturnValue(0xFF0000);
            }
        }
    }
}