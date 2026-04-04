package com.maxwell.highspeedlib.network.packets;

import com.maxwell.highspeedlib.client.ClientEffectManager;
import com.maxwell.highspeedlib.client.ParryArmRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2CParryPacket {
    public S2CParryPacket() {
    }

    public static void encode(S2CParryPacket msg, FriendlyByteBuf buffer) {
    }

    public static S2CParryPacket decode(FriendlyByteBuf buffer) {
        return new S2CParryPacket();
    }
    public static void handle(S2CParryPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {

            ParryArmRenderer.startHitstop(5); 

            ClientEffectManager.triggerParryFlash();

            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                mc.player.setDeltaMovement(0, 0, 0);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}