package com.maxwell.highspeedlib.mixin.parry;

import com.maxwell.highspeedlib.common.logic.ServerArmManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.function.Predicate;
@Mixin(Level.class)
public abstract class UltimateGhostMixin {
    @Inject(method = "getEntities", at = @At("RETURN"), cancellable = true)
    private void hideFromBoss(Entity getter, AABB area, Predicate<? super Entity> predicate, CallbackInfoReturnable<List<Entity>> cir) {
        // 全てのエンティティ検索において、パリィ中のプレイヤーをリストから抹消する
        List<Entity> list = cir.getReturnValue();
        if (list != null && !list.isEmpty()) {
            list.removeIf(e -> e instanceof ServerPlayer player && ServerArmManager.isPlayerParrying(player));
        }
    }
}