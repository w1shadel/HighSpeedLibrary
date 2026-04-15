package com.maxwell.highspeedlib.client.renderer;

import com.maxwell.highspeedlib.HighSpeedLib;
import com.maxwell.highspeedlib.api.config.HighSpeedClientConfig;
import com.maxwell.highspeedlib.common.logic.combat.ArmType;
import com.maxwell.highspeedlib.common.logic.combat.PunchCooldownManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

@SuppressWarnings("removal")
@Mod.EventBusSubscriber(modid = HighSpeedLib.MODID, value = Dist.CLIENT)
public class UltraHudRenderer {
    private static final ResourceLocation PUNCH1_ICON = new ResourceLocation(HighSpeedLib.MODID, "textures/gui/punch1.png");
    private static final ResourceLocation PUNCH2_ICON = new ResourceLocation(HighSpeedLib.MODID, "textures/gui/punch2.png");
    private static final float HUD_FULL_W = 152f;
    private static final float HUD_BASE_H = 150f;
    private static final float SCALE_LEFT = 1.0f;
    private static final float SCALE_RIGHT = 0.85f;
    private static final float TILT_UP_RIGHT = -22.0f;
    public static boolean dashUnlocked = true;
    public static boolean punchUnlocked = true;
    public static boolean whiplashUnlocked = true;
    public static boolean slidingUnlocked = true;
    public static boolean slamUnlocked = true;
    public static boolean walljumpUnlocked = true;
    private static double clientStamina = 3.0;
    private static float lastYaw = 0f;
    private static float lastPitch = 0f;
    private static float yawOffset = 0f;
    private static float pitchOffset = 0f;
    private static double clientMaxStamina = 3.0;
    private static float staminaColorLerp = 0f;
    private static double clientCoinStock = 4.0;
    private static int clientMaxCoins = 4;
    private static ArmType clientArm = ArmType.FEEDBACKER;

    public static void setClientCoinStock(double stock) {
        clientCoinStock = stock;
    }

    public static void setMaxCoins(int max) {
        clientMaxCoins = max;
    }

    public static void setClientStamina(double stamina, double maxStamina) {
        clientStamina = stamina;
        clientMaxStamina = maxStamina;
    }

    public static void setClientArm(ArmType arm) {
        clientArm = arm;
    }

    private static ResourceLocation getArmIcon(ArmType type) {
        return type == ArmType.FEEDBACKER ? PUNCH1_ICON : PUNCH2_ICON;
    }

    private static float getPerspectiveY(float x, float y) {
        float hudCenterY = HUD_BASE_H / 2f;
        float progress = x / HUD_FULL_W;
        float s = net.minecraft.util.Mth.lerp(progress, SCALE_LEFT, SCALE_RIGHT);
        float t = net.minecraft.util.Mth.lerp(progress, 0f, TILT_UP_RIGHT);
        return hudCenterY + (y - hudCenterY) * s + t;
    }

    @SubscribeEvent
    public static void onRenderHud(RenderGuiOverlayEvent.Pre event) {
        if (!HighSpeedClientConfig.ULTRAHUD_VISIBLE.get()) return;
        var id = event.getOverlay().id();
        if (id.equals(VanillaGuiOverlay.PLAYER_HEALTH.id()) ||
                id.equals(VanillaGuiOverlay.HOTBAR.id()) ||
                id.equals(VanillaGuiOverlay.FOOD_LEVEL.id()) ||
                id.equals(VanillaGuiOverlay.EXPERIENCE_BAR.id()) ||
                id.equals(VanillaGuiOverlay.ARMOR_LEVEL.id())) {
            event.setCanceled(true);
        }
        if (id.equals(VanillaGuiOverlay.HOTBAR.id())) {
            renderUltraHud(event.getGuiGraphics());
        }
    }

