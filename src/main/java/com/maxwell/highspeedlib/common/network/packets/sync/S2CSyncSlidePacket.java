package com.maxwell.highspeedlib.common.network.packets.sync;

import com.maxwell.highspeedlib.HighSpeedLib;

import com.maxwell.highspeedlib.client.network.ClientPacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;






public class S2CSyncSlidePacket implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<S2CSyncSlidePacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(HighSpeedLib.MODID, "s2c_sync_slide_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, S2CSyncSlidePacket> STREAM_CODEC = StreamCodec.of((RegistryFriendlyByteBuf buf, S2CSyncSlidePacket msg) -> S2CSyncSlidePacket.encode(msg, buf), S2CSyncSlidePacket::decode);

    @Override
    public CustomPacketPayload.Type<S2CSyncSlidePacket> type() { return TYPE; }
    private final int entityId;
    private final boolean sliding;

    public S2CSyncSlidePacket(int entityId, boolean sliding) {
        this.entityId = entityId;
        this.sliding = sliding;
    }

    public static void encode(S2CSyncSlidePacket msg, FriendlyByteBuf buffer) {
        buffer.writeInt(msg.entityId);
        buffer.writeBoolean(msg.sliding);
    }

    public static S2CSyncSlidePacket decode(FriendlyByteBuf buffer) {
        return new S2CSyncSlidePacket(buffer.readInt(), buffer.readBoolean());
    }

    public static void handle(S2CSyncSlidePacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ClientPacketHandler.handleSlideSync(msg.entityId, msg.sliding);
        });
    }
}
