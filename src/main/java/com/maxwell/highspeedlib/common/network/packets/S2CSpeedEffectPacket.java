package com.maxwell.highspeedlib.common.network.packets;

import com.maxwell.highspeedlib.client.ClientEffectManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2CSpeedEffectPacket {
    private final boolean isSpeeding;

    public S2CSpeedEffectPacket(boolean isSpeeding) {
        this.isSpeeding = isSpeeding;
    }

    public static void encode(S2CSpeedEffectPacket msg, FriendlyByteBuf buffer) {
        buffer.writeBoolean(msg.isSpeeding);
    }

    public static S2CSpeedEffectPacket decode(FriendlyByteBuf buffer) {
        return new S2CSpeedEffectPacket(buffer.readBoolean());
    }

    public static void handle(S2CSpeedEffectPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ClientEffectManager.setSpeeding(msg.isSpeeding);
        });
        ctx.get().setPacketHandled(true);
    }
}