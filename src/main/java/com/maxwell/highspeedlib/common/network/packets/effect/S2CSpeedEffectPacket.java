package com.maxwell.highspeedlib.common.network.packets.effect;

import com.maxwell.highspeedlib.HighSpeedLib;

import com.maxwell.highspeedlib.client.logic.ClientEffectManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;




public class S2CSpeedEffectPacket implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<S2CSpeedEffectPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(HighSpeedLib.MODID, "s2c_speed_effect_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, S2CSpeedEffectPacket> STREAM_CODEC = StreamCodec.of((RegistryFriendlyByteBuf buf, S2CSpeedEffectPacket msg) -> S2CSpeedEffectPacket.encode(msg, buf), S2CSpeedEffectPacket::decode);

    @Override
    public CustomPacketPayload.Type<S2CSpeedEffectPacket> type() { return TYPE; }
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

    public static void handle(S2CSpeedEffectPacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ClientEffectManager.setSpeeding(msg.isSpeeding);
        });
    }
}
