package com.maxwell.highspeedlib.client.renderer;

import com.maxwell.highspeedlib.HighSpeedLib;
import com.maxwell.highspeedlib.client.V1WingModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

public class V1WingRenderer implements ICurioRenderer {
    private static final ResourceLocation TEXTURE = new ResourceLocation(HighSpeedLib.MODID, "textures/item/v1wing_r.png");
    private final V1WingModel<LivingEntity> model;

    public V1WingRenderer() {
        this.model = new V1WingModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(V1WingModel.LAYER_LOCATION));
    }

    @Override
    public <T extends LivingEntity, M extends EntityModel<T>> void render(
            ItemStack stack,
            SlotContext slotContext,
            PoseStack poseStack,
            RenderLayerParent<T, M> renderLayerParent,
            MultiBufferSource renderTypeBuffer,
            int light,
            float limbSwing, float limbSwingAmount,
            float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        LivingEntity entity = slotContext.entity();
        M baseModel = renderLayerParent.getModel();
        if (!(baseModel instanceof HumanoidModel<?> playerModel)) return;
        poseStack.pushPose();
        playerModel.body.translateAndRotate(poseStack);
        poseStack.translate(0.0D, 0D, 0D);
        VertexConsumer vertexConsumer = renderTypeBuffer.getBuffer(RenderType.entityTranslucentEmissive(TEXTURE));
        this.model.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        this.model.renderToBuffer(
                poseStack,
                vertexConsumer,
                15728880,
                OverlayTexture.NO_OVERLAY,
                1.0F, 1.0F, 1.0F, 1.0F
        );
        poseStack.popPose();
    }
}
