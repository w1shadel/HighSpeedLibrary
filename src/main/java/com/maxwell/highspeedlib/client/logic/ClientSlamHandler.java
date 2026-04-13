package com.maxwell.highspeedlib.client.logic;

import com.maxwell.highspeedlib.HighSpeedLib;
import com.maxwell.highspeedlib.client.renderer.ClientTrailRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Mod.EventBusSubscriber(modid = HighSpeedLib.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientSlamHandler {
    private static final Set<Integer> slammingEntities = new HashSet<>();
    private static final List<SlamWave> activeWaves = new ArrayList<>();

    public static void updateSlamming(int entityId, boolean isSlamming) {
        if (isSlamming) slammingEntities.add(entityId);
        else slammingEntities.remove(entityId);
    }

    public static void spawnImpactWave(Vec3 pos) {
        activeWaves.add(new SlamWave(pos));
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        activeWaves.removeIf(w -> ++w.age >= w.maxAge);
        for (int id : slammingEntities) {
            Entity entity = mc.level.getEntity(id);
            if (!(entity instanceof Player player)) continue;
            var rand = mc.level.random;
            long time = mc.level.getGameTime();
            for (int i = 0; i < 2; i++) {
                Vec3 currentPos = player.position().add((rand.nextFloat() - 0.5) * 1.2, 0.5 + rand.nextFloat(), (rand.nextFloat() - 0.5) * 1.2);
                Vec3 tailPos = currentPos.add(0, 1.5, 0);
                String lineID = "slam_trail_" + id + "_" + (time % 10) + "_" + i;
                ClientTrailRenderer.getOrCreateTrail(player.getUUID(), lineID, 1.0f, 0.5f, 0.1f, 0.6f, 0.1f)
                        .addPoint(currentPos, 6);
                ClientTrailRenderer.getOrCreateTrail(player.getUUID(), lineID, 1.0f, 0.5f, 0.1f, 0.6f, 0.1f)
                        .addPoint(tailPos, 6);
            }
        }
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;
        if (activeWaves.isEmpty()) return;
        Minecraft mc = Minecraft.getInstance();
        Vec3 cameraPos = mc.gameRenderer.getMainCamera().getPosition();
        PoseStack poseStack = event.getPoseStack();
        VertexConsumer consumer = mc.renderBuffers().bufferSource().getBuffer(RenderType.leash());
        poseStack.pushPose();
        poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        Matrix4f matrix = poseStack.last().pose();
        for (SlamWave wave : activeWaves) {
            renderShockwave(matrix, consumer, wave);
        }
        poseStack.popPose();
    }

    private static void renderShockwave(Matrix4f matrix, VertexConsumer consumer, SlamWave wave) {
        float progress = (float) wave.age / wave.maxAge;
        float ease = 1.0f - (float) Math.pow(1.0f - progress, 4);
        float radius = ease * 5.0f;
        float innerRadius = radius * 0.8f;
        float alpha = (1.0f - progress) * 0.6f;
        int r = 255, g = 255, b = 255, a = (int) (alpha * 255);
        int segments = 32;
        for (int i = 0; i < segments; i++) {
            float angle = (float) (i * Math.PI * 2 / segments);
            float nextAngle = (float) ((i + 1) * Math.PI * 2 / segments);
            float x1 = (float) (wave.pos.x + Math.cos(angle) * innerRadius);
            float z1 = (float) (wave.pos.z + Math.sin(angle) * innerRadius);
            float x2 = (float) (wave.pos.x + Math.cos(angle) * radius);
            float z2 = (float) (wave.pos.z + Math.sin(angle) * radius);
            float x3 = (float) (wave.pos.x + Math.cos(nextAngle) * radius);
            float z3 = (float) (wave.pos.z + Math.sin(nextAngle) * radius);
            float x4 = (float) (wave.pos.x + Math.cos(nextAngle) * innerRadius);
            float z4 = (float) (wave.pos.z + Math.sin(nextAngle) * innerRadius);
            float y = (float) wave.pos.y + 0.15f;
            addWaveVertex(matrix, consumer, x1, y, z1, r, g, b, a);
            addWaveVertex(matrix, consumer, x2, y, z2, r, g, b, a);
            addWaveVertex(matrix, consumer, x3, y, z3, r, g, b, a);
            addWaveVertex(matrix, consumer, x4, y, z4, r, g, b, a);
            addWaveVertex(matrix, consumer, x4, y, z4, r, g, b, a);
            addWaveVertex(matrix, consumer, x3, y, z3, r, g, b, a);
            addWaveVertex(matrix, consumer, x2, y, z2, r, g, b, a);
            addWaveVertex(matrix, consumer, x1, y, z1, r, g, b, a);
        }
    }

    private static void addWaveVertex(Matrix4f matrix, VertexConsumer consumer, float x, float y, float z, int r, int g, int b, int a) {
        consumer.vertex(matrix, x, y, z).color(r, g, b, a).uv2(15728880).endVertex();
    }

    private static class SlamWave {
        final int maxAge = 12;
        Vec3 pos;
        int age;

        SlamWave(Vec3 pos) {
            this.pos = pos;
            this.age = 0;
        }
    }
}
