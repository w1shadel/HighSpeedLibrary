package com.maxwell.highspeedlib.mixin;

import com.maxwell.highspeedlib.common.logic.TimeManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({
        Entity.class,
        LivingEntity.class,
        Projectile.class,
        AbstractArrow.class,
        ThrowableProjectile.class,
        ItemEntity.class,
        ExperienceOrb.class
})
public class EntityMixin {
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void highspeedlib$hitstopEntityTick(CallbackInfo ci) {
        if (!TimeManager.shouldTick()) {
            ci.cancel();
        }
    }

}
