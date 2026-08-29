package com.maxwell.highspeedlib.common.network.packets.effect;

import com.maxwell.highspeedlib.HighSpeedLib;
import com.maxwell.highspeedlib.client.logic.ClientEffectManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class S2CScreenShakePacket implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<S2CScreenShakePacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(HighSpeedLib.MODID, "s2c_screen_shake_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, S2CScreenShakePacket> STREAM_CODEC = StreamCodec.of((RegistryFriendlyByteBuf buf, S2CScreenShakePacket msg) -> S2CScreenShakePacket.encode(msg, buf), S2CScreenShakePacket::decode);
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

    public static void handle(S2CScreenShakePacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ClientEffectManager.startShake(msg.intensity, msg.duration);
        });
    }

    @Override
    public CustomPacketPayload.Type<S2CScreenShakePacket> type() {
        return TYPE;
    }
}
