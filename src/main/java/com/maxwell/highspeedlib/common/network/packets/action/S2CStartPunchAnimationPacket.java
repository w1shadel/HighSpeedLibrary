package com.maxwell.highspeedlib.common.network.packets.action;

import com.maxwell.highspeedlib.HighSpeedLib;

import com.maxwell.highspeedlib.client.network.ClientPacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;





public class S2CStartPunchAnimationPacket implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<S2CStartPunchAnimationPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(HighSpeedLib.MODID, "s2c_start_punch_animation_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, S2CStartPunchAnimationPacket> STREAM_CODEC = StreamCodec.of((RegistryFriendlyByteBuf buf, S2CStartPunchAnimationPacket msg) -> S2CStartPunchAnimationPacket.encode(msg, buf), S2CStartPunchAnimationPacket::decode);

    @Override
    public CustomPacketPayload.Type<S2CStartPunchAnimationPacket> type() { return TYPE; }
    private final int entityId;

    public S2CStartPunchAnimationPacket(int entityId) {
        this.entityId = entityId;
    }

    public static void encode(S2CStartPunchAnimationPacket msg, FriendlyByteBuf buffer) {
        buffer.writeInt(msg.entityId);
    }

    public static S2CStartPunchAnimationPacket decode(FriendlyByteBuf buffer) {
        return new S2CStartPunchAnimationPacket(buffer.readInt());
    }

    public static void handle(S2CStartPunchAnimationPacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ClientPacketHandler.handlePunchAnimation(msg.entityId);
        });
    }
}
