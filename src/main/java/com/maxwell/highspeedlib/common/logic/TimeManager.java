package com.maxwell.highspeedlib.common.logic;

public class TimeManager {
    public static int hitstopTicks = 0;
    public static float slowMotionFactor = 1.0f;
    public static boolean isSlowMoTick = true;
    private static float slowCounter = 0;

    public static void tick() {
        if (hitstopTicks > 0) {
            hitstopTicks--;
            isSlowMoTick = false;
        } else {
            if (slowMotionFactor >= 1.0f) {
                isSlowMoTick = true;
                slowCounter = 0;
            } else {
                slowCounter += slowMotionFactor;
                if (slowCounter >= 1.0f) {
                    slowCounter -= 1.0f;
                    isSlowMoTick = true;
                } else {
                    isSlowMoTick = false;
                }
            }
        }
    }

    public static boolean shouldTick() {
        return isSlowMoTick;
    }

    public static void setHitstop(int ticks) {
        hitstopTicks = ticks;
    }

    public static void setSlowMo(float factor) {
        slowMotionFactor = factor;
    }
}