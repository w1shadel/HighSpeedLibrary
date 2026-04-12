package com.maxwell.highspeedlib.client.state;

import com.maxwell.highspeedlib.api.main.mob.MobModeManager;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;


public class UltraBossBarManager {
    private static final Set<UUID> trackedEntities = new HashSet<>();
    private static LivingEntity currentBoss = null;

    
    public static void track(Entity entity) {
        if (entity instanceof LivingEntity living) {
            trackedEntities.add(living.getUUID());
        }
    }

    public static LivingEntity getCurrentBoss() {
        return currentBoss;
    }

    
    public static void tick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            currentBoss = null;
            return;
        }

        List<LivingEntity> candidates = new ArrayList<>();

        for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity instanceof LivingEntity living) {
                if (MobModeManager.isBoss(living) || trackedEntities.contains(living.getUUID())) {
                    if (living.isAlive() && living.distanceToSqr(mc.player) < 4096) { 
                        candidates.add(living);
                    } else if (!living.isAlive()) {
                        trackedEntities.remove(living.getUUID());
                    }
                }
            }
        }

        if (candidates.isEmpty()) {
            currentBoss = null;
            return;
        }


        LivingEntity best = null;
        double bestScore = Double.MAX_VALUE;

        for (LivingEntity e : candidates) {
            double distSqr = e.distanceToSqr(mc.player);

            var viewVec = mc.player.getViewVector(1.0f);
            var toEntity = e.position().add(0, e.getEyeHeight() * 0.5, 0).subtract(mc.player.getEyePosition(1.0f)).normalize();
            double dot = viewVec.dot(toEntity);


            double score = distSqr - (dot * 500.0);

            if (score < bestScore) {
                bestScore = score;
                best = e;
            }
        }

        currentBoss = best;
    }
}
