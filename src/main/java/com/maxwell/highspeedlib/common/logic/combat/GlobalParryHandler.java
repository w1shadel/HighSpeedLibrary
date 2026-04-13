package com.maxwell.highspeedlib.common.logic.combat;

import com.maxwell.highspeedlib.HighSpeedLib;
import com.maxwell.highspeedlib.api.config.HighSpeedServerConfig;
import com.maxwell.highspeedlib.api.main.IParryable;
import com.maxwell.highspeedlib.common.entity.ThrownCoinEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@SuppressWarnings("removal")
@Mod.EventBusSubscriber(modid = HighSpeedLib.MODID)
public class GlobalParryHandler {
    @SubscribeEvent
    public static void onImpact(ProjectileImpactEvent event) {
        Projectile p = event.getProjectile();
        if (p.level().isClientSide) return;
        if (event.getRayTraceResult() instanceof EntityHitResult eHit && eHit.getEntity() instanceof ServerPlayer player) {
            if (ServerArmManager.isPlayerParrying(player)) {
                if (p instanceof IParryable parryable && !parryable.canBeParried(player)) {
                    return;
                }
                if (!(p instanceof ThrownCoinEntity)) {
                    ServerArmManager.performProjectileParry(p, player);
                    if (p instanceof IParryable parryable) parryable.onParried(player);
                    ServerArmManager.triggerParryEffects(player);
                    event.setCanceled(true);
                    return;
                }
            }
        }
        if (p.getPersistentData().getBoolean("hs_explosive")) {
            Vec3 pos = event.getRayTraceResult().getLocation();
            p.level().explode(p.getOwner(), pos.x, pos.y, pos.z, 3.0f, Level.ExplosionInteraction.NONE);
            p.getPersistentData().remove("hs_explosive");
        }
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingAttackEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (ServerArmManager.isPlayerParrying(player)) {
            Entity attacker = event.getSource().getDirectEntity();
            if (attacker instanceof LivingEntity livingAttacker && attacker != player) {
                if (attacker instanceof IParryable parryable && !parryable.canBeParried(player)) {
                    return;
                }
                event.setCanceled(true);
                float counterDamage = HighSpeedServerConfig.PARRY_COUNTER_DAMAGE.get().floatValue();
                livingAttacker.hurt(player.damageSources().mobAttack(player), counterDamage);
                livingAttacker.hurtMarked = true;
                ServerArmManager.triggerParryEffects(player);
                if (attacker instanceof IParryable parryable) parryable.onParried(player);
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                        net.minecraft.sounds.SoundEvents.ANVIL_PLACE, net.minecraft.sounds.SoundSource.PLAYERS, 0.5f, 4.0f);
            }
        }
    }
}
