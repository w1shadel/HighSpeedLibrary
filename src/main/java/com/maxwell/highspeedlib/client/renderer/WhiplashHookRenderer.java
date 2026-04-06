package com.maxwell.highspeedlib.client.renderer;

import com.maxwell.highspeedlib.common.entity.WhiplashHookEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

@SuppressWarnings("removal")
public class WhiplashHookRenderer extends EntityRenderer<WhiplashHookEntity> {
    public WhiplashHookRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(WhiplashHookEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        Player owner = (Player) entity.level().getEntity(entity.getOwnerId());
        if (owner == null) return;
        Vec3 hookPos = entity.getPosition(partialTicks);
        Vec3 start = owner.getEyePosition(partialTicks).add(owner.getLookAngle().cross(new Vec3(0, 1, 0)).normalize().scale(-0.3));
        float dx = (float) (start.x - hookPos.x);
        float dy = (float) (start.y - hookPos.y);
        float dz = (float) (start.z - hookPos.z);
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(new ResourceLocation("minecraft", "textures/block/black_concrete.png")));
        Matrix4f matrix = poseStack.last().pose();
        Vec3 dir = new Vec3(dx, dy, dz);
        Vec3 up = new Vec3(0, 1, 0);
        if (Math.abs(dir.normalize().dot(up)) > 0.9) {
            up = new Vec3(1, 0, 0);
        }
        Vec3 v1 = dir.cross(up).normalize().scale(0.06);
        Vec3 v2 = dir.cross(v1).normalize().scale(0.06);
        float v1x = (float) v1.x, v1y = (float) v1.y, v1z = (float) v1.z;
        float v2x = (float) v2.x, v2y = (float) v2.y, v2z = (float) v2.z;
        float fdx = (float) dx, fdy = (float) dy, fdz = (float) dz;
        consumer.vertex(matrix, -v1x, -v1y, -v1z).color(0, 0, 0, 255).uv(0, 0).overlayCoords(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(poseStack.last().normal(), 0, 1, 0).endVertex();
        consumer.vertex(matrix, v1x, v1y, v1z).color(0, 0, 0, 255).uv(1, 0).overlayCoords(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(poseStack.last().normal(), 0, 1, 0).endVertex();
        consumer.vertex(matrix, fdx + v1x, fdy + v1y, fdz + v1z).color(0, 0, 0, 255).uv(1, 1).overlayCoords(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(poseStack.last().normal(), 0, 1, 0).endVertex();
        consumer.vertex(matrix, fdx - v1x, fdy - v1y, fdz - v1z).color(0, 0, 0, 255).uv(0, 1).overlayCoords(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(poseStack.last().normal(), 0, 1, 0).endVertex();
        consumer.vertex(matrix, -v2x, -v2y, -v2z).color(0, 0, 0, 255).uv(0, 0).overlayCoords(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(poseStack.last().normal(), 1, 0, 0).endVertex();
        consumer.vertex(matrix, v2x, v2y, v2z).color(0, 0, 0, 255).uv(1, 0).overlayCoords(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(poseStack.last().normal(), 1, 0, 0).endVertex();
        consumer.vertex(matrix, fdx + v2x, fdy + v2y, fdz + v2z).color(0, 0, 0, 255).uv(1, 1).overlayCoords(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(poseStack.last().normal(), 1, 0, 0).endVertex();
        consumer.vertex(matrix, fdx - v2x, fdy - v2y, fdz - v2z).color(0, 0, 0, 255).uv(0, 1).overlayCoords(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(poseStack.last().normal(), 1, 0, 0).endVertex();
        super.render(entity, entityYaw, partialTicks, poseStack, bufferSource, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(WhiplashHookEntity entity) {
        return null;
    }
}
