package com.maxwell.highspeedlib.common.logic.combat;

public enum ArmType {
    FEEDBACKER(0xFF4444FF, "F"),
    KNUCKLEBLASTER(0xFFFF4444, "K");
    public final int color;
    public final String letter;

    ArmType(int color, String letter) {
        this.color = color;
        this.letter = letter;
    }
}
