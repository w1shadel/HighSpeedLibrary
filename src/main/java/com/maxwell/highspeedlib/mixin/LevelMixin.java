package com.maxwell.highspeedlib.mixin;

import com.maxwell.highspeedlib.common.logic.TimeManager;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Level.class)
public class LevelMixin {
    @Inject(method = "tickBlockEntities", at = @At("HEAD"), cancellable = true)
    private void highspeedlib$onTickBlockEntities(CallbackInfo ci) {
        if (!TimeManager.shouldTick()) {
            ci.cancel();
        }
    }
}