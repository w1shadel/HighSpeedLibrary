package com.maxwell.highspeedlib.client.state;

import net.minecraft.network.chat.Component;

public class TextData {
    public Type type;
    public Component text;
    public double x, y;
    public int color;
    public int duration;
    public int maxDuration;
    public float scale;
    public TextData(Type type, Component text, double x, double y, int color, int duration, float scale) {
        this.type = type;
        this.text = text;
        this.x = x;
        this.y = y;
        this.color = color;
        this.duration = duration;
        this.maxDuration = duration;
        this.scale = scale;
    }

    public void tick() {
        if (this.duration > 0) this.duration--;
    }

    public boolean isExpired() {
        return this.duration <= 0;
    }

    public enum Type {TITLE, SUBTITLE}
}