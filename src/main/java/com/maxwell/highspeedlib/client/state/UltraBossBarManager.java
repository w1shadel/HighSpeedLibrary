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
    private static final List<LivingEntity> activeBosses = new ArrayList<>();

    
    public static void track(Entity entity) {
        if (entity instanceof LivingEntity living) {
            trackedEntities.add(living.getUUID());
        }
    }

    public static List<LivingEntity> getActiveBosses() {
        return activeBosses;
    }

    
    public static void tick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            activeBosses.clear();
            return;
        }

        activeBosses.clear();

        for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity instanceof LivingEntity living) {
                if (MobModeManager.isBoss(living) || trackedEntities.contains(living.getUUID())) {
                    if (living.isAlive() && living.distanceToSqr(mc.player) < 4096) { 
                        activeBosses.add(living);
                    } else if (!living.isAlive()) {
                        trackedEntities.remove(living.getUUID());
                    }
                }
            }
        }

        
        activeBosses.sort((e1, e2) -> {
            double d1 = e1.distanceToSqr(mc.player);
            double d2 = e2.distanceToSqr(mc.player);
            return Double.compare(d1, d2);
        });
    }
}
