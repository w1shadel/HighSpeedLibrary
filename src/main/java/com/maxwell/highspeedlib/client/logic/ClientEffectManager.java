package com.maxwell.highspeedlib.client.logic;

import com.maxwell.highspeedlib.HighSpeedLib;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader; // ★ 追加
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent; // ★ 改名
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import org.joml.Math;

import java.io.IOException;
import java.util.Objects;

@SuppressWarnings("removal")
@EventBusSubscriber(value = Dist.CLIENT)
public class ClientEffectManager {
    private static float parryAlpha = 0f;
    private static boolean isSpeeding = false;
    private static float fovModifier = 1.0f;
    private static float shakeIntensity = 0f;
    private static int shakeTicks = 0;
    private static ShaderInstance parryShader;

    public static void triggerParryFlash() {
        parryAlpha = 1.0f;
    }

    public static void setSpeeding(boolean speeding) {
        isSpeeding = speeding;
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (parryAlpha > 0) {
            parryAlpha = Math.max(0, parryAlpha - 0.04f);
        }
        float targetFov = isSpeeding ? 1.25f : 1.0f;
        fovModifier = Math.lerp(fovModifier, targetFov, 0.1f);
        if (shakeTicks > 0) {
            shakeTicks--;
            if (shakeTicks <= 0) shakeIntensity = 0;
        }
    }

    @SubscribeEvent
    public static void onComputeFov(ViewportEvent.ComputeFov event) {
        event.setFOV(event.getFOV() * fovModifier);
    }

    private static void renderParryOverlay(int width, int height) {
        if (parryAlpha > 0 && parryShader != null) {
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShader(() -> parryShader);
            if (parryShader.safeGetUniform("Intensity") != null) {
                parryShader.safeGetUniform("Intensity").set(parryAlpha);
            }
            RenderSystem.setShaderTexture(0, Minecraft.getInstance().getMainRenderTarget().getColorTextureId());

            // ★ 1.21.1 新しい描画パイプラインの書き方
            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder bufferbuilder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

            bufferbuilder.addVertex(0.0F, (float) height, 0.0F).setUv(0.0F, 0.0F);
            bufferbuilder.addVertex((float) width, (float) height, 0.0F).setUv(1.0F, 0.0F);
            bufferbuilder.addVertex((float) width, 0.0F, 0.0F).setUv(1.0F, 1.0F);
            bufferbuilder.addVertex(0.0F, 0.0F, 0.0F).setUv(0.0F, 1.0F);

            // バッファをビルドしてシェーダーで描画
            BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());

            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
            RenderSystem.disableBlend();
        }
    }

    public static void startShake(float intensity, int duration) {
        shakeIntensity = intensity;
        shakeTicks = duration;
    }

    @SubscribeEvent
    public static void onCameraSetup(ViewportEvent.ComputeCameraAngles event) {
        if (shakeTicks > 0) {
            float f = (Objects.requireNonNull(Minecraft.getInstance().level).random.nextFloat() - 0.5f) * shakeIntensity;
            float g = (Minecraft.getInstance().level.random.nextFloat() - 0.5f) * shakeIntensity;
            event.setPitch(event.getPitch() + f);
            event.setYaw(event.getYaw() + g);
            event.setRoll(event.getRoll() + f * 0.5f);
        }
    }

    @EventBusSubscriber(value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
    public static class ModBusEvents {
        @SubscribeEvent
        public static void onRegisterShaders(RegisterShadersEvent event) throws IOException {
            event.registerShader(new ShaderInstance(event.getResourceProvider(), ResourceLocation.fromNamespaceAndPath(HighSpeedLib.MODID, "parry_flash"), DefaultVertexFormat.POSITION_TEX), s -> {
                parryShader = s;
            });
        }

        // ★ RegisterGuiLayersEvent への変更
        @SubscribeEvent
        public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
            event.registerAboveAll(
                    ResourceLocation.fromNamespaceAndPath(HighSpeedLib.MODID, "parry_overlay"),
                    (guiGraphics, deltaTracker) -> renderParryOverlay(guiGraphics.guiWidth(), guiGraphics.guiHeight())
            );
        }
    }
}