package com.maxwell.highspeedlib.network.packets;

import com.maxwell.highspeedlib.client.ClientEffectManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2CScreenShakePacket {
    private final float intensity;
    private final int duration;

    public S2CScreenShakePacket(float intensity, int duration) {
        this.intensity = intensity;
        this.duration = duration;
    }

    public static void encode(S2CScreenShakePacket msg, FriendlyByteBuf buffer) {
        buffer.writeFloat(msg.intensity);
        buffer.writeInt(msg.duration);
    }

    public static S2CScreenShakePacket decode(FriendlyByteBuf buffer) {
        return new S2CScreenShakePacket(buffer.readFloat(), buffer.readInt());
    }

    public static void handle(S2CScreenShakePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ClientEffectManager.startShake(msg.intensity, msg.duration);
        });
        ctx.get().setPacketHandled(true);
    }
}