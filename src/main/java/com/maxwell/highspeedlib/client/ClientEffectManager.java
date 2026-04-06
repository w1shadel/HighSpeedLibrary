package com.maxwell.highspeedlib.client;

import com.maxwell.highspeedlib.HighSpeedLib;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Math;

import java.io.IOException;
import java.util.Objects;

@SuppressWarnings("removal")
@Mod.EventBusSubscriber(value = Dist.CLIENT)
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
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
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
            BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
            bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
            bufferbuilder.vertex(0.0D, height, 0.0D).uv(0.0F, 0.0F).endVertex();
            bufferbuilder.vertex(width, height, 0.0D).uv(1.0F, 0.0F).endVertex();
            bufferbuilder.vertex(width, 0.0D, 0.0D).uv(1.0F, 1.0F).endVertex();
            bufferbuilder.vertex(0.0D, 0.0D, 0.0D).uv(0.0F, 1.0F).endVertex();
            Tesselator.getInstance().end();
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

    @Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModBusEvents {
        @SubscribeEvent
        public static void onRegisterShaders(RegisterShadersEvent event) throws IOException {
            event.registerShader(new ShaderInstance(event.getResourceProvider(), new ResourceLocation(HighSpeedLib.MODID, "parry_flash"), DefaultVertexFormat.POSITION_TEX), s -> {
                parryShader = s;
            });
        }

        @SubscribeEvent
        public static void onRegisterGuiOverlays(RegisterGuiOverlaysEvent event) {
            event.registerAboveAll("parry_overlay", (gui, guiGraphics, partialTick, width, height) -> {
                renderParryOverlay(width, height);
            });
        }
    }
}