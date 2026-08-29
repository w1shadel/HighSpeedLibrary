package com.maxwell.highspeedlib.common.network.packets.sync;

import com.maxwell.highspeedlib.HighSpeedLib;
import com.maxwell.highspeedlib.client.network.ClientPacketHandler;
import com.maxwell.highspeedlib.common.logic.combat.ArmType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class S2CSyncArmPacket implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<S2CSyncArmPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(HighSpeedLib.MODID, "s2c_sync_arm_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, S2CSyncArmPacket> STREAM_CODEC = StreamCodec.of((RegistryFriendlyByteBuf buf, S2CSyncArmPacket msg) -> S2CSyncArmPacket.encode(msg, buf), S2CSyncArmPacket::decode);
    private final int armOrdinal;

    public S2CSyncArmPacket(ArmType arm) {
        this.armOrdinal = arm.ordinal();
    }

    public S2CSyncArmPacket(int ordinal) {
        this.armOrdinal = ordinal;
    }

    public static void encode(S2CSyncArmPacket msg, FriendlyByteBuf buffer) {
        buffer.writeInt(msg.armOrdinal);
    }

    public static S2CSyncArmPacket decode(FriendlyByteBuf buffer) {
        return new S2CSyncArmPacket(buffer.readInt());
    }

    public static void handle(S2CSyncArmPacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ClientPacketHandler.handleArmSync(ArmType.values()[msg.armOrdinal]);
        });
    }

    @Override
    public CustomPacketPayload.Type<S2CSyncArmPacket> type() {
        return TYPE;
    }
}