    private static void renderUltraHud(GuiGraphics graphics) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui || !HighSpeedClientConfig.ULTRAHUD_VISIBLE.get()) return;
        Player player = mc.player;
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        float partialTick = mc.getPartialTick();
        float yawDiff = player.getYRot() - lastYaw;
        float pitchDiff = player.getXRot() - lastPitch;
        if (Math.abs(yawDiff) > 180) yawDiff = 0;
        yawOffset = net.minecraft.util.Mth.lerp(0.15f * partialTick, yawOffset, net.minecraft.util.Mth.clamp(-yawDiff * 1.5f, -15f, 15f));
        pitchOffset = net.minecraft.util.Mth.lerp(0.15f * partialTick, pitchOffset, net.minecraft.util.Mth.clamp(pitchDiff * 1.5f, -10f, 10f));
        lastYaw = player.getYRot();
        lastPitch = player.getXRot();
        float totalWidth = 124f;
        float hudHeight = 150f;
        float hudLeft = HighSpeedClientConfig.HUD_OFFSET_X.get().floatValue();
        float hudTop = screenHeight - HighSpeedClientConfig.HUD_OFFSET_Y.get().floatValue();
        float centerX = hudLeft + (totalWidth / 2.0f);
        float centerY = hudTop + (hudHeight / 2.0f);
        graphics.pose().pushPose();
        graphics.pose().translate(centerX + yawOffset - (totalWidth / 2.0f), centerY + pitchOffset - (hudHeight / 2.0f), 0);
        float zBg = 0.0f;
        float zBar = 0.01f;
        float zTop = 0.02f;
        float gap = 1f;
        float currentY = 0f;
        ItemStack heldItem = player.getMainHandItem();
        float boxHeight = 60f;
        drawTrapezoid(graphics, 0, currentY, totalWidth, boxHeight, 0xAA222222, zBg);
        if (!heldItem.isEmpty()) {
            drawItemWithPerspective(graphics, heldItem, totalWidth / 2.0f, currentY + (boxHeight / 2.0f), zTop + 0.5f);
        }
        currentY += boxHeight + gap;
        float hpHeight = 14f;
        float hpY = currentY;
        float POINTS_PER_HP = 5.0f;
        float currentHP = player.getHealth();
        float absorption = player.getAbsorptionAmount();
        int displayHP = (int) ((currentHP + absorption) * POINTS_PER_HP);
        drawTrapezoid(graphics, 0, currentY, totalWidth, hpHeight, 0xAA222222, zBg);
        float barPadding = 2f;
        float barW = totalWidth - (barPadding * 2);
        float hpRatio = Math.min(1.0f, currentHP / 20.0f);
        if (hpRatio > 0)
            drawTrapezoid(graphics, barPadding, currentY + barPadding, hpRatio * barW, hpHeight - (barPadding * 2), 0xFFFF2222, zBar);
        float overRatio = Math.min(1.0f, absorption / 20.0f);
        if (overRatio > 0)
            drawTrapezoid(graphics, barPadding, currentY + barPadding, overRatio * barW, hpHeight - (barPadding * 2), 0xFF44FF44, zTop);
        drawTextWithPerspective(graphics, mc, String.valueOf(displayHP), 5, currentY + 3, 0xFFFFFFFF, zTop + 0.1f);
        currentY += hpHeight + gap;
        float actualStaminaH = 0f;
        if (dashUnlocked) {
            float staminaHeight = 10f;
            float targetColorLerp = clientStamina <= 1.0 ? 1.0f : 0.0f;
            staminaColorLerp = net.minecraft.util.Mth.lerp(0.25f * partialTick, staminaColorLerp, targetColorLerp);
            int r_s = (int) net.minecraft.util.Mth.lerp(staminaColorLerp, 0x66, 0xFF);
            int g_s = (int) net.minecraft.util.Mth.lerp(staminaColorLerp, 0xEE, 0x44);
            int b_s = (int) net.minecraft.util.Mth.lerp(staminaColorLerp, 0xFF, 0x44);
            int currentStaminaColor = 0xFF000000 | (r_s << 16) | (g_s << 8) | b_s;
            int segments = (int) clientMaxStamina;
            if (segments <= 0) segments = 3;
            float staminaHPadding = 2f;
            float innerSpacing = 1.0f;
            float availableStaminaW = totalWidth - (staminaHPadding * 2);
            float segWidth = (availableStaminaW - (innerSpacing * (segments - 1))) / segments;
            drawTrapezoid(graphics, 0, currentY, totalWidth, staminaHeight, 0xAA222222, zBg);
            for (int i = 0; i < segments; i++) {
                float sx = staminaHPadding + (i * (segWidth + innerSpacing));
                int bgColor = 0x33000000 | (r_s / 4 << 16) | (g_s / 4 << 8) | b_s / 4;
                drawTrapezoid(graphics, sx, currentY + 2f, segWidth, staminaHeight - 4f, bgColor, zBar);
                float ratio = (float) Math.max(0, Math.min(1.0, clientStamina - i));
                if (ratio > 0)
                    drawTrapezoid(graphics, sx, currentY + 2f, ratio * segWidth, staminaHeight - 4f, currentStaminaColor, zTop);
            }
            actualStaminaH = staminaHeight;
            currentY += staminaHeight + gap;
        }
        if (punchUnlocked) {
            float armSectionX = totalWidth + 4f;
            float armSectionW = 24f;
            float armSectionH = hpHeight + (dashUnlocked ? gap + actualStaminaH : 0);
            drawTrapezoid(graphics, armSectionX, hpY, armSectionW, armSectionH, 0xAA222222, zBg);
            float iconSize = 20f;
            float iconX = armSectionX + (armSectionW - iconSize) / 2f;
            float iconY = hpY + (armSectionH - iconSize) / 2f;
            drawTextureWithPerspective(graphics, getArmIcon(clientArm), iconX, iconY, iconSize, iconSize, zTop + 1.0f);
            double currentEnergy = PunchCooldownManager.getEnergy(player);
            float cooldownRatio = (float) Math.min(1.0, currentEnergy / 2.0);
            if (cooldownRatio < 1.0f) {
                float overlayH = (1.0f - cooldownRatio) * iconSize;
                drawTrapezoid(graphics, iconX, iconY, iconSize, overlayH, 0x88444444, zTop + 1.1f);
            }
            float dotSize = 4f;
            float dotGap = 2f;
            float startX = armSectionX + (armSectionW / 2f) - ((clientMaxCoins * (dotSize + dotGap) - dotGap) / 2f);
            float dotY = hpY + armSectionH - 2f;
            for (int i = 0; i < clientMaxCoins; i++) {
                float dx = startX + (i * (dotSize + dotGap));
                drawTrapezoid(graphics, dx, dotY, dotSize, dotSize, 0xAA222222, zTop + 0.1f);
                if (clientCoinStock >= i + 1) {
                    drawTrapezoid(graphics, dx, dotY, dotSize, dotSize, 0xFFFFFF00, zTop + 0.2f);
                } else if (clientCoinStock > i) {
                    float chargeRatio = (float) (clientCoinStock - i);
                    float progressH = dotSize * chargeRatio;
                    drawTrapezoid(graphics, dx, dotY + (dotSize - progressH), dotSize, progressH, 0x88AAAA00, zTop + 0.2f);
                }
            }
        }
        float foodHeight = 6f;
        drawTrapezoid(graphics, 0, currentY, totalWidth, foodHeight, 0xAA222222, zBg);
        float foodHPadding = 2f;
        float foodFillW = (player.getFoodData().getFoodLevel() / 20.0f) * (totalWidth - (foodHPadding * 2));
        if (foodFillW > 0)
            drawTrapezoid(graphics, foodHPadding, currentY + 1f, foodFillW, foodHeight - 2f, 0xFFFF8800, zBar);
        currentY += foodHeight + gap;
        float expHeight = 10f;
        drawTrapezoid(graphics, 0, currentY, totalWidth, expHeight, 0xAA222222, zBg);
        float expHPadding = 2f;
        float expFillW = player.experienceProgress * (totalWidth - (expHPadding * 2));
        if (expFillW > 0)
            drawTrapezoid(graphics, expHPadding, currentY + 1f, expFillW, expHeight - 2f, 0xFF00FF00, zBar);
        if (player.experienceLevel > 0) {
            String levelStr = String.valueOf(player.experienceLevel);
            float textX = (totalWidth / 2f) - (mc.font.width(levelStr) / 2f);
            drawTextWithPerspective(graphics, mc, levelStr, textX, currentY + (expHeight / 2f) - 4.5f, 0xFFFFFFFF, zTop + 0.1f);
        }
        graphics.pose().popPose();
    }

    private static void drawTrapezoid(GuiGraphics graphics, float x, float y, float w, float h, int color, float z) {
        float yTopL = getPerspectiveY(x, y);
        float yBotL = getPerspectiveY(x, y + h);
        float yTopR = getPerspectiveY(x + w, y);
        float yBotR = getPerspectiveY(x + w, y + h);
        float a = (float) (color >> 24 & 255) / 255.0F;
        float r = (float) (color >> 16 & 255) / 255.0F;
        float g = (float) (color >> 8 & 255) / 255.0F;
        float b = (float) (color & 255) / 255.0F;
        Matrix4f matrix = graphics.pose().last().pose();
        com.mojang.blaze3d.vertex.VertexConsumer builder = graphics.bufferSource().getBuffer(net.minecraft.client.renderer.RenderType.gui());
        builder.vertex(matrix, x, yBotL, z).color(r, g, b, a).endVertex();
        builder.vertex(matrix, x + w, yBotR, z).color(r, g, b, a).endVertex();
        builder.vertex(matrix, x + w, yTopR, z).color(r, g, b, a).endVertex();
        builder.vertex(matrix, x, yTopL, z).color(r, g, b, a).endVertex();
    }

    private static void drawTextWithPerspective(GuiGraphics graphics, Minecraft mc, String text, float x, float y, int color, float z) {
        float progress = x / HUD_FULL_W;
        float scale = net.minecraft.util.Mth.lerp(progress, SCALE_LEFT, SCALE_RIGHT);
        float yPos = getPerspectiveY(x, y);
        float yNext = getPerspectiveY(x + 10f, y);
        float angle = (float) Math.toDegrees(Math.atan2(yNext - yPos, 10f));
        graphics.pose().pushPose();
        graphics.pose().translate(x, yPos, z);
        graphics.pose().scale(scale, scale, 1.0f);
        graphics.pose().mulPose(com.mojang.math.Axis.ZP.rotationDegrees(angle));
        graphics.drawString(mc.font, text, 0, 0, color, false);
        graphics.pose().popPose();
    }

    private static void drawItemWithPerspective(GuiGraphics graphics, ItemStack heldItem, float x, float y, float z) {
        float progress = x / HUD_FULL_W;
        float scale = net.minecraft.util.Mth.lerp(progress, SCALE_LEFT, SCALE_RIGHT);
        float yPos = getPerspectiveY(x, y);
        float yNext = getPerspectiveY(x + 10f, y);
        float angle = (float) Math.toDegrees(Math.atan2(yNext - yPos, 10f));
        graphics.pose().pushPose();
        graphics.pose().translate(x, yPos, z);
        graphics.pose().scale(3.0f * scale, 3.0f * scale, 1.0f);
        graphics.pose().mulPose(com.mojang.math.Axis.ZP.rotationDegrees(angle));
        graphics.renderItem(heldItem, -8, -8);
        graphics.pose().popPose();
    }

    private static void drawTextureWithPerspective(GuiGraphics graphics, ResourceLocation texture, float x, float y, float w, float h, float z) {
        float yTopL = getPerspectiveY(x, y);
        float yBotL = getPerspectiveY(x, y + h);
        float yTopR = getPerspectiveY(x + w, y);
        float yBotR = getPerspectiveY(x + w, y + h);
        graphics.flush();
        RenderSystem.setShader(net.minecraft.client.renderer.GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        Matrix4f matrix = graphics.pose().last().pose();
        com.mojang.blaze3d.vertex.Tesselator tesselator = com.mojang.blaze3d.vertex.Tesselator.getInstance();
        com.mojang.blaze3d.vertex.BufferBuilder builder = tesselator.getBuilder();
        builder.begin(com.mojang.blaze3d.vertex.VertexFormat.Mode.QUADS, com.mojang.blaze3d.vertex.DefaultVertexFormat.POSITION_TEX);
        builder.vertex(matrix, x, yTopL, z).uv(0, 0).endVertex();
        builder.vertex(matrix, x, yBotL, z).uv(0, 1).endVertex();
        builder.vertex(matrix, x + w, yBotR, z).uv(1, 1).endVertex();
        builder.vertex(matrix, x + w, yTopR, z).uv(1, 0).endVertex();
        tesselator.end();
        RenderSystem.disableBlend();
    }
}