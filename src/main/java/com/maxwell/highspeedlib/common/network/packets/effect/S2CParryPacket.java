package com.maxwell.highspeedlib.common.network.packets.effect;

import com.maxwell.highspeedlib.HighSpeedLib;
import com.maxwell.highspeedlib.client.network.ClientPacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class S2CParryPacket implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<S2CParryPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(HighSpeedLib.MODID, "s2c_parry_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, S2CParryPacket> STREAM_CODEC = StreamCodec.of((RegistryFriendlyByteBuf buf, S2CParryPacket msg) -> S2CParryPacket.encode(msg, buf), S2CParryPacket::decode);

    public S2CParryPacket() {
    }

    public static void encode(S2CParryPacket msg, FriendlyByteBuf buffer) {
    }

    public static S2CParryPacket decode(FriendlyByteBuf buffer) {
        return new S2CParryPacket();
    }

    public static void handle(S2CParryPacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ClientPacketHandler.handleParry();
        });
    }

    @Override
    public CustomPacketPayload.Type<S2CParryPacket> type() {
        return TYPE;
    }
}
