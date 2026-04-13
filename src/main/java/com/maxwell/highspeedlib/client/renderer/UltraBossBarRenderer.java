package com.maxwell.highspeedlib.client.renderer;

import com.maxwell.highspeedlib.HighSpeedLib;
import com.maxwell.highspeedlib.api.main.mob.MobModeManager;
import com.maxwell.highspeedlib.client.state.UltraBossBarManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = HighSpeedLib.MODID, value = Dist.CLIENT)
public class UltraBossBarRenderer {
    private static final List<UIParticle> particles = new ArrayList<>();

    public static void tick() {
        particles.removeIf(p -> {
            p.life--;
            p.x += p.vx;
            p.y += p.vy;
            return p.life <= 0;
        });
    }

    @SubscribeEvent
    public static void onRenderBossBar(RenderGuiOverlayEvent.Pre event) {
        if (!event.getOverlay().id().equals(VanillaGuiOverlay.BOSS_EVENT_PROGRESS.id())) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || mc.options.hideGui) return;
        List<LivingEntity> bosses = UltraBossBarManager.getActiveBosses();
        if (bosses.isEmpty()) {
            particles.clear();
            return;
        }
        GuiGraphics graphics = event.getGuiGraphics();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        float barWidth = screenWidth * 0.85f;
        float barHeight = 12f;
        float bgPadding = 4f;
        float bgWidth = barWidth + bgPadding * 2;
        float bgHeight = barHeight + bgPadding * 2;
        float startY = 12f;
        float spacing = bgHeight + 6f;
        for (int i = 0; i < bosses.size(); i++) {
            LivingEntity boss = bosses.get(i);
            float y = startY + (i * spacing);
            float x = (screenWidth - barWidth) / 2f;
            drawBossBar(graphics, mc, boss, x, y, barWidth, barHeight, bgPadding);
        }
        for (UIParticle p : particles) {
            float alpha = (float) p.life / p.maxLife;
            int a = (int) (alpha * 255) << 24;
            graphics.fill((int) p.x, (int) p.y, (int) (p.x + 3), (int) (p.y + 3), a | 0xFF2222);
        }
    }

    private static void drawBossBar(GuiGraphics graphics, Minecraft mc, LivingEntity boss, float x, float y, float w, float h, float padding) {
        float hpRatio = boss.getHealth() / boss.getMaxHealth();
        if (MobModeManager.isEnraged(boss)) {
            float shakeX = (mc.level.random.nextFloat() - 0.5f) * 4f;
            float shakeY = (mc.level.random.nextFloat() - 0.5f) * 4f;
            x += shakeX;
            y += shakeY;
            if (mc.level.random.nextFloat() < 0.5f) {
                float px = x + mc.level.random.nextFloat() * w;
                float py = y + mc.level.random.nextFloat() * h;
                float vx = (mc.level.random.nextFloat() - 0.5f) * 2f;
                float vy = (mc.level.random.nextFloat() - 0.5f) * 5f - 1f;
                particles.add(new UIParticle(px, py, vx, vy, 15 + mc.level.random.nextInt(15)));
            }
        }
        graphics.fill((int) (x - padding), (int) (y - padding), (int) (x + w + padding), (int) (y + h + padding), 0xAA444444);
        graphics.fill((int) x, (int) y, (int) (x + w), (int) (y + h), 0xFF222222);
        if (hpRatio > 0) {
            graphics.fill((int) x, (int) y, (int) (x + w * hpRatio), (int) (y + h), 0xFFFF2222);
        }
        String name = boss.getDisplayName().getString();
        float textX = x + (w / 2f) - (mc.font.width(name) / 2f);
        float textY = y + (h / 2f) - (mc.font.lineHeight / 2f) + 1;
        graphics.drawString(mc.font, name, (int) textX, (int) textY, 0xFFFFFFFF, true);
    }

    static class UIParticle {
        float x, y, vx, vy;
        int life, maxLife;

        UIParticle(float x, float y, float vx, float vy, int life) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.life = life;
            this.maxLife = life;
        }
    }
}
