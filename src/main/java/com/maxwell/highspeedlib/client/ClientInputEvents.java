package com.maxwell.highspeedlib.client;

import com.maxwell.highspeedlib.network.PacketHandler;
import com.maxwell.highspeedlib.network.packets.C2SKeyInputPacket;
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
            if (!ParryArmRenderer.isPunching()) {
                PacketHandler.INSTANCE.sendToServer(new C2SKeyInputPacket(1));
                ParryArmRenderer.startPunch();
            }
        }

        while (KeyInputHandler.DASH_KEY.consumeClick()) {
            PacketHandler.INSTANCE.sendToServer(new C2SKeyInputPacket(0));
            ClientEffectManager.setSpeeding(true);
        }

        boolean isSliding = KeyInputHandler.SLIDING_KEY.isDown();
        if (isSliding != wasSliding) {
            if (isSliding) {
                PacketHandler.INSTANCE.sendToServer(new C2SKeyInputPacket(3)); 
            } else {
                PacketHandler.INSTANCE.sendToServer(new C2SKeyInputPacket(4)); 
            }
            wasSliding = isSliding;
        }
    }
}