package com.maxwell.highspeedlib.client.renderer;

import com.maxwell.highspeedlib.HighSpeedLib;
import com.maxwell.highspeedlib.api.main.mob.MobModeManager;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
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
        for (LivingEntity entity : mc.level.getEntitiesOfClass(LivingEntity.class, mc.player.getBoundingBox().inflate(64))) {
            if (MobModeManager.isEnraged(entity)) {
                renderEnrageCircle(poseStack, entity, event.getPartialTick(), cameraPos);
            }
        }
    }

    private static void renderEnrageCircle(PoseStack poseStack, LivingEntity entity, float partialTicks, Vec3 cameraPos) {
        poseStack.pushPose();
        double x = Mth.lerp(partialTicks, entity.xo, entity.getX()) - cameraPos.x;
        double y = Mth.lerp(partialTicks, entity.yo, entity.getY()) - cameraPos.y + (entity.getBbHeight() * 0.5);
        double z = Mth.lerp(partialTicks, entity.zo, entity.getZ()) - cameraPos.z;
        poseStack.translate(x, y, z);
        float radius = entity.getBbWidth() * 0.8f;
        float innerRadius = radius * 0.9f;
        VertexConsumer consumer = Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();
        float rotation = (entity.tickCount + partialTicks) * 0.1f;
        int segments = 32;
        for (int i = 0; i < segments; i++) {
            float angle = (float) (i * Math.PI * 2 / segments) + rotation;
            float nextAngle = (float) ((i + 1) * Math.PI * 2 / segments) + rotation;
            drawSegment(matrix, consumer, angle, nextAngle, radius, innerRadius, 255, 30, 30, 200);
            drawSegment(matrix, consumer, angle, nextAngle, innerRadius, 0, 255, 100, 100, 100);
        }
        poseStack.popPose();
    }

    private static void drawSegment(Matrix4f matrix, VertexConsumer consumer, float a1, float a2, float r1, float r2, int r, int g, int b, int alpha) {
        consumer.vertex(matrix, (float) Math.cos(a1) * r1, 0, (float) Math.sin(a1) * r1).color(r, g, b, alpha).endVertex();
        consumer.vertex(matrix, (float) Math.cos(a2) * r1, 0, (float) Math.sin(a2) * r1).color(r, g, b, alpha).endVertex();
        consumer.vertex(matrix, (float) Math.cos(a2) * r2, 0, (float) Math.sin(a2) * r2).color(r, g, b, alpha).endVertex();
        consumer.vertex(matrix, (float) Math.cos(a1) * r2, 0, (float) Math.sin(a1) * r2).color(r, g, b, alpha).endVertex();
    }
}