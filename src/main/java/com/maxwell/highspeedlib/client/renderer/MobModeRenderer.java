package com.maxwell.highspeedlib.client.renderer;

import com.maxwell.highspeedlib.HighSpeedLib;
import com.maxwell.highspeedlib.api.main.mob.MobModeManager;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

@Mod.EventBusSubscriber(modid = HighSpeedLib.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class MobModeRenderer {
    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        PoseStack poseStack = event.getPoseStack();
        Vec3 cameraPos = mc.gameRenderer.getMainCamera().getPosition();
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        boolean renderedAny = false;
        for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity instanceof LivingEntity living && MobModeManager.isEnraged(living)) {
                renderEnrageCircle(poseStack, living, event.getPartialTick(), cameraPos, bufferSource);
                renderedAny = true;
            }
        }
        if (renderedAny) {
            bufferSource.endBatch(RenderType.lightning());
        }
    }

    private static void renderEnrageCircle(PoseStack poseStack, LivingEntity entity, float partialTicks, Vec3 cameraPos, MultiBufferSource bufferSource) {
        poseStack.pushPose();
        double x = Mth.lerp(partialTicks, entity.xo, entity.getX()) - cameraPos.x;
        double y = Mth.lerp(partialTicks, entity.yo, entity.getY()) - cameraPos.y + (entity.getBbHeight() * 0.5f);
        double z = Mth.lerp(partialTicks, entity.zo, entity.getZ()) - cameraPos.z;
        poseStack.translate(x, y, z);
        poseStack.mulPose(Minecraft.getInstance().gameRenderer.getMainCamera().rotation());
        poseStack.translate(0, 0, -0.2f);
        float alpha = 1.0f;
        float radius = entity.getBbWidth() * 1.5f;
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();
        int segments = 48;
        for (int i = 0; i < segments; i++) {
            float a1 = (float) (i * Math.PI * 2 / segments);
            float a2 = (float) ((i + 1) * Math.PI * 2 / segments);
            drawSegment(matrix, consumer, a1, a2, radius, radius * 0.70f, 255, 0, 0, (int) (255 * alpha));
            drawSegment(matrix, consumer, a1, a2, radius * 0.70f, 0, 255, 30, 30, (int) (180 * alpha));
        }
        poseStack.popPose();
    }

    private static void drawSegment(Matrix4f matrix, VertexConsumer consumer, float a1, float a2, float r1, float r2, int r, int g, int b, int alpha) {
        consumer.vertex(matrix, (float) Math.cos(a1) * r1, (float) Math.sin(a1) * r1, 0).color(r, g, b, alpha).endVertex();
        consumer.vertex(matrix, (float) Math.cos(a2) * r1, (float) Math.sin(a2) * r1, 0).color(r, g, b, alpha).endVertex();
        consumer.vertex(matrix, (float) Math.cos(a2) * r2, (float) Math.sin(a2) * r2, 0).color(r, g, b, alpha).endVertex();
        consumer.vertex(matrix, (float) Math.cos(a1) * r2, (float) Math.sin(a1) * r2, 0).color(r, g, b, alpha).endVertex();
    }
}