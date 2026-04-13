package com.maxwell.highspeedlib.common.network.packets.sync;

import com.maxwell.highspeedlib.client.logic.ClientSlamHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2CSyncSlamPacket {
    private final int entityId;
    private final boolean isSlamming;
    private final boolean hasImpact;
    private final double x, y, z;

    public S2CSyncSlamPacket(int entityId, boolean isSlamming) {
        this(entityId, isSlamming, false, 0, 0, 0);
    }

    public S2CSyncSlamPacket(int entityId, Vec3 impactPos) {
        this(entityId, false, true, impactPos.x, impactPos.y, impactPos.z);
    }

    private S2CSyncSlamPacket(int entityId, boolean isSlamming, boolean hasImpact, double x, double y, double z) {
        this.entityId = entityId;
        this.isSlamming = isSlamming;
        this.hasImpact = hasImpact;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static void encode(S2CSyncSlamPacket msg, FriendlyByteBuf buffer) {
        buffer.writeInt(msg.entityId);
        buffer.writeBoolean(msg.isSlamming);
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
        boolean impact = buffer.readBoolean();
        if (impact) {
            return new S2CSyncSlamPacket(id, slamming, true, buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
        }
        return new S2CSyncSlamPacket(id, slamming);
    }

    public static void handle(S2CSyncSlamPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ClientSlamHandler.updateSlamming(msg.entityId, msg.isSlamming);
            if (msg.hasImpact) {
                ClientSlamHandler.spawnImpactWave(new Vec3(msg.x, msg.y, msg.z));
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
