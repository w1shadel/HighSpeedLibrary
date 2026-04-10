package com.maxwell.highspeedlib.client.renderer;

import com.maxwell.highspeedlib.HighSpeedLib;
import com.maxwell.highspeedlib.api.main.mob.MobModeManager;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = HighSpeedLib.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class MobOutlineRenderer {
    @SubscribeEvent
    public static void onRenderLiving(RenderLivingEvent.Post<LivingEntity, EntityModel<LivingEntity>> event) {
        LivingEntity entity = event.getEntity();
        int tier = MobModeManager.getRadianceTier(entity);
        boolean isEnraged = MobModeManager.isEnraged(entity);
        if (tier <= 0 && !isEnraged) return;
        int r, g, b;
        if (tier > 0) {
            float hue = (entity.tickCount + event.getPartialTick()) * 0.05f;
            int rgb = net.minecraft.util.Mth.hsvToRgb(hue % 1.0f, 0.7f, 1.0f);
            r = (rgb >> 16) & 0xFF;
            g = (rgb >> 8) & 0xFF;
            b = rgb & 0xFF;
        } else {
            r = 255;
            g = 50;
            b = 50;
        }
        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();
        float scale = 1.04f;
        poseStack.scale(scale, scale, scale);
        poseStack.translate(0, -0.01, 0);
        ResourceLocation texture = event.getRenderer().getTextureLocation(entity);
        VertexConsumer consumer = event.getMultiBufferSource().getBuffer(RenderType.entityTranslucentEmissive(texture));
        event.getRenderer().getModel().renderToBuffer(
                poseStack,
                consumer,
                15728880,
                OverlayTexture.NO_OVERLAY,
                r / 255f, g / 255f, b / 255f,
                0.5f
        );
        poseStack.popPose();
    }
}