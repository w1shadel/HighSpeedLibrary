package com.maxwell.highspeedlib.client.renderer;

import com.maxwell.highspeedlib.common.entity.ShockwaveEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

public class ShockwaveRenderer extends EntityRenderer<ShockwaveEntity> {

    public ShockwaveRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(ShockwaveEntity entity, float yaw, float partialTicks, PoseStack pose, MultiBufferSource buffer, int light) {
        float radius = entity.getRadius() + (entity.getSpeed() * partialTicks);
        float height = entity.getHeight();
        int colorInt = entity.getColor();

        float aBase = ((colorInt >> 24) & 0xFF) / 255.0f;
        float r = ((colorInt >> 16) & 0xFF) / 255.0f;
        float g = ((colorInt >> 8) & 0xFF) / 255.0f;
        float b = (colorInt & 0xFF) / 255.0f;

        float alpha = aBase * (1.0f - (radius / entity.getMaxRadius()));
        if (alpha <= 0) return;

        pose.pushPose();
        VertexConsumer builder = buffer.getBuffer(RenderType.lightning());
        Matrix4f matrix = pose.last().pose();

        int segments = 64;
        for (int i = 0; i < segments; i++) {
            float a1 = (float) (i * Math.PI * 2 / segments);
            float a2 = (float) ((i + 1) * Math.PI * 2 / segments);

            float x1 = (float) Math.cos(a1) * radius;
            float z1 = (float) Math.sin(a1) * radius;
            float x2 = (float) Math.cos(a2) * radius;
            float z2 = (float) Math.sin(a2) * radius;

            vertex(builder, matrix, x1, 0, z1, r, g, b, alpha);
            vertex(builder, matrix, x1, height, z1, r, g, b, 0.0f);
            vertex(builder, matrix, x2, height, z2, r, g, b, 0.0f);
            vertex(builder, matrix, x2, 0, z2, r, g, b, alpha);
            float thickness = 0.5f;
            drawHorizontalSegment(builder, matrix, radius - thickness, radius, a1, a2, r, g, b, alpha);
        }

        pose.popPose();
    }

    private void drawHorizontalSegment(VertexConsumer builder, Matrix4f matrix, float r1, float r2, float a1, float a2, float r, float g, float b, float alpha) {
        vertex(builder, matrix, (float)Math.cos(a1)*r1, 0, (float)Math.sin(a1)*r1, r, g, b, alpha);
        vertex(builder, matrix, (float)Math.cos(a1)*r2, 0, (float)Math.sin(a1)*r2, r, g, b, 0);
        vertex(builder, matrix, (float)Math.cos(a2)*r2, 0, (float)Math.sin(a2)*r2, r, g, b, 0);
        vertex(builder, matrix, (float)Math.cos(a2)*r1, 0, (float)Math.sin(a2)*r1, r, g, b, alpha);
    }

    private void vertex(VertexConsumer builder, Matrix4f matrix, float x, float y, float z, float r, float g, float b, float a) {
        builder.vertex(matrix, x, y, z)
                .color(r, g, b, a)
                .uv(0, 0)
                .overlayCoords(0)
                .uv2(240)
                .normal(0, 1, 0)
                .endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(ShockwaveEntity entity) {
        return null;
    }
}