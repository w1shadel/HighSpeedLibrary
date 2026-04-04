package com.maxwell.highspeedlib.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ParryArmRenderer {
    private static float animationProgress = 0f;
    private static boolean isPunching = false;
    private static int hitstopTicks = 0;

    public static void startPunch() {
        isPunching = true;
        animationProgress = 0f;
    }
    public static void startHitstop(int ticks) {
        hitstopTicks = ticks;
    }
    public static boolean isPunching() {
        return isPunching;
    }

    private static float getPunchCurve(float progress) {

        if (progress >= 1.0f) return 0.0f;

        if (progress < 0.1f) {

            return (float) Math.sin((progress / 0.1f) * Math.PI / 2);
        } else if (progress < 0.25f) {

            return 1.0f;
        } else {


            float backProgress = (progress - 0.25f) / 0.75f;

            return 1.0f - (float) (1.0 - Math.pow(1.0 - backProgress, 3));
        }
    }


    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        if (hitstopTicks > 0) {
            hitstopTicks--;
            return;
        }

        if (isPunching) {
            animationProgress += 0.08f;

            if (animationProgress >= 1.0f) {
                animationProgress = 0f;
                isPunching = false;
            }
        }
    }
    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        if (!isPunching) return;

        if (event.getHand() == InteractionHand.OFF_HAND) {
            renderPunch(event.getPoseStack(), event.getMultiBufferSource(), event.getPackedLight());

            event.setCanceled(true);
        }
    }

    private static void renderPunch(PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;

        float swing = getPunchCurve(animationProgress);

        PlayerRenderer playerRenderer = (PlayerRenderer) mc.getEntityRenderDispatcher().getRenderer(player);
        PlayerModel<AbstractClientPlayer> model = playerRenderer.getModel();

        poseStack.pushPose();

        float fov = (float) mc.options.fov().get();
        float fovScale = 70.0f / fov;

        double shoulderX = -0.85D * fovScale;
        double shoulderY = -0.55D;
        double shoulderZ = 0.2D;
        poseStack.translate(shoulderX, shoulderY, shoulderZ);

        poseStack.mulPose(Axis.XP.rotationDegrees(-90f));



        poseStack.mulPose(Axis.ZP.rotationDegrees(-15f));


        poseStack.mulPose(Axis.XP.rotationDegrees(12f));


        double punchReach = swing * 1.5D;
        poseStack.translate(0.0D, punchReach, 0.0D);

        poseStack.mulPose(Axis.YP.rotationDegrees(swing * 90f));

        model.leftArm.xRot = 0;
        model.leftArm.yRot = 0;
        model.leftArm.zRot = 0;
        model.leftArm.setPos(0, 0, 0);

        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entitySolid(player.getSkinTextureLocation()));
        float r = 0.2f, g = 0.6f, b = 1.0f;

        model.leftArm.render(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, r, g, b, 1.0f);
        model.leftSleeve.copyFrom(model.leftArm);
        model.leftSleeve.render(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, r, g, b, 0.8f);

        poseStack.popPose();
    }
}