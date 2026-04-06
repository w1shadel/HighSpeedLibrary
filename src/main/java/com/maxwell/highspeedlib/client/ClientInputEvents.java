package com.maxwell.highspeedlib.client;

import com.maxwell.highspeedlib.common.logic.ArmType;
import com.maxwell.highspeedlib.client.state.ArmManager;

import com.maxwell.highspeedlib.common.network.PacketHandler;
import com.maxwell.highspeedlib.common.network.packets.C2SKeyInputPacket;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientInputEvents {
    private static boolean wasSliding = false;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        while (KeyInputHandler.PARRY_KEY.consumeClick()) {
            ArmType current = ArmManager.getArm(mc.player);
            boolean isRed = (current == ArmType.KNUCKLEBLASTER);
            if (!ExtendsArmRenderer.isPunching() && com.maxwell.highspeedlib.common.logic.PunchCooldownManager.tryConsume(mc.player, isRed)) {
                PacketHandler.INSTANCE.sendToServer(new C2SKeyInputPacket(1));
                ExtendsArmRenderer.startPunch();
            }
        }
        while (KeyInputHandler.DASH_KEY.consumeClick()) {
            PacketHandler.INSTANCE.sendToServer(new C2SKeyInputPacket(0, mc.player.xxa, mc.player.zza));
            ClientEffectManager.setSpeeding(true);
        }
        while (KeyInputHandler.COIN_KEY.consumeClick()) {
            PacketHandler.INSTANCE.sendToServer(new C2SKeyInputPacket(5));
        }
        boolean isSliding = KeyInputHandler.SLIDING_KEY.isDown();
        if (isSliding != wasSliding) {
            if (isSliding) {
                PacketHandler.INSTANCE.sendToServer(new C2SKeyInputPacket(3, mc.player.xxa, mc.player.zza));
            } else {
                PacketHandler.INSTANCE.sendToServer(new C2SKeyInputPacket(4));
            }
            wasSliding = isSliding;
        }
        while (mc.options.keyJump.consumeClick()) {
            if (!mc.player.onGround()) {
                PacketHandler.INSTANCE.sendToServer(new C2SKeyInputPacket(6));
            } else {
                mc.player.jumpFromGround();
            }
        }
        while (KeyInputHandler.CHANGEARM_KEY.consumeClick()) {
            PacketHandler.INSTANCE.sendToServer(new C2SKeyInputPacket(7));
            mc.player.playSound(net.minecraft.sounds.SoundEvents.ARMOR_EQUIP_GENERIC, 1.0f, 1.5f);
        }
        while (KeyInputHandler.WHIPLASH_KEY.consumeClick()) {
            PacketHandler.INSTANCE.sendToServer(new C2SKeyInputPacket(8));
        }
    }
}