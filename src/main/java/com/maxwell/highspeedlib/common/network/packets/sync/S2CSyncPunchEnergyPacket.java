package com.maxwell.highspeedlib.common.network.packets.sync;

import com.maxwell.highspeedlib.HighSpeedLib;
import com.maxwell.highspeedlib.client.network.ClientPacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class S2CSyncPunchEnergyPacket implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<S2CSyncPunchEnergyPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(HighSpeedLib.MODID, "s2c_sync_punch_energy_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, S2CSyncPunchEnergyPacket> STREAM_CODEC = StreamCodec.of((RegistryFriendlyByteBuf buf, S2CSyncPunchEnergyPacket msg) -> S2CSyncPunchEnergyPacket.encode(msg, buf), S2CSyncPunchEnergyPacket::decode);
    private final double energy;

    public S2CSyncPunchEnergyPacket(double energy) {
        this.energy = energy;
    }

    public static void encode(S2CSyncPunchEnergyPacket msg, FriendlyByteBuf buffer) {
        buffer.writeDouble(msg.energy);
    }

    public static S2CSyncPunchEnergyPacket decode(FriendlyByteBuf buffer) {
        return new S2CSyncPunchEnergyPacket(buffer.readDouble());
    }

    public static void handle(S2CSyncPunchEnergyPacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ClientPacketHandler.handlePunchEnergySync(msg.energy);
        });
    }

    @Override
    public CustomPacketPayload.Type<S2CSyncPunchEnergyPacket> type() {
        return TYPE;
    }
}
