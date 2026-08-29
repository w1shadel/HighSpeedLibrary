package com.maxwell.highspeedlib.common.network.packets.sync;

import com.maxwell.highspeedlib.HighSpeedLib;
import com.maxwell.highspeedlib.client.logic.ClientWhiplashManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class S2CSyncWhiplashPacket implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<S2CSyncWhiplashPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(HighSpeedLib.MODID, "s2c_sync_whiplash_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, S2CSyncWhiplashPacket> STREAM_CODEC = StreamCodec.of((RegistryFriendlyByteBuf buf, S2CSyncWhiplashPacket msg) -> S2CSyncWhiplashPacket.encode(msg, buf), S2CSyncWhiplashPacket::decode);
    public final java.util.UUID playerUuid;
    public final int state;
    public final double distance;
    public final int targetId;
    public final Vec3 hitPos;
    public final Vec3 shootDir;
    public S2CSyncWhiplashPacket(java.util.UUID playerUuid, int state, double distance, int targetId, Vec3 hitPos, Vec3 shootDir) {
        this.playerUuid = playerUuid;
        this.state = state;
        this.distance = distance;
        this.targetId = targetId;
        this.hitPos = hitPos;
        this.shootDir = shootDir;
    }

    public static void encode(S2CSyncWhiplashPacket msg, FriendlyByteBuf buffer) {
        buffer.writeUUID(msg.playerUuid);
        buffer.writeInt(msg.state);
        buffer.writeDouble(msg.distance);
        buffer.writeInt(msg.targetId);
        boolean hasHitPos = msg.hitPos != null;
        buffer.writeBoolean(hasHitPos);
        if (hasHitPos) {
            buffer.writeDouble(msg.hitPos.x);
            buffer.writeDouble(msg.hitPos.y);
            buffer.writeDouble(msg.hitPos.z);
        }
        buffer.writeDouble(msg.shootDir.x);
        buffer.writeDouble(msg.shootDir.y);
        buffer.writeDouble(msg.shootDir.z);
    }

    public static S2CSyncWhiplashPacket decode(FriendlyByteBuf buffer) {
        java.util.UUID playerUuid = buffer.readUUID();
        int state = buffer.readInt();
        double distance = buffer.readDouble();
        int targetId = buffer.readInt();
        boolean hasHitPos = buffer.readBoolean();
        Vec3 hitPos = null;
        if (hasHitPos) {
            hitPos = new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
        }
        Vec3 shootDir = new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
        return new S2CSyncWhiplashPacket(playerUuid, state, distance, targetId, hitPos, shootDir);
    }

    public static void handle(S2CSyncWhiplashPacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ClientWhiplashManager.handleSync(msg);
        });
    }

    @Override
    public CustomPacketPayload.Type<S2CSyncWhiplashPacket> type() {
        return TYPE;
    }
}
