package com.maxwell.highspeedlib.common.network.packets.sync;

import com.maxwell.highspeedlib.HighSpeedLib;
import com.maxwell.highspeedlib.client.logic.ClientSlamHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class S2CSyncSlamPacket implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<S2CSyncSlamPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(HighSpeedLib.MODID, "s2c_sync_slam_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, S2CSyncSlamPacket> STREAM_CODEC = StreamCodec.of((RegistryFriendlyByteBuf buf, S2CSyncSlamPacket msg) -> S2CSyncSlamPacket.encode(msg, buf), S2CSyncSlamPacket::decode);
    private final int entityId;
    private final boolean isSlamming;
    private final boolean isStorage;
    private final boolean hasImpact;
    private final double x, y, z;
    public S2CSyncSlamPacket(int entityId, boolean isSlamming) {
        this(entityId, isSlamming, false, 0, 0, 0, false);
    }

    public S2CSyncSlamPacket(int entityId, boolean isSlamming, boolean isStorage) {
        this(entityId, isSlamming, false, 0, 0, 0, isStorage);
    }

    public S2CSyncSlamPacket(int entityId, Vec3 impactPos) {
        this(entityId, false, true, impactPos.x, impactPos.y, impactPos.z, false);
    }

    private S2CSyncSlamPacket(int entityId, boolean isSlamming, boolean hasImpact, double x, double y, double z, boolean isStorage) {
        this.entityId = entityId;
        this.isSlamming = isSlamming;
        this.hasImpact = hasImpact;
        this.x = x;
        this.y = y;
        this.z = z;
        this.isStorage = isStorage;
    }

    public static void encode(S2CSyncSlamPacket msg, FriendlyByteBuf buffer) {
        buffer.writeInt(msg.entityId);
        buffer.writeBoolean(msg.isSlamming);
        buffer.writeBoolean(msg.isStorage);
        buffer.writeBoolean(msg.hasImpact);
        if (msg.hasImpact) {
            buffer.writeDouble(msg.x);
            buffer.writeDouble(msg.y);
            buffer.writeDouble(msg.z);
        }
    }

    public static S2CSyncSlamPacket decode(FriendlyByteBuf buffer) {
        int id = buffer.readInt();
        boolean slamming = buffer.readBoolean();
        boolean storage = buffer.readBoolean();
        boolean impact = buffer.readBoolean();
        double x = 0, y = 0, z = 0;
        if (impact) {
            x = buffer.readDouble();
            y = buffer.readDouble();
            z = buffer.readDouble();
        }
        return new S2CSyncSlamPacket(id, slamming, impact, x, y, z, storage);
    }

    public static void handle(S2CSyncSlamPacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ClientSlamHandler.updateSlamming(msg.entityId, msg.isSlamming, msg.isStorage);
            if (msg.hasImpact) {
                ClientSlamHandler.spawnImpactWave(new Vec3(msg.x, msg.y, msg.z));
            }
        });
    }

    @Override
    public CustomPacketPayload.Type<S2CSyncSlamPacket> type() {
        return TYPE;
    }
}
