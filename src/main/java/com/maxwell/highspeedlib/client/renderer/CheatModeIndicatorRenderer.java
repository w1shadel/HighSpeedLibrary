package com.maxwell.highspeedlib.client.renderer;

import com.maxwell.highspeedlib.HighSpeedLib;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = HighSpeedLib.MODID, value = Dist.CLIENT)
public class CheatModeIndicatorRenderer {
    private static final int[] KONAMI_CODE = {
            GLFW.GLFW_KEY_UP, GLFW.GLFW_KEY_UP,
            GLFW.GLFW_KEY_DOWN, GLFW.GLFW_KEY_DOWN,
            GLFW.GLFW_KEY_LEFT, GLFW.GLFW_KEY_RIGHT,
            GLFW.GLFW_KEY_LEFT, GLFW.GLFW_KEY_RIGHT,
            GLFW.GLFW_KEY_B, GLFW.GLFW_KEY_A
    };
    private static final List<Integer> inputBuffer = new ArrayList<>();
    private static boolean fakeCheatEnabled = false;
    private static float animationTimer = 0f;

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (event.getAction() != GLFW.GLFW_PRESS) return;
        inputBuffer.add(event.getKey());
        if (inputBuffer.size() > KONAMI_CODE.length) {
            inputBuffer.remove(0);
        }
        if (checkKonamiCode()) {
            fakeCheatEnabled = !fakeCheatEnabled;
            inputBuffer.clear();
            Minecraft.getInstance().player.playSound(
                    fakeCheatEnabled ? SoundEvents.CONDUIT_AMBIENT : SoundEvents.ENCHANTMENT_TABLE_USE,
                    1.0f, 1.0f
            );
        }
    }

    private static boolean checkKonamiCode() {
        if (inputBuffer.size() != KONAMI_CODE.length) return false;
        for (int i = 0; i < KONAMI_CODE.length; i++) {
            if (inputBuffer.get(i) != KONAMI_CODE[i]) return false;
        }
        return true;
    }

    @SubscribeEvent
    public static void onRenderHud(RenderGuiOverlayEvent.Post event) {
        if (Minecraft.getInstance().screen == null) {
            renderIndicator(event.getGuiGraphics());
        }
    }

    @SubscribeEvent
    public static void onRenderScreen(ScreenEvent.Render.Post event) {
        renderIndicator(event.getGuiGraphics());
    }

    private static void renderIndicator(GuiGraphics graphics) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui || !com.maxwell.highspeedlib.api.config.HighSpeedClientConfig.CHEAT_INDICATOR_VISIBLE.get())
            return;
        boolean isReal = mc.player.isCreative();
        if (!isReal && !fakeCheatEnabled) return;
        animationTimer += 0.05f;
        String text = isReal ? "Cheat Enabled" : "Fake Cheat Enabled";
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int textWidth = mc.font.width(text);
        int padding = 6;
        int x = screenWidth - textWidth - padding - 10;
        int y = 10;
        float pulse = (float) Math.sin(animationTimer) * 0.5f + 0.5f;
        int alpha = (int) (200 + pulse * 55);
        int color = isReal ? 0xFFFFAA00 : 0xFFAAAAAA;
        graphics.pose().pushPose();
        graphics.fill(x - padding, y - padding, x + textWidth + padding, y + mc.font.lineHeight + padding, 0xAA000000);
        int borderColor = (alpha << 24) | (color & 0xFFFFFF);
        drawOutlinedBox(graphics, x - padding, y - padding, textWidth + padding * 2, mc.font.lineHeight + padding * 2, borderColor);
        graphics.drawString(mc.font, text, x, y, borderColor, true);
        graphics.pose().popPose();
    }

    private static void drawOutlinedBox(GuiGraphics graphics, int x, int y, int w, int h, int color) {
        graphics.fill(x, y, x + w, y + 1, color);
        graphics.fill(x, y + h - 1, x + w, y + h, color);
        graphics.fill(x, y, x + 1, y + h, color);
        graphics.fill(x + w - 1, y, x + w, y + h, color);
    }
}
