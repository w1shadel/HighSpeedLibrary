package com.maxwell.highspeedlib.mixin;

import com.maxwell.highspeedlib.common.logic.TimeManager;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

@Mixin(ServerLevel.class)
public class ServerLevelMixin {
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void highspeedlib$onTick(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        if (!TimeManager.shouldTick()) {
            ci.cancel();
        }
    }
}