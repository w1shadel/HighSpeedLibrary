package com.maxwell.highspeedlib.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ExtendsArmRenderer {
    private static float animationProgress = 0f;
    private static boolean isPunching = false;
    private static ArmAnimType currentAnim = ArmAnimType.NONE;

    public static boolean isPunching() {
        return isPunching;
    }

    public static void startPunch() {
        currentAnim = ArmAnimType.PUNCH;
        animationProgress = 0f;
        isPunching = true;
    }

    public static void startToss() {
        currentAnim = ArmAnimType.TOSS;
        animationProgress = 0f;
        isPunching = true;
    }

    private static float getTossCurve(float progress) {
        if (progress < 0.2f) {
            return (float) Math.sin((progress / 0.2f) * Math.PI / 2);
        } else {
            return 1.0f - (float) Math.pow((progress - 0.2f) / 0.8f, 2);
        }
    }

    public static float getPunchCurve(float progress) {
        if (progress < 0.1f) return (float) Math.sin((progress / 0.1f) * Math.PI / 2);
        else if (progress < 0.25f) return 1.0f;
        else {
            float backProgress = (progress - 0.25f) / 0.75f;
            return 1.0f - (float) (1.0 - Math.pow(1.0 - backProgress, 3));
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!com.maxwell.highspeedlib.common.logic.TimeManager.shouldTick()) return;
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
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        if (isPunching && event.getHand() == InteractionHand.OFF_HAND) {
            if (currentAnim == ArmAnimType.PUNCH) {
                renderPunch(event.getPoseStack(), event.getMultiBufferSource(), event.getPackedLight());
                event.setCanceled(true);
            } else if (currentAnim == ArmAnimType.TOSS) {
                renderCoinToss(event.getPoseStack(), event.getMultiBufferSource(), event.getPackedLight());
                event.setCanceled(true);
            }
            return;
        }
        com.maxwell.highspeedlib.common.logic.ServerWhiplashManager.HookData hookData = com.maxwell.highspeedlib.client.ClientWhiplashManager.getHookData(player.getUUID());
        int renderTicks = com.maxwell.highspeedlib.client.ClientWhiplashManager.getRenderTicks(player.getUUID());
        if (renderTicks > 0 && event.getHand() == InteractionHand.OFF_HAND) {
            float renderProgress = renderTicks / 10.0f;
            if (renderProgress > 1.0f) renderProgress = 1.0f;
            renderWhiplashArm(event.getPoseStack(), event.getMultiBufferSource(), event.getPackedLight(), renderProgress, hookData);
            event.setCanceled(true);
        }
    }

    private static void renderWhiplashArm(PoseStack poseStack, MultiBufferSource buffer, int packedLight, float progress, com.maxwell.highspeedlib.common.logic.ServerWhiplashManager.HookData data) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;
        PlayerRenderer playerRenderer = (PlayerRenderer) mc.getEntityRenderDispatcher().getRenderer(player);
        PlayerModel<AbstractClientPlayer> model = playerRenderer.getModel();
        poseStack.pushPose();
        float swing = 1.0f - (float) Math.pow(1.0f - progress, 3);
        float tensionIntensity = 0f;
        if (data.state == com.maxwell.highspeedlib.common.logic.ServerWhiplashManager.HOOKED) {
            tensionIntensity = 0.035f;
        } else if (data.state == com.maxwell.highspeedlib.common.logic.ServerWhiplashManager.RETRACTING) {
            tensionIntensity = 0.015f;
        }
        float tensionShakeX = (float) (Math.sin(player.tickCount * 4.2) * tensionIntensity);
        float tensionShakeY = (float) (Math.cos(player.tickCount * 3.8) * tensionIntensity);
        float fov = (float) mc.options.fov().get();
        float fovScale = 70.0f / fov;
        double shoulderX = -1.0D * fovScale;
        double shoulderY = -0.55D;
        double shoulderZ = 0.2D;
        poseStack.translate(shoulderX + tensionShakeX, shoulderY + tensionShakeY, shoulderZ);
        poseStack.mulPose(Axis.XP.rotationDegrees(-90f));
        poseStack.mulPose(Axis.ZP.rotationDegrees(15f));
        float recoilAngle = 40f * (1.0f - swing);
        if (data.state == com.maxwell.highspeedlib.common.logic.ServerWhiplashManager.HOOKED) {
            recoilAngle += 10f;
        }
        poseStack.mulPose(Axis.XP.rotationDegrees(recoilAngle));
        double extension = swing * 1.6D;
        poseStack.translate(0.0D, extension, 0.0D);
        poseStack.mulPose(Axis.YP.rotationDegrees(swing * 30f));
        model.leftArm.xRot = 0;
        model.leftArm.yRot = 0;
        model.leftArm.zRot = 0;
        model.leftArm.setPos(0, 0, 0);
        boolean oldVisible = model.leftArm.visible;
        boolean oldSleeveVisible = model.leftSleeve.visible;
        model.leftArm.visible = true;
        model.leftSleeve.visible = true;
        int overlay = LivingEntityRenderer.getOverlayCoords(player, 0.0F);
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(player.getSkinTextureLocation()));
        float r = 1.0f, g = 1.0f, b = 1.0f;
        model.leftArm.render(poseStack, vertexConsumer, packedLight, overlay, r, g, b, 1.0f);
        model.leftSleeve.copyFrom(model.leftArm);
        model.leftSleeve.render(poseStack, vertexConsumer, packedLight, overlay, r, g, b, 1.0f);
        model.leftArm.visible = oldVisible;
        model.leftSleeve.visible = oldSleeveVisible;
        poseStack.popPose();
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
        boolean oldVisible = model.leftArm.visible;
        boolean oldSleeveVisible = model.leftSleeve.visible;
        model.leftArm.visible = true;
        model.leftSleeve.visible = true;
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        int overlay = LivingEntityRenderer.getOverlayCoords(player, 0.0F);
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(player.getSkinTextureLocation()));
        float r = 1.0f, g = 1.0f, b = 1.0f;
        model.leftArm.render(poseStack, vertexConsumer, packedLight, overlay, r, g, b, 1.0f);
        model.leftSleeve.copyFrom(model.leftArm);
        model.leftSleeve.render(poseStack, vertexConsumer, packedLight, overlay, r, g, b, 1.0f);
        model.leftArm.visible = oldVisible;
        model.leftSleeve.visible = oldSleeveVisible;
        poseStack.popPose();
    }

    private static void renderCoinToss(PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        float swing = getTossCurve(animationProgress);
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        poseStack.pushPose();
        float fovAdjust = 70.0f / (float) Minecraft.getInstance().options.fov().get();
        poseStack.translate(-0.4D * fovAdjust, -0.6D, -0.4D);
        double moveY = swing * 0.4D;
        double moveZ = swing * -0.2D;
        poseStack.translate(0, moveY, moveZ);
        PlayerRenderer playerRenderer = (PlayerRenderer) mc.getEntityRenderDispatcher().getRenderer(player);
        PlayerModel<AbstractClientPlayer> model = playerRenderer.getModel();
        poseStack.mulPose(Axis.XP.rotationDegrees(-40f));
        poseStack.mulPose(Axis.YP.rotationDegrees(20f));
        poseStack.mulPose(Axis.XP.rotationDegrees(swing * -30f));
        poseStack.mulPose(Axis.ZP.rotationDegrees(swing * 20f));
        model.leftArm.xRot = 0;
        model.leftArm.yRot = 0;
        model.leftArm.zRot = 0;
        model.leftArm.setPos(0, 0, 0);
        boolean oldVisible = model.leftArm.visible;
        boolean oldSleeveVisible = model.leftSleeve.visible;
        model.leftArm.visible = true;
        model.leftSleeve.visible = true;
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        int overlay = LivingEntityRenderer.getOverlayCoords(player, 0.0F);
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(player.getSkinTextureLocation()));
        float r = 1.0f, g = 1.0f, b = 1.0f;
        model.leftArm.render(poseStack, vertexConsumer, packedLight, overlay, r, g, b, 1.0f);
        model.leftSleeve.copyFrom(model.leftArm);
        model.leftSleeve.render(poseStack, vertexConsumer, packedLight, overlay, r, g, b, 1.0f);
        model.leftArm.visible = oldVisible;
        model.leftSleeve.visible = oldSleeveVisible;
        poseStack.popPose();
    }

    public enum ArmAnimType {
        NONE, PUNCH, TOSS
    }
}