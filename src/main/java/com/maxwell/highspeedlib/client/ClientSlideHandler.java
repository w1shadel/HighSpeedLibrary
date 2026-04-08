package com.maxwell.highspeedlib.client;

import com.maxwell.highspeedlib.HighSpeedLib;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderBlockScreenEffectEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Mod.EventBusSubscriber(modid = HighSpeedLib.MODID, value = Dist.CLIENT)
public class ClientSlideHandler {
    private static final Set<Integer> otherSlidingPlayers = new HashSet<>();
    private static final float SLIDE_FOV_TARGET = 1.2f;
    private static final float NORMAL_FOV_TARGET = 1.0f;
    private static final float FOV_LERP_SPEED = 0.1f;
    private static final double SLIDE_SPEED = 0.75;
    private static boolean clientIsSliding = false;
    private static float slideProgress = 0f;
    private static float prevSlideProgress = 0f;
    private static float currentFovModifier = 1.0f;
    private static Vec3 lockedSlideDir = null;

    public static void setSliding(boolean sliding) {
        if (!clientIsSliding && sliding) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                float x = mc.player.xxa;
                float z = mc.player.zza;
                if (x == 0 && z == 0) {
                    lockedSlideDir = new Vec3(mc.player.getLookAngle().x, 0, mc.player.getLookAngle().z).normalize();
                } else {
                    float yaw = mc.player.getYRot();
                    float f1 = (float) Math.sin(yaw * (Math.PI / 180.0));
                    float f2 = (float) Math.cos(yaw * (Math.PI / 180.0));
                    lockedSlideDir = new Vec3(x * f2 - z * f1, 0, z * f2 + x * f1).normalize();
                }
            }
        }
        clientIsSliding = sliding;
        if (!sliding) {
            lockedSlideDir = null;
        }
    }

    public static void updateOtherPlayerSliding(int entityId, boolean sliding) {
        if (Minecraft.getInstance().player != null && Minecraft.getInstance().player.getId() == entityId) {
            setSliding(sliding);
        } else {
            if (sliding) otherSlidingPlayers.add(entityId);
            else otherSlidingPlayers.remove(entityId);
        }
    }

    public static boolean isPlayerSliding(int entityId) {
        if (Minecraft.getInstance().player != null && Minecraft.getInstance().player.getId() == entityId) {
            return clientIsSliding;
        }
        return otherSlidingPlayers.contains(entityId);
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            prevSlideProgress = slideProgress;
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null && clientIsSliding && lockedSlideDir != null) {
                Vec3 motion = mc.player.getDeltaMovement();
                mc.player.setDeltaMovement(lockedSlideDir.x * SLIDE_SPEED, motion.y, lockedSlideDir.z * SLIDE_SPEED);
            } else if (!clientIsSliding) {
                lockedSlideDir = null;
            }
            float target = clientIsSliding ? SLIDE_FOV_TARGET : NORMAL_FOV_TARGET;
            currentFovModifier = Mth.lerp(FOV_LERP_SPEED, currentFovModifier, target);
            float slideTarget = clientIsSliding ? 1.0f : 0.0f;
            float lerpAlpha = clientIsSliding ? 0.6f : 0.3f;
            slideProgress = Mth.lerp(lerpAlpha, slideProgress, slideTarget);
        }
    }

    @SubscribeEvent
    public static void onComputeFov(ViewportEvent.ComputeFov event) {
        float newFov = (float) (event.getFOV() * currentFovModifier);
        if (newFov > 130.0f) newFov = 130.0f;
        event.setFOV(newFov);
    }

    @SubscribeEvent
    public static void onCameraSetup(ViewportEvent.ComputeCameraAngles event) {
        if (clientIsSliding) {
            float shake = (Objects.requireNonNull(Minecraft.getInstance().level).random.nextFloat() - 0.5f) * 0.1f;
            event.setPitch(event.getPitch() + shake);
        }
    }

    public static float getSlideProgress(float partialTicks) {
        return Mth.lerp(partialTicks, prevSlideProgress, slideProgress);
    }

    public static float getSlideProgress() {
        return slideProgress;
    }

    @SubscribeEvent
    public static void onRenderBlockOverlay(RenderBlockScreenEffectEvent event) {
        if (clientIsSliding) {
            event.setCanceled(true);
        }
    }
}