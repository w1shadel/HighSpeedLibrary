package com.maxwell.highspeedlib.mixin;

import com.maxwell.highspeedlib.common.logic.SlideManager;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerMixin {
    @Inject(method = "updatePlayerPose", at = @At("HEAD"), cancellable = true)
    private void highspeedlib$forceSlidePose(CallbackInfo ci) {
        Player player = (Player) (Object) this;
        if (SlideManager.isSliding(player)) {
            player.setPose(Pose.STANDING);
            ci.cancel();
        }
    }

    @Inject(method = "getDimensions", at = @At("HEAD"), cancellable = true)
    private void highspeedlib$overrideDimensions(Pose pose, CallbackInfoReturnable<EntityDimensions> cir) {
        Player player = (Player) (Object) this;
        if (SlideManager.isSliding(player)) {
            cir.setReturnValue(EntityDimensions.scalable(0.6F, 0.6F));
        }
    }

    @Inject(method = "isSwimming", at = @At("HEAD"), cancellable = true)
    private void highspeedlib$overrideIsSwimming(CallbackInfoReturnable<Boolean> cir) {
        Player player = (Player) (Object) this;
        if (SlideManager.isSliding(player)) {
            cir.setReturnValue(false);
        }
    }
}

