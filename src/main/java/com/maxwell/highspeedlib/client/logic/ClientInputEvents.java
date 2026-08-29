package com.maxwell.highspeedlib.client.logic;

import com.maxwell.highspeedlib.client.renderer.ClientTrailRenderer;
import com.maxwell.highspeedlib.client.renderer.ExtendsArmRenderer;
import com.maxwell.highspeedlib.client.renderer.UltraHudRenderer;
import com.maxwell.highspeedlib.client.state.ArmManager;
import com.maxwell.highspeedlib.common.logic.combat.ArmType;
import com.maxwell.highspeedlib.common.network.PacketHandler;
import com.maxwell.highspeedlib.common.network.packets.action.C2SKeyInputPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.network.PacketDistributor;
@SuppressWarnings("removal")
@EventBusSubscriber(value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public class ClientInputEvents {
    private static boolean wasSliding = false;
    private static boolean jumpKeyWasPressed = false;
    private static boolean wasOnGroundLastTick = false;
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        com.maxwell.highspeedlib.client.state.UltraBossBarManager.tick();
        com.maxwell.highspeedlib.client.renderer.UltraBossBarRenderer.tick();
        ClientTrailRenderer.tick();
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        while (KeyInputHandler.PARRY_KEY.consumeClick()) {
            if (UltraHudRenderer.punchUnlocked) {
                ArmType current = ArmManager.getArm(mc.player);
                boolean isRed = (current == ArmType.KNUCKLEBLASTER);
                if (!ExtendsArmRenderer.isPunching() && com.maxwell.highspeedlib.common.logic.combat.PunchCooldownManager.tryConsume(mc.player, isRed)) {
                    PacketDistributor.sendToServer(new C2SKeyInputPacket(1));
                    ExtendsArmRenderer.startPunch();
                }
            } else {
                mc.player.playSound(SoundEvents.DISPENSER_FAIL, 1.0f, 2.0f);
            }
        }
        while (KeyInputHandler.DASH_KEY.consumeClick()) {
            if (UltraHudRenderer.dashUnlocked) {
                PacketDistributor.sendToServer(new C2SKeyInputPacket(0, mc.player.xxa, mc.player.zza,false));
                ClientEffectManager.setSpeeding(true);
                ClientDashHandler.spawnDashEffects();
            }
        }
        while (KeyInputHandler.COIN_KEY.consumeClick()) {
            if (UltraHudRenderer.punchUnlocked) {
                PacketDistributor.sendToServer(new C2SKeyInputPacket(5));
            }
        }
        boolean isSlidingInput = KeyInputHandler.SLIDING_KEY.isDown();
        if (isSlidingInput != wasSliding) {
            if (isSlidingInput) {
                boolean canPerform = mc.player.onGround() ? UltraHudRenderer.slidingUnlocked : UltraHudRenderer.slamUnlocked;
                if (canPerform) {
                    PacketDistributor.sendToServer(new C2SKeyInputPacket(3, mc.player.xxa, mc.player.zza,false));
                }
            } else {
                PacketDistributor.sendToServer(new C2SKeyInputPacket(4));
            }
            wasSliding = isSlidingInput;
        }
        boolean jumpKeyDown = mc.options.keyJump.isDown();
        if (jumpKeyDown && !jumpKeyWasPressed) {
            if (!mc.player.onGround() && !wasOnGroundLastTick && UltraHudRenderer.walljumpUnlocked) {
                boolean isSliding = KeyInputHandler.SLIDING_KEY.isDown();
                PacketDistributor.sendToServer(new C2SKeyInputPacket(6, 0, 0, isSliding));
            }
        }
        jumpKeyWasPressed = jumpKeyDown;
        wasOnGroundLastTick = mc.player.onGround();
        while (KeyInputHandler.CHANGEARM_KEY.consumeClick()) {
            if (UltraHudRenderer.punchUnlocked) {
                PacketDistributor.sendToServer(new C2SKeyInputPacket(7));
                mc.player.playSound(SoundEvents.ARMOR_EQUIP_GENERIC.value(), 1.0f, 1.5f);
            }
        }
        while (KeyInputHandler.WHIPLASH_KEY.consumeClick()) {
            if (UltraHudRenderer.whiplashUnlocked) {
                PacketDistributor.sendToServer(new C2SKeyInputPacket(8));
            }
        }
    }
}
