package com.maxwell.highspeedlib.client.renderer;

import com.maxwell.highspeedlib.HighSpeedLib;
import com.maxwell.highspeedlib.api.main.mob.MobModeManager;
import com.maxwell.highspeedlib.client.state.UltraBossBarManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * カスタムボスバーの描画を担当するクラス。
 */
@Mod.EventBusSubscriber(modid = HighSpeedLib.MODID, value = Dist.CLIENT)
public class UltraBossBarRenderer {
    private static final Random RANDOM = new Random();
    private static final List<BossBarParticle> particles = new ArrayList<>();

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            // ボス選定ロジックの実行
            UltraBossBarManager.tick();
            
            // パーティクルの物理演算更新
            Iterator<BossBarParticle> it = particles.iterator();
            while (it.hasNext()) {
                BossBarParticle p = it.next();
                p.tick();
                if (p.life <= 0) it.remove();
            }
        }
    }

    @SubscribeEvent
    public static void onRenderHud(RenderGuiOverlayEvent.Pre event) {
        // Hotbar描画タイミングに合わせてボスバーを描画
        if (event.getOverlay().id().equals(VanillaGuiOverlay.HOTBAR.id())) {
            LivingEntity boss = UltraBossBarManager.getCurrentBoss();
            if (boss != null) {
                drawBossBar(event.getGuiGraphics(), boss);
            }
        }
    }

    private static void drawBossBar(GuiGraphics graphics, LivingEntity entity) {
        Minecraft mc = Minecraft.getInstance();
        int screenW = mc.getWindow().getGuiScaledWidth();
        
        float hpRatio = entity.getHealth() / entity.getMaxHealth();
        Component name = entity.getDisplayName();
        
        // 画面端手前まで伸ばす設定
        int margin = 40;
        int barW = screenW - (margin * 2);
        int barH = 5; // バーの太さ
        int x = margin;
        int y = 20;

        // 条件（Enraged状態）によるエフェクト判定
        boolean isEnraged = MobModeManager.isEnraged(entity);
        
        float offsetX = 0;
        float offsetY = 0;
        
        if (isEnraged) {
            // バー自体をランダムに揺らす（Jitter）
            offsetX = (RANDOM.nextFloat() - 0.5f) * 4f;
            offsetY = (RANDOM.nextFloat() - 0.5f) * 4f;
            
            // 赤いパーティクルの生成
            if (RANDOM.nextFloat() < 0.4f) {
                particles.add(new BossBarParticle(
                        x + RANDOM.nextFloat() * barW,
                        y + RANDOM.nextFloat() * barH
                ));
            }
        }

        graphics.pose().pushPose();
        graphics.pose().translate(offsetX, offsetY, 0);

        // 背景：一回り大きく薄い灰色のバー (0xAA333333)
        int bgPad = 2; // 背景の広がり
        graphics.fill(x - bgPad, y - bgPad, x + barW + bgPad, y + barH + bgPad, 0xAA333333);

        // 前景：赤色のHPバー (0xFFFF0000)
        int currentBarW = (int) (barW * hpRatio);
        if (currentBarW > 0) {
            graphics.fill(x, y, x + currentBarW, y + barH, 0xFFFF0000);
        }

        // エンティティの名前を中心（バーの中央上部）に表示
        int nameW = mc.font.width(name);
        int nameX = (screenW - nameW) / 2;
        graphics.drawString(mc.font, name, nameX, y - 12, 0xFFFFFFFF, true);

        // パーティクルの描画（バーの揺れと同期させるためposeの内部で描画）
        for (BossBarParticle p : particles) {
            // パーティクル自体も少し透過させて赤色に (0xCCFF0000)
            graphics.fill((int)p.x, (int)p.y, (int)(p.x + p.size), (int)(p.y + p.size), 0xCCFF0000);
        }

        graphics.pose().popPose();
    }

    /**
     * ボスバーから放出される簡易的なUIパーティクル。
     */
    private static class BossBarParticle {
        float x, y;
        float vx, vy;
        float size;
        int life;

        BossBarParticle(float x, float y) {
            this.x = x;
            this.y = y;
            // 左右に散りつつ、少し下に落ちるような動き
            this.vx = (RANDOM.nextFloat() - 0.5f) * 1.5f;
            this.vy = (RANDOM.nextFloat() * 1.0f) - 0.5f;
            this.size = 1f + RANDOM.nextFloat() * 2f;
            this.life = 15 + RANDOM.nextInt(10);
        }

        void tick() {
            x += vx;
            y += vy;
            vy += 0.05f; // ゆるやかな重力
            life--;
        }
    }
}
