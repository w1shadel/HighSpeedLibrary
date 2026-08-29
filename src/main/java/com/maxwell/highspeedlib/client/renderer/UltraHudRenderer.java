package com.maxwell.highspeedlib.client.renderer;

import com.maxwell.highspeedlib.HighSpeedLib;
import com.maxwell.highspeedlib.api.config.HighSpeedClientConfig;
import com.maxwell.highspeedlib.common.logic.combat.ArmType;
import com.maxwell.highspeedlib.common.logic.combat.PunchCooldownManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import org.joml.Matrix4f;

@SuppressWarnings("removal")
@EventBusSubscriber(modid = HighSpeedLib.MODID, value = Dist.CLIENT)
public class UltraHudRenderer {
    private static final ResourceLocation PUNCH1_ICON = ResourceLocation.fromNamespaceAndPath(HighSpeedLib.MODID, "textures/gui/punch1.png");
    private static final ResourceLocation PUNCH2_ICON = ResourceLocation.fromNamespaceAndPath(HighSpeedLib.MODID, "textures/gui/punch2.png");
    private static final float Z_BG = 0.0f;
    private static final float Z_BAR = 5.0f;
    private static final float Z_TOP = 10.0f;
    private static final float Z_ITEM = 20.0f;
    private static final float Z_ICON = 30.0f;
    private static final float HUD_FULL_W = 152f;
    private static final float HUD_BASE_H = 150f;
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
        float scale = net.minecraft.util.Mth.lerp(progress, HighSpeedClientConfig.HUD_SCALE_LEFT.get().floatValue(), HighSpeedClientConfig.HUD_SCALE_RIGHT.get().floatValue());
        float tilt = net.minecraft.util.Mth.lerp(progress, 0f, HighSpeedClientConfig.HUD_TILT.get().floatValue());
        return hudCenterY + (y - hudCenterY) * scale + tilt;
    }

    @SubscribeEvent
    public static void onRenderHud(RenderGuiLayerEvent.Pre event) {
        if (!HighSpeedClientConfig.ULTRAHUD_VISIBLE.get()) return;
        var id = event.getName();
        if (id.equals(VanillaGuiLayers.PLAYER_HEALTH) ||
                id.equals(VanillaGuiLayers.HOTBAR) ||
                id.equals(VanillaGuiLayers.FOOD_LEVEL) ||
                id.equals(VanillaGuiLayers.EXPERIENCE_BAR) ||
                id.equals(VanillaGuiLayers.ARMOR_LEVEL)) {
            event.setCanceled(true);
        }
        if (id.equals(VanillaGuiLayers.HOTBAR)) {
            renderUltraHud(event.getGuiGraphics(), event.getPartialTick().getGameTimeDeltaPartialTick(false));
        }
    }

    private static void renderUltraHud(GuiGraphics graphics, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;
        Player player = mc.player;
        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        float yawDiff = player.getYRot() - lastYaw;
        float pitchDiff = player.getXRot() - lastPitch;
        if (Math.abs(yawDiff) > 180) yawDiff = 0;
        float yawSens = HighSpeedClientConfig.HUD_YAW_SENSITIVITY.get().floatValue();
        float pitchSens = HighSpeedClientConfig.HUD_PITCH_SENSITIVITY.get().floatValue();
        float maxYawOff = HighSpeedClientConfig.HUD_MAX_OFFSET_YAW.get().floatValue();
        float maxPitchOff = HighSpeedClientConfig.HUD_MAX_OFFSET_PITCH.get().floatValue();
        yawOffset = net.minecraft.util.Mth.lerp(0.15f * partialTick, yawOffset, net.minecraft.util.Mth.clamp(-yawDiff * yawSens, -maxYawOff, maxYawOff));
        pitchOffset = net.minecraft.util.Mth.lerp(0.15f * partialTick, pitchOffset, net.minecraft.util.Mth.clamp(pitchDiff * pitchSens, -maxPitchOff, maxPitchOff));
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
        float gap = 1f;
        float currentY = 0f;
        ItemStack heldItem = player.getMainHandItem();
        float boxHeight = 60f;
        drawTrapezoid(graphics, 0, currentY, totalWidth, boxHeight, 0xAA222222, Z_BG);
        if (!heldItem.isEmpty()) {
            drawItemWithPerspective(graphics, heldItem, totalWidth / 2.0f, currentY + (boxHeight / 2.0f), Z_ITEM);
        }
        currentY += boxHeight + gap;
        float hpHeight = 14f;
        float hpY = currentY;
        float pointsPerHP = HighSpeedClientConfig.HUD_POINTS_PER_HP.get().floatValue();
        float currentHP = player.getHealth();
        float absorption = player.getAbsorptionAmount();
        int displayHP = (int) ((currentHP + absorption) * pointsPerHP);
        drawTrapezoid(graphics, 0, currentY, totalWidth, hpHeight, 0xAA222222, Z_BG);
        float barPadding = 2f;
        float barW = totalWidth - (barPadding * 2);
        int colorHP = HighSpeedClientConfig.getColor(HighSpeedClientConfig.HUD_COLOR_HP);
        int colorAbs = HighSpeedClientConfig.getColor(HighSpeedClientConfig.HUD_COLOR_ABSORPTION);
        if (currentHP > 0)
            drawTrapezoid(graphics, barPadding, currentY + barPadding, Math.min(1.0f, currentHP / 20.0f) * barW, hpHeight - (barPadding * 2), colorHP, Z_BAR);
        if (absorption > 0)
            drawTrapezoid(graphics, barPadding, currentY + barPadding, Math.min(1.0f, absorption / 20.0f) * barW, hpHeight - (barPadding * 2), colorAbs, Z_TOP);
        drawTextWithPerspective(graphics, mc, String.valueOf(displayHP), 5, currentY + 3, 0xFFFFFFFF, Z_TOP);
        currentY += hpHeight + gap;
        float actualStaminaH = 0f;
        if (dashUnlocked) {
            float staminaHeight = 10f;
            float targetColorLerp = clientStamina <= 1.0 ? 1.0f : 0.0f;
            staminaColorLerp = net.minecraft.util.Mth.lerp(0.25f * partialTick, staminaColorLerp, targetColorLerp);
            int colorNormal = HighSpeedClientConfig.getColor(HighSpeedClientConfig.HUD_COLOR_STAMINA_NORMAL);
            int colorLow = HighSpeedClientConfig.getColor(HighSpeedClientConfig.HUD_COLOR_STAMINA_LOW);
            int r_n = (colorNormal >> 16) & 0xFF;
            int g_n = (colorNormal >> 8) & 0xFF;
            int b_n = colorNormal & 0xFF;
            int r_l = (colorLow >> 16) & 0xFF;
            int g_l = (colorLow >> 8) & 0xFF;
            int b_l = colorLow & 0xFF;
            int r_s = (int) net.minecraft.util.Mth.lerp(staminaColorLerp, r_n, r_l);
            int g_s = (int) net.minecraft.util.Mth.lerp(staminaColorLerp, g_n, g_l);
            int b_s = (int) net.minecraft.util.Mth.lerp(staminaColorLerp, b_n, b_l);
            int currentStaminaColor = 0xFF000000 | (r_s << 16) | (g_s << 8) | b_s;
            int segments = (int) Math.max(1, clientMaxStamina);
            float staminaHPadding = 2f;
            float innerSpacing = 1.0f;
            float availableStaminaW = totalWidth - (staminaHPadding * 2);
            float segWidth = (availableStaminaW - (innerSpacing * (segments - 1))) / segments;
            drawTrapezoid(graphics, 0, currentY, totalWidth, staminaHeight, 0xAA222222, Z_BG);
            for (int i = 0; i < segments; i++) {
                float sx = staminaHPadding + (i * (segWidth + innerSpacing));
                drawTrapezoid(graphics, sx, currentY + 2f, segWidth, staminaHeight - 4f, 0x33000000, Z_BAR);
                float ratio = (float) Math.max(0, Math.min(1.0, clientStamina - i));
                if (ratio > 0)
                    drawTrapezoid(graphics, sx, currentY + 2f, ratio * segWidth, staminaHeight - 4f, currentStaminaColor, Z_TOP);
            }
            actualStaminaH = staminaHeight;
            currentY += staminaHeight + gap;
        }
        float armSectionX = totalWidth + 4f;
        float armSectionW = 24f;
        float armSectionH = hpHeight + (dashUnlocked ? gap + actualStaminaH : 0);
        if (punchUnlocked) {
            drawTrapezoid(graphics, armSectionX, hpY, armSectionW, armSectionH, 0xAA222222, Z_BG);
            float iconSize = 20f;
            float iconX = armSectionX + (armSectionW - iconSize) / 2f;
            float iconY = hpY + (armSectionH - iconSize) / 2f;
            drawTextureWithPerspective(graphics, getArmIcon(clientArm), iconX, iconY, iconSize, iconSize, Z_ICON);
            double currentEnergy = PunchCooldownManager.getEnergy(player);
            float cooldownRatio = (float) Math.min(1.0, currentEnergy / 2.0);
            if (cooldownRatio < 1.0f) {
                float overlayH = (1.0f - cooldownRatio) * iconSize;
                drawTrapezoid(graphics, iconX, iconY, iconSize, overlayH, 0x88444444, Z_ICON + 1);
            }
            float dotSize = 4f;
            float dotGap = 2f;
            float startX = armSectionX + (armSectionW / 2f) - ((clientMaxCoins * (dotSize + dotGap) - dotGap) / 2f);
            float dotY = hpY + armSectionH - 6f;
            int colorCoin = HighSpeedClientConfig.getColor(HighSpeedClientConfig.HUD_COLOR_COIN);
            for (int i = 0; i < clientMaxCoins; i++) {
                float dx = startX + (i * (dotSize + dotGap));
                drawTrapezoid(graphics, dx, dotY, dotSize, dotSize, 0xAA222222, Z_TOP);
                if (clientCoinStock >= i + 1) {
                    drawTrapezoid(graphics, dx, dotY, dotSize, dotSize, colorCoin, Z_TOP + 1);
                } else if (clientCoinStock > i) {
                    float chargeRatio = (float) (clientCoinStock - i);
                    int dimmedCoin = (colorCoin & 0xFFFFFF) | 0x88000000;
                    drawTrapezoid(graphics, dx, dotY + (dotSize - (dotSize * chargeRatio)), dotSize, dotSize * chargeRatio, dimmedCoin, Z_TOP + 1);
                }
            }
        }
        int slotsToShow = HighSpeedClientConfig.HUD_SLOTS_TO_SHOW.get();
        float slotSize = 20f;
        float slotGap = 2f;
        for (int i = 1; i <= slotsToShow; i++) {
            int nextIndex = (player.getInventory().selected + i) % 9;
            ItemStack nextStack = player.getInventory().getItem(nextIndex);
            float slotY = hpY - (i * (slotSize + slotGap));
            drawTrapezoid(graphics, armSectionX, slotY, armSectionW, slotSize, 0x88111111, Z_BG);
            if (!nextStack.isEmpty()) {
                drawSmallItemWithPerspective(graphics, mc, nextStack, armSectionX + (armSectionW / 2f), slotY + (slotSize / 2f), Z_ITEM);
            }
        }
        float foodHeight = 6f;
        drawTrapezoid(graphics, 0, currentY, totalWidth, foodHeight, 0xAA222222, Z_BG);
        float foodHPadding = 2f;
        float foodFillW = (player.getFoodData().getFoodLevel() / 20.0f) * (totalWidth - (foodHPadding * 2));
        int colorFood = HighSpeedClientConfig.getColor(HighSpeedClientConfig.HUD_COLOR_FOOD);
        if (foodFillW > 0)
            drawTrapezoid(graphics, foodHPadding, currentY + 1f, foodFillW, foodHeight - 2f, colorFood, Z_BAR);
        currentY += foodHeight + gap;
        float expHeight = 10f;
        drawTrapezoid(graphics, 0, currentY, totalWidth, expHeight, 0xAA222222, Z_BG);
        float expHPadding = 2f;
        float expFillW = player.experienceProgress * (totalWidth - (expHPadding * 2));
        int colorExp = HighSpeedClientConfig.getColor(HighSpeedClientConfig.HUD_COLOR_EXP);
        if (expFillW > 0)
            drawTrapezoid(graphics, expHPadding, currentY + 1f, expFillW, expHeight - 2f, colorExp, Z_BAR);
        if (player.experienceLevel > 0) {
            String levelStr = String.valueOf(player.experienceLevel);
            drawTextWithPerspective(graphics, mc, levelStr, (totalWidth / 2f) - (mc.font.width(levelStr) / 2f), currentY + 1, 0xFFFFFFFF, Z_TOP);
        }
        graphics.flush();
        RenderSystem.disableDepthTest();
        graphics.pose().popPose();
    }

    private static void drawTrapezoid(GuiGraphics graphics, float x, float y, float w, float h, int color, float z) {
        float yTopL = getPerspectiveY(x, y);
        float yBotL = getPerspectiveY(x, y + h);
        float yTopR = getPerspectiveY(x + w, y);
        float yBotR = getPerspectiveY(x + w, y + h);
        int a = (color >> 24) & 0xFF;
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        Matrix4f matrix = graphics.pose().last().pose();
        VertexConsumer builder = graphics.bufferSource().getBuffer(RenderType.gui());
        builder.addVertex(matrix, x, yBotL, z).setColor(r, g, b, a);
        builder.addVertex(matrix, x + w, yBotR, z).setColor(r, g, b, a);
        builder.addVertex(matrix, x + w, yTopR, z).setColor(r, g, b, a);
        builder.addVertex(matrix, x, yTopL, z).setColor(r, g, b, a);
    }

    private static void drawTextWithPerspective(GuiGraphics graphics, Minecraft mc, String text, float x, float y, int color, float z) {
        float progress = x / HUD_FULL_W;
        float scale = net.minecraft.util.Mth.lerp(progress, HighSpeedClientConfig.HUD_SCALE_LEFT.get().floatValue(), HighSpeedClientConfig.HUD_SCALE_RIGHT.get().floatValue());
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

    private static void drawSmallItemWithPerspective(GuiGraphics graphics, Minecraft mc, ItemStack item, float x, float y, float z) {
        float progress = x / HUD_FULL_W;
        float scale = net.minecraft.util.Mth.lerp(progress, HighSpeedClientConfig.HUD_SCALE_LEFT.get().floatValue(), HighSpeedClientConfig.HUD_SCALE_RIGHT.get().floatValue());
        float yPos = getPerspectiveY(x, y);
        float yNext = getPerspectiveY(x + 10f, y);
        float angle = (float) Math.toDegrees(Math.atan2(yNext - yPos, 10f));
        graphics.pose().pushPose();
        graphics.pose().translate(x, yPos, z);
        graphics.pose().scale(1.2f * scale, 1.2f * scale, 1.0f);
        graphics.pose().mulPose(com.mojang.math.Axis.ZP.rotationDegrees(angle));
        graphics.renderItem(item, -8, -8);
        graphics.pose().popPose();
    }

    private static void drawItemWithPerspective(GuiGraphics graphics, ItemStack heldItem, float x, float y, float z) {
        float progress = x / HUD_FULL_W;
        float scale = net.minecraft.util.Mth.lerp(progress, HighSpeedClientConfig.HUD_SCALE_LEFT.get().floatValue(), HighSpeedClientConfig.HUD_SCALE_RIGHT.get().floatValue());
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
        VertexConsumer builder = graphics.bufferSource().getBuffer(RenderType.text(texture));
        Matrix4f matrix = graphics.pose().last().pose();
        builder.addVertex(matrix, x, yTopL, z).setColor(255, 255, 255, 255).setUv(0, 0).setLight(15728880);
        builder.addVertex(matrix, x, yBotL, z).setColor(255, 255, 255, 255).setUv(0, 1).setLight(15728880);
        builder.addVertex(matrix, x + w, yBotR, z).setColor(255, 255, 255, 255).setUv(1, 1).setLight(15728880);
        builder.addVertex(matrix, x + w, yTopR, z).setColor(255, 255, 255, 255).setUv(1, 0).setLight(15728880);
    }

}
