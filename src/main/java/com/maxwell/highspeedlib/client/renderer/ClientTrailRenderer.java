package com.maxwell.highspeedlib.client.renderer;

import com.maxwell.highspeedlib.client.effect.SpeedTrailManager;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientTrailRenderer {
    private static final Map<UUID, Map<String, SpeedTrailManager.TrailInstance>> playerTrails = new HashMap<>();

    public static SpeedTrailManager.TrailInstance getOrCreateTrail(UUID id, String name, float r, float g, float b, float a, float width) {
        return playerTrails.computeIfAbsent(id, k -> new HashMap<>())
                .computeIfAbsent(name, k -> new SpeedTrailManager.TrailInstance(r, g, b, a, width));
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;
        Minecraft mc = Minecraft.getInstance();
        Vec3 cameraPos = mc.gameRenderer.getMainCamera().getPosition();
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
        poseStack.pushPose();
        poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        Matrix4f matrix = poseStack.last().pose();
        for (var playerEntry : playerTrails.entrySet()) {
            for (var trail : playerEntry.getValue().values()) {
                drawTrail(matrix, consumer, trail);
            }
        }
        poseStack.popPose();
    }

    private static void drawTrail(Matrix4f matrix, VertexConsumer consumer, SpeedTrailManager.TrailInstance trail) {
        if (trail.points.size() < 2) return;
        for (int i = 0; i < trail.points.size() - 1; i++) {
            SpeedTrailManager.TrailPoint p1 = trail.points.get(i);
            SpeedTrailManager.TrailPoint p2 = trail.points.get(i + 1);
            float alpha1 = ((float) p1.life / p1.maxLife) * trail.a;
            float alpha2 = ((float) p2.life / p2.maxLife) * trail.a;
            int r = (int) (trail.r * 255), g = (int) (trail.g * 255), b = (int) (trail.b * 255);
            int a1 = (int) (alpha1 * 255), a2 = (int) (alpha2 * 255);
            float w = trail.width;
            consumer.vertex(matrix, (float) p1.pos.x, (float) p1.pos.y - w, (float) p1.pos.z).color(r, g, b, a1).endVertex();
            consumer.vertex(matrix, (float) p2.pos.x, (float) p2.pos.y - w, (float) p2.pos.z).color(r, g, b, a2).endVertex();
            consumer.vertex(matrix, (float) p2.pos.x, (float) p2.pos.y + w, (float) p2.pos.z).color(r, g, b, a2).endVertex();
            consumer.vertex(matrix, (float) p1.pos.x, (float) p1.pos.y + w, (float) p1.pos.z).color(r, g, b, a1).endVertex();
            consumer.vertex(matrix, (float) p1.pos.x, (float) p1.pos.y + w, (float) p1.pos.z).color(r, g, b, a1).endVertex();
            consumer.vertex(matrix, (float) p2.pos.x, (float) p2.pos.y + w, (float) p2.pos.z).color(r, g, b, a2).endVertex();
            consumer.vertex(matrix, (float) p2.pos.x, (float) p2.pos.y - w, (float) p2.pos.z).color(r, g, b, a2).endVertex();
            consumer.vertex(matrix, (float) p1.pos.x, (float) p1.pos.y - w, (float) p1.pos.z).color(r, g, b, a1).endVertex();
        }
    }

    public static void tick() {
        playerTrails.values().forEach(map -> map.values().forEach(SpeedTrailManager.TrailInstance::tick));
    }
}