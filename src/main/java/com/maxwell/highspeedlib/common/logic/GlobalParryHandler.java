package com.maxwell.highspeedlib.common.logic;

import com.maxwell.highspeedlib.HighSpeedLib;
import com.maxwell.highspeedlib.common.entity.ThrownCoinEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = HighSpeedLib.MODID)
public class GlobalParryHandler {
    @SubscribeEvent
    public static void onImpact(ProjectileImpactEvent event) {
        Projectile p = event.getProjectile();
        if (p.level().isClientSide) return;
        if (event.getRayTraceResult() instanceof EntityHitResult eHit && eHit.getEntity() instanceof ServerPlayer player) {
            if (ServerArmManager.isPlayerParrying(player)) {
                if (!(p instanceof ThrownCoinEntity)) {
                    ServerArmManager.performProjectileParry(p, player);
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
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (ServerArmManager.isPlayerParrying(player)) {
            Entity attacker = event.getSource().getDirectEntity();
            if (attacker instanceof LivingEntity livingAttacker && attacker != player) {
                event.setCanceled(true);
                event.setAmount(0);
                livingAttacker.hurt(player.damageSources().mobAttack(player), 12.0f);
                livingAttacker.hurtMarked = true;
                ServerArmManager.triggerParryEffects(player);
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                        net.minecraft.sounds.SoundEvents.ANVIL_PLACE, net.minecraft.sounds.SoundSource.PLAYERS, 0.5f, 2.0f);
            }
        }
    }
}