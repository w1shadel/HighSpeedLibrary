package com.maxwell.highspeedlib.client.renderer;

import com.maxwell.highspeedlib.client.effect.SpeedTrailManager;
import com.maxwell.highspeedlib.client.state.ClientPlayerState;
import com.maxwell.highspeedlib.client.state.ClientStateManager;
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

import java.util.UUID;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientTrailRenderer {
    public static SpeedTrailManager.TrailInstance getOrCreateTrail(UUID id, String name, float r, float g, float b, float a, float width) {
        return ClientStateManager.getPlayerState(id).trailInstances.computeIfAbsent(name, k -> new SpeedTrailManager.TrailInstance(r, g, b, a, width));
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        Vec3 cameraPos = mc.gameRenderer.getMainCamera().getPosition();
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
        poseStack.pushPose();
        poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        Matrix4f matrix = poseStack.last().pose();
        for (net.minecraft.world.entity.player.Player player : mc.level.players()) {
            ClientPlayerState state = ClientStateManager.getPlayerState(player.getUUID());
            for (SpeedTrailManager.TrailInstance trail : state.trailInstances.values()) {
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
            float alpha = ((float) p1.life / p1.maxLife) * trail.a;
            int r = (int) (trail.r * 255), g = (int) (trail.g * 255), b = (int) (trail.b * 255), a = (int) (alpha * 255);
            float w = trail.width;
            Vec3 dir = p2.pos.subtract(p1.pos).normalize();
            Vec3 up = new Vec3(0, 1, 0);
            if (Math.abs(dir.y) > 0.9) up = new Vec3(1, 0, 0);
            Vec3 v1 = dir.cross(up).normalize().scale(w);
            Vec3 v2 = dir.cross(v1).normalize().scale(w);
            float x1 = (float) p1.pos.x, y1 = (float) p1.pos.y, z1 = (float) p1.pos.z;
            float x2 = (float) p2.pos.x, y2 = (float) p2.pos.y, z2 = (float) p2.pos.z;
            float v1x = (float) v1.x, v1y = (float) v1.y, v1z = (float) v1.z;
            addQuad(matrix, consumer, x1, y1, z1, x2, y2, z2, v1x, v1y, v1z, r, g, b, a);
            float v2x = (float) v2.x, v2y = (float) v2.y, v2z = (float) v2.z;
            addQuad(matrix, consumer, x1, y1, z1, x2, y2, z2, v2x, v2y, v2z, r, g, b, a);
        }
    }

    private static void addQuad(Matrix4f matrix, VertexConsumer consumer, float x1, float y1, float z1, float x2, float y2, float z2, float offX, float offY, float offZ, int r, int g, int b, int a) {
        consumer.vertex(matrix, x1 - offX, y1 - offY, z1 - offZ).color(r, g, b, a).uv2(15728880).endVertex();
        consumer.vertex(matrix, x1 + offX, y1 + offY, z1 + offZ).color(r, g, b, a).uv2(15728880).endVertex();
        consumer.vertex(matrix, x2 + offX, y2 + offY, z2 + offZ).color(r, g, b, a).uv2(15728880).endVertex();
        consumer.vertex(matrix, x2 - offX, y2 - offY, z2 - offZ).color(r, g, b, a).uv2(15728880).endVertex();
        consumer.vertex(matrix, x1 - offX, y1 - offY, z1 - offZ).color(r, g, b, a).uv2(15728880).endVertex();
        consumer.vertex(matrix, x2 - offX, y2 - offY, z2 - offZ).color(r, g, b, a).uv2(15728880).endVertex();
        consumer.vertex(matrix, x2 + offX, y2 + offY, z2 + offZ).color(r, g, b, a).uv2(15728880).endVertex();
        consumer.vertex(matrix, x1 + offX, y1 + offY, z1 + offZ).color(r, g, b, a).uv2(15728880).endVertex();
    }

    public static void tick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            for (net.minecraft.world.entity.player.Player player : mc.level.players()) {
                ClientPlayerState state = ClientStateManager.getPlayerState(player.getUUID());
                state.trailInstances.values().forEach(SpeedTrailManager.TrailInstance::tick);
            }
        }
    }
}