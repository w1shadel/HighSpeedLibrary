package com.maxwell.highspeedlib.common.network.packets.action;

import com.maxwell.highspeedlib.HighSpeedLib;

import com.maxwell.highspeedlib.client.network.ClientPacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;





public class S2CStartTossAnimationPacket implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<S2CStartTossAnimationPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(HighSpeedLib.MODID, "s2c_start_toss_animation_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, S2CStartTossAnimationPacket> STREAM_CODEC = StreamCodec.of((RegistryFriendlyByteBuf buf, S2CStartTossAnimationPacket msg) -> S2CStartTossAnimationPacket.encode(msg, buf), S2CStartTossAnimationPacket::decode);

    @Override
    public CustomPacketPayload.Type<S2CStartTossAnimationPacket> type() { return TYPE; }
    private final int entityId;

    public S2CStartTossAnimationPacket(int entityId) {
        this.entityId = entityId;
    }

    public static void encode(S2CStartTossAnimationPacket msg, FriendlyByteBuf buffer) {
        buffer.writeInt(msg.entityId);
    }

    public static S2CStartTossAnimationPacket decode(FriendlyByteBuf buffer) {
        return new S2CStartTossAnimationPacket(buffer.readInt());
    }

    public static void handle(S2CStartTossAnimationPacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ClientPacketHandler.handleTossAnimation(msg.entityId);
        });
    }
}
