package com.maxwell.highspeedlib.client.logic;

import com.maxwell.highspeedlib.client.renderer.ClientTrailRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;

public class ClientDashHandler {
    public static void spawnDashEffects() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        Vec3 pos = mc.player.position();
        Vec3 look = mc.player.getLookAngle().normalize();
        Vec3 velocity = mc.player.getDeltaMovement();
        var rand = mc.level.random;
        long time = mc.level.getGameTime();
        int lineCount = 15;
        for (int i = 0; i < lineCount; i++) {
            double radius = 1.2;
            double angle = rand.nextDouble() * Math.PI * 2;
            double offsetX = Math.cos(angle) * radius * rand.nextDouble();
            double offsetZ = Math.sin(angle) * radius * rand.nextDouble();
            double offsetY = 0.2 + rand.nextDouble() * 1.8;
            Vec3 spawnPos = pos.add(offsetX, offsetY, offsetZ);
            Vec3 tailPos = spawnPos.subtract(look.scale(1.5));
            String lineID = "dash_" + mc.player.getUUID() + "_" + time + "_" + i;
            float thickness = 0.015f;
            var trail = ClientTrailRenderer.getOrCreateTrail(mc.player.getUUID(), lineID, 1.0f, 1.0f, 1.0f, 0.15f, thickness);
            trail.addPoint(spawnPos, 4);
            trail.addPoint(tailPos, 4);
        }
    }
}
