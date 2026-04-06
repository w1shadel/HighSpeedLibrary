package com.maxwell.highspeedlib.mixin;

import com.maxwell.highspeedlib.common.logic.SlideManager;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @ModifyVariable(method = "travel", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;handleRelativeFrictionAndCalculateMovement(Lnet/minecraft/world/phys/Vec3;F)Lnet/minecraft/world/phys/Vec3;"), ordinal = 0, argsOnly = true)
    private Vec3 highspeedlib$overrideSlideMovement(Vec3 travelVector) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity instanceof Player player && SlideManager.isSliding(player)) {
            return new Vec3(travelVector.x * 1.05D, travelVector.y, travelVector.z * 1.05D);
        }
        return travelVector;
    }

    @Inject(method = "getJumpPower", at = @At("RETURN"), cancellable = true)
    private void highspeedlib$boostSlideJump(CallbackInfoReturnable<Float> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity instanceof Player player && SlideManager.isSliding(player)) {
            Vec3 motion = player.getDeltaMovement();
            double hSpeed = Math.sqrt(motion.x * motion.x + motion.z * motion.z);
            float boostedPower = 0.42f + (float) (hSpeed * 0.25f);
            cir.setReturnValue(boostedPower);
        }
    }
}