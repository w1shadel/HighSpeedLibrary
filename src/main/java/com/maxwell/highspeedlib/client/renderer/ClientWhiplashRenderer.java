package com.maxwell.highspeedlib.client.renderer;

import com.maxwell.highspeedlib.client.logic.ClientWhiplashManager;
import com.maxwell.highspeedlib.common.logic.combat.ServerWhiplashManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;

@SuppressWarnings("removal")
@EventBusSubscriber(value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public class ClientWhiplashRenderer {
    private static final ResourceLocation BLACK_TEXTURE = ResourceLocation.fromNamespaceAndPath("minecraft", "textures/block/black_concrete.png");

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;
        float partialTicks = event.getPartialTick().getRealtimeDeltaTicks();
        PoseStack poseStack = event.getPoseStack();
        Vec3 cameraPos = mc.gameRenderer.getMainCamera().getPosition();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        for (Player player : mc.level.players()) {
            ServerWhiplashManager.HookData data = ClientWhiplashManager.getHookData(player.getUUID());
            if (data == null || data.state == ServerWhiplashManager.NONE) continue;
            Vec3 start = player.getEyePosition(partialTicks).add(player.getViewVector(partialTicks).cross(new Vec3(0, 1, 0)).normalize().scale(-0.35));
            Vec3 hookPos;
            if (data.state == ServerWhiplashManager.HOOKED && data.targetId != -1) {
                Entity target = mc.level.getEntity(data.targetId);
                hookPos = (target != null) ? target.getPosition(partialTicks).add(0, target.getBbHeight() * 0.5, 0) : start;
            } else if (data.state == ServerWhiplashManager.RETRACTING && data.hitPos != null) {
                Vec3 dirToHit = data.hitPos.subtract(start).normalize();
                hookPos = start.add(dirToHit.scale((float) data.distance));
            } else {
                hookPos = start.add(data.shootDir.scale((float) data.distance));
            }
            poseStack.pushPose();
            poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
            VertexConsumer consumer = mc.renderBuffers().bufferSource().getBuffer(RenderType.entitySolid(BLACK_TEXTURE));
            Matrix4f matrix = poseStack.last().pose();
            Vec3 dirVec = hookPos.subtract(start);
            double totalDist = dirVec.length();
            int segments = (int) Math.max(totalDist * 2, 8);
            int r = 0, g = 0, b = 0, a = 255;
            Vec3 lastPos = start;
            for (int i = 1; i <= segments; i++) {
                float t = (float) i / segments;
                Vec3 currentPos = start.add(dirVec.scale(t));
                if (data.state == ServerWhiplashManager.RETRACTING) {
                    double wave = Math.sin(t * Math.PI * 4 + (mc.level.getGameTime() + partialTicks) * 0.5) * 0.12 * (1.0 - t);
                    currentPos = currentPos.add(0, wave, 0);
                }
                drawContinuousSegment(matrix, consumer, lastPos, currentPos, r, g, b, a);
                lastPos = currentPos;
            }
            drawHookHead(matrix, consumer, hookPos, 0.08f, r, g, b, a);
            poseStack.popPose();
        }
    }

    private static void drawContinuousSegment(Matrix4f matrix, VertexConsumer consumer, Vec3 start, Vec3 end, int r, int g, int b, int a) {
        Vec3 dir = end.subtract(start).normalize();
        Vec3 up = new Vec3(0, 1, 0);
        if (Math.abs(dir.dot(up)) > 0.9) up = new Vec3(1, 0, 0);
        Vec3 v1 = dir.cross(up).normalize().scale(0.03);
        Vec3 v2 = dir.cross(v1).normalize().scale(0.03);
        float sx = (float) start.x, sy = (float) start.y, sz = (float) start.z;
        float ex = (float) end.x, ey = (float) end.y, ez = (float) end.z;
        drawQuadInternal(matrix, consumer, sx - (float) v1.x, sy - (float) v1.y, sz - (float) v1.z,
                sx + (float) v1.x, sy + (float) v1.y, sz + (float) v1.z,
                ex + (float) v1.x, ey + (float) v1.y, ez + (float) v1.z,
                ex - (float) v1.x, ey - (float) v1.y, ez - (float) v1.z, r, g, b, a);
        drawQuadInternal(matrix, consumer, sx - (float) v2.x, sy - (float) v2.y, sz - (float) v2.z,
                sx + (float) v2.x, sy + (float) v2.y, sz + (float) v2.z,
                ex + (float) v2.x, ey + (float) v2.y, ez + (float) v2.z,
                ex - (float) v2.x, ey - (float) v2.y, ez - (float) v2.z, r, g, b, a);
    }

    private static void drawQuadInternal(Matrix4f matrix, VertexConsumer consumer, float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, float x4, float y4, float z4, int r, int g, int b, int a) {
        consumer.addVertex(matrix, x1, y1, z1).setColor(r, g, b, a).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(15728880).setNormal(0, 1, 0);
        consumer.addVertex(matrix, x2, y2, z2).setColor(r, g, b, a).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(15728880).setNormal(0, 1, 0);
        consumer.addVertex(matrix, x3, y3, z3).setColor(r, g, b, a).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(15728880).setNormal(0, 1, 0);
        consumer.addVertex(matrix, x4, y4, z4).setColor(r, g, b, a).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(15728880).setNormal(0, 1, 0);
    }

    private static void drawHookHead(Matrix4f matrix, VertexConsumer consumer, Vec3 pos, float size, int r, int g, int b, int a) {
        float x = (float) pos.x, y = (float) pos.y, z = (float) pos.z;
        drawQuadInternal(matrix, consumer, x - size, y - size, z - size, x + size, y - size, z - size, x + size, y + size, z - size, x - size, y + size, z - size, r, g, b, a);
        drawQuadInternal(matrix, consumer, x - size, y - size, z + size, x + size, y - size, z + size, x + size, y + size, z + size, x - size, y + size, z + size, r, g, b, a);
        drawQuadInternal(matrix, consumer, x - size, y - size, z - size, x - size, y - size, z + size, x - size, y + size, z + size, x - size, y + size, z - size, r, g, b, a);
        drawQuadInternal(matrix, consumer, x + size, y - size, z - size, x + size, y - size, z + size, x + size, y + size, z + size, x + size, y + size, z - size, r, g, b, a);
    }
}