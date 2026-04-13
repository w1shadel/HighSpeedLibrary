package com.maxwell.highspeedlib.client.network;

import com.maxwell.highspeedlib.client.logic.ClientEffectManager;
import com.maxwell.highspeedlib.client.logic.ClientSlideHandler;
import com.maxwell.highspeedlib.client.logic.ThirdPersonCoinTossManager;
import com.maxwell.highspeedlib.client.logic.ThirdPersonPunchManager;
import com.maxwell.highspeedlib.client.renderer.ExtendsArmRenderer;
import com.maxwell.highspeedlib.client.renderer.UltraHudRenderer;
import com.maxwell.highspeedlib.common.logic.combat.ArmType;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;

public class ClientPacketHandler {
    public static void handleStaminaSync(double stamina, double maxStamina) {
        UltraHudRenderer.setClientStamina(stamina, maxStamina);
    }

    public static void handlePunchEnergySync(double energy) {
        com.maxwell.highspeedlib.common.logic.combat.PunchCooldownManager.setClientEnergy(energy);
    }

    public static void handlePunchAnimation(int entityId) {
        ThirdPersonPunchManager.startAnimation(entityId);
    }

    public static void handleTossAnimation(int entityId) {
        ThirdPersonCoinTossManager.startAnimation(entityId);
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.player.getId() == entityId) {
            ExtendsArmRenderer.startToss();
        }
    }

    public static void handleArmSync(ArmType arm) {
        UltraHudRenderer.setClientArm(arm);
    }

    public static void handleSlideSync(int entityId, boolean sliding) {
        ClientSlideHandler.updateOtherPlayerSliding(entityId, sliding);
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            Entity entity = mc.level.getEntity(entityId);
            if (entity != null) {
                entity.refreshDimensions();
            }
        }
    }

    public static void handleParry() {
        com.maxwell.highspeedlib.common.logic.TimeManager.setHitstop(10);
        ClientEffectManager.triggerParryFlash();
    }
}
