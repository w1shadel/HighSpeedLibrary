package com.maxwell.highspeedlib.client.renderer;

import com.maxwell.highspeedlib.HighSpeedLib;
import com.maxwell.highspeedlib.client.state.TextData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = HighSpeedLib.MODID, value = Dist.CLIENT)
public class ClientTextRenderHandler {
    private static final List<TextData> activeTexts = new ArrayList<>();

    public static void addText(TextData data) {
        activeTexts.add(data);
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        GuiGraphics graphics = event.getGuiGraphics();
        Minecraft mc = Minecraft.getInstance();
        int sw = mc.getWindow().getGuiScaledWidth();
        int sh = mc.getWindow().getGuiScaledHeight();
        for (TextData data : activeTexts) {
            float alpha = Math.min(1.0f, (float) data.duration / 10.0f);
            int alphaInt = (int) (alpha * 255) << 24;
            int color = (data.color & 0x00FFFFFF) | alphaInt;
            graphics.pose().pushPose();
            if (data.type == TextData.Type.TITLE) {
                graphics.pose().translate(sw * data.x, sh * data.y, 0);
                graphics.pose().scale(data.scale, data.scale, 1.0f);
                int tw = mc.font.width(data.text);
                graphics.drawString(mc.font, data.text, -tw / 2 + 1, 1, 0xAA000000 | (alphaInt >> 8), false);
                graphics.drawString(mc.font, data.text, -tw / 2, 0, color, false);

            } else if (data.type == TextData.Type.SUBTITLE) {
                int tw = mc.font.width(data.text);
                int padding = 6;
                int bgWidth = tw + padding * 2;
                int bgHeight = 12;
                graphics.pose().translate(sw * data.x, sh * data.y, 0);
                graphics.pose().scale(data.scale, data.scale, 1.0f);
                graphics.fill(-(bgWidth / 2), -1, bgWidth / 2, bgHeight, 0x88000000 | (alphaInt >> 8));
                graphics.drawString(mc.font, data.text, -tw / 2, 1, color, false);
            }
            graphics.pose().popPose();
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            activeTexts.removeIf(text -> {
                text.tick();
                return text.isExpired();
            });
        }
    }
}