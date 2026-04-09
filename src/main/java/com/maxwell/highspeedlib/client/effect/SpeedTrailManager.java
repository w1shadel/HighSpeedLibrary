package com.maxwell.highspeedlib.client.effect;

import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class SpeedTrailManager {
    public static class TrailPoint {
        public final Vec3 pos;
        public final int maxLife;
        public int life;

        public TrailPoint(Vec3 pos, int life) {
            this.pos = pos;
            this.life = life;
            this.maxLife = life;
        }
    }

    public static class TrailInstance {
        public final List<TrailPoint> points = new ArrayList<>();
        public float r, g, b, a;
        public float width;

        public TrailInstance(float r, float g, float b, float a, float width) {
            this.r = r;
            this.g = g;
            this.b = b;
            this.a = a;
            this.width = width;
        }

        public void addPoint(Vec3 pos, int life) {
            points.add(0, new TrailPoint(pos, life));
        }

        public void tick() {
            points.removeIf(p -> --p.life <= 0);
        }
    }
}