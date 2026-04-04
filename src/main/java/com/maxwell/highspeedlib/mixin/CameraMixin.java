package com.maxwell.highspeedlib.mixin;

import com.maxwell.highspeedlib.client.ClientSlideHandler;
import com.maxwell.highspeedlib.logic.SlideManager;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {
    @Shadow private Vec3 position;

    @Inject(method = "setup", at = @At("TAIL"))
    private void highspeedlib$adjustSlideCameraHeight(BlockGetter level, Entity entity, boolean detached, boolean mirrored, float partialTick, CallbackInfo ci) {
        if (!detached && entity instanceof Player player && SlideManager.isSliding(player)) {
            float slideProgress = ClientSlideHandler.getSlideProgress();
            double yOffset = 1.1D * slideProgress;

            this.position = new Vec3(this.position.x, this.position.y - yOffset, this.position.z);
        }
    }
}