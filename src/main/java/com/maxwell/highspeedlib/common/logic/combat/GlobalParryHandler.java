package com.maxwell.highspeedlib.common.logic.combat;

import com.maxwell.highspeedlib.HighSpeedLib;
import com.maxwell.highspeedlib.api.config.HighSpeedServerConfig;
import com.maxwell.highspeedlib.api.main.IParryable;
import com.maxwell.highspeedlib.common.entity.ThrownCoinEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

@EventBusSubscriber(modid = HighSpeedLib.MODID)
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
            float explosionSize = HighSpeedServerConfig.PARRY_EXPLOSION_SIZE.get().floatValue();
            p.level().explode(p.getOwner(), pos.x, pos.y, pos.z, explosionSize, Level.ExplosionInteraction.NONE);
            p.getPersistentData().remove("hs_explosive");
        }
    }

    
    @SubscribeEvent
    public static void onLivingDamage(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        if (ServerArmManager.isPlayerParrying(player)) {
            var damageSource = event.getSource();
            Entity attacker = damageSource.getDirectEntity();
            if (attacker == null) {
                attacker = damageSource.getEntity();
            }

            boolean isExplosion = damageSource.is(DamageTypeTags.IS_EXPLOSION);

            if (attacker instanceof IParryable parryable && !parryable.canBeParried(player)) {
                return;
            }

            event.setCanceled(true);

            ServerArmManager.triggerParryEffects(player);

            if (attacker instanceof LivingEntity livingAttacker && attacker != player) {
                float counterDamage = HighSpeedServerConfig.PARRY_COUNTER_DAMAGE.get().floatValue();
                livingAttacker.hurt(player.damageSources().playerAttack(player), counterDamage);
                livingAttacker.hurtMarked = true;

                Vec3 look = player.getLookAngle();
                livingAttacker.setDeltaMovement(look.scale(0.8).add(0, 0.2, 0));

                if (attacker instanceof IParryable parryable) {
                    parryable.onParried(player);
                }
            }

            if (isExplosion) {
                Vec3 forward = player.getEyePosition().add(player.getLookAngle().scale(2.0));
                float explosionSize = HighSpeedServerConfig.PARRY_EXPLOSION_SIZE.get().floatValue();
                player.level().explode(player, forward.x, forward.y, forward.z, explosionSize, Level.ExplosionInteraction.NONE);
            }

            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ANVIL_PLACE, SoundSource.PLAYERS, 0.6f, 4.0f);
        }
    }
}