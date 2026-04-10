package com.maxwell.highspeedlib.client.renderer;

import com.maxwell.highspeedlib.HighSpeedLib;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

import java.util.*;

@SuppressWarnings("removal")
@Mod.EventBusSubscriber(modid = HighSpeedLib.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BloodRenderManager {
    private static final ResourceLocation WHITE_TEX = new ResourceLocation("minecraft", "textures/block/white_concrete.png");
    private static final int MAX_GROUND_SPLATS = 600;
    private static final int MAX_AIR_DROPS = 400;
    private static final List<GroundSplat> groundSplats = new ArrayList<>();
    private static final List<BloodDrop> airDrops = new ArrayList<>();
    private static final List<BloodDrop> pendingAirDrops = Collections.synchronizedList(new ArrayList<>());
    private static final Random RAND = new Random();

    public static void spawnBloodSpray(Vec3 pos, Vec3 direction, int count) {
        synchronized (pendingAirDrops) {
            for (int i = 0; i < count; i++) {
                Vec3 vel = direction.add((RAND.nextDouble() - 0.5) * 1.2, (RAND.nextDouble() - 0.5) * 0.5, (RAND.nextDouble() - 0.5) * 0.2)
                        .scale(0.2 + RAND.nextDouble() * 0.6);
                pendingAirDrops.add(new BloodDrop(pos, vel, 0.04f + RAND.nextFloat() * 0.06f));
            }
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || Minecraft.getInstance().level == null) return;
        synchronized (pendingAirDrops) {
            if (!pendingAirDrops.isEmpty()) {
                airDrops.addAll(pendingAirDrops);
                pendingAirDrops.clear();
            }
        }
        groundSplats.removeIf(s -> ++s.age > 1200);
        if (groundSplats.size() > MAX_GROUND_SPLATS) {
            groundSplats.subList(0, groundSplats.size() - MAX_GROUND_SPLATS).clear();
        }
        if (airDrops.size() > MAX_AIR_DROPS) {
            airDrops.subList(0, airDrops.size() - MAX_AIR_DROPS).clear();
        }
        Iterator<BloodDrop> it = airDrops.iterator();
        while (it.hasNext()) {
            BloodDrop d = it.next();
            d.age++;
            Vec3 nextPos = d.pos.add(d.velocity);
            BlockHitResult hit = Minecraft.getInstance().level.clip(new ClipContext(d.pos, nextPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, Minecraft.getInstance().player));
            boolean shouldRemove = false;
            if (hit.getType() == HitResult.Type.BLOCK) {
                float yOff = (groundSplats.size() % 30) * 0.0005f;
                groundSplats.add(new GroundSplat(hit.getLocation(), RAND.nextFloat() * 360f, d.size * 5f, yOff));
                shouldRemove = true;
            } else if (d.age > 50) {
                shouldRemove = true;
            }
            if (shouldRemove) {
                it.remove();
            } else {
                d.pos = nextPos;
                d.velocity = d.velocity.add(0, -0.045, 0).scale(0.94);
            }
        }
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;
        Minecraft mc = Minecraft.getInstance();
        Vec3 cam = mc.gameRenderer.getMainCamera().getPosition();
        PoseStack poseStack = event.getPoseStack();
        VertexConsumer builder = mc.renderBuffers().bufferSource().getBuffer(RenderType.entityCutoutNoCull(WHITE_TEX));
        poseStack.pushPose();
        poseStack.translate(-cam.x, -cam.y, -cam.z);
        Matrix4f matrix = poseStack.last().pose();
        for (GroundSplat s : groundSplats) {
            float alpha = s.age > 1000 ? (1200 - s.age) / 200f : 1.0f;
            drawGorySplat(matrix, builder, s, alpha);
        }
        for (BloodDrop d : airDrops) {
            drawStretchedDrop(matrix, builder, d);
        }
        poseStack.popPose();
    }

    private static void drawGorySplat(Matrix4f matrix, VertexConsumer builder, GroundSplat s, float alpha) {
        int r = 110, g = 0, b = 0, a = (int) (alpha * 255);
        float y = (float) s.pos.y + 0.02f + s.yOffset;
        for (int i = 0; i < 10; i++) {
            float a1 = (float) Math.toRadians(s.rot + i * 36);
            float a2 = (float) Math.toRadians(s.rot + (i + 1) * 36);
            float r1 = s.radii[i];
            float r2 = s.radii[(i + 1) % 10];
            addVertex(matrix, builder, (float) s.pos.x, y, (float) s.pos.z, r - 30, 0, 0, a, 0.5f, 0.5f);
            addVertex(matrix, builder, (float) s.pos.x + (float) Math.cos(a1) * r1, y, (float) s.pos.z + (float) Math.sin(a1) * r1, r, 0, 0, a, 0, 0);
            addVertex(matrix, builder, (float) s.pos.x + (float) Math.cos(a2) * r2, y, (float) s.pos.z + (float) Math.sin(a2) * r2, r, 0, 0, a, 1, 1);
            addVertex(matrix, builder, (float) s.pos.x, y, (float) s.pos.z, r - 30, 0, 0, a, 0.5f, 0.5f);
        }
    }

    private static void drawStretchedDrop(Matrix4f matrix, VertexConsumer builder, BloodDrop d) {
        Vec3 dir = d.velocity.lengthSqr() > 0 ? d.velocity.normalize() : new Vec3(0, -1, 0);
        Vec3 up = new Vec3(0, 1, 0);
        if (Math.abs(dir.y) > 0.9) up = new Vec3(1, 0, 0);
        Vec3 side = dir.cross(up).normalize().scale(d.size);
        Vec3 vertical = dir.cross(side).normalize().scale(d.size);
        Vec3 head = d.pos;
        Vec3 tail = d.pos.subtract(d.velocity.scale(2.0));
        int r = 130, g = 0, b = 0, a = 255;
        drawQuad(matrix, builder, head, tail, side, r, g, b, a);
        drawQuad(matrix, builder, head, tail, vertical, r, g, b, a);
    }

    private static void drawQuad(Matrix4f matrix, VertexConsumer builder, Vec3 h, Vec3 t, Vec3 off, int r, int g, int b, int a) {
        float ox = (float) off.x, oy = (float) off.y, oz = (float) off.z;
        addVertex(matrix, builder, (float) h.x - ox, (float) h.y - oy, (float) h.z - oz, r, g, b, a, 0, 0);
        addVertex(matrix, builder, (float) h.x + ox, (float) h.y + oy, (float) h.z + oz, r, g, b, a, 1, 0);
        addVertex(matrix, builder, (float) t.x + ox, (float) t.y + oy, (float) t.z + oz, r, g, b, a, 1, 1);
        addVertex(matrix, builder, (float) t.x - ox, (float) t.y - oy, (float) t.z - oz, r, g, b, a, 0, 1);
    }

    private static void addVertex(Matrix4f matrix, VertexConsumer builder, float x, float y, float z, int r, int g, int b, int a, float u, float v) {
        builder.vertex(matrix, x, y, z).color(r, g, b, a).uv(u, v).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(0, 1, 0).endVertex();
    }

    private static class GroundSplat {
        Vec3 pos;
        int age;
        float[] radii;
        float rot;
        float yOffset;

        GroundSplat(Vec3 pos, float rot, float baseSize, float yOff) {
            this.pos = pos;
            this.rot = rot;
            this.age = 0;
            this.yOffset = yOff;
            this.radii = new float[10];
            Random rand = new Random();
            for (int i = 0; i < 10; i++) this.radii[i] = baseSize * (0.4f + rand.nextFloat() * 1.2f);
        }
    }

    private static class BloodDrop {
        Vec3 pos;
        Vec3 velocity;
        int age;
        float size;

        BloodDrop(Vec3 pos, Vec3 vel, float size) {
            this.pos = pos;
            this.velocity = vel;
            this.size = size;
            this.age = 0;
        }
    }
}