package com.maxwell.highspeedlib.common.network.packets.effect;

import com.maxwell.highspeedlib.client.renderer.BloodRenderManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2CBloodSplatPacket {
    private final Vec3 pos;
    private final Vec3 dir;
    private final int bloods;

    public S2CBloodSplatPacket(Vec3 pos, Vec3 dir, int bloods) {
        this.pos = pos;
        this.dir = dir;
        this.bloods = bloods;
    }

    public static void encode(S2CBloodSplatPacket msg, FriendlyByteBuf buffer) {
        buffer.writeDouble(msg.pos.x);
        buffer.writeDouble(msg.pos.y);
        buffer.writeDouble(msg.pos.z);
        buffer.writeDouble(msg.dir.x);
        buffer.writeDouble(msg.dir.y);
        buffer.writeDouble(msg.dir.z);
        buffer.writeInt(msg.bloods);
    }

    public static S2CBloodSplatPacket decode(FriendlyByteBuf buffer) {
        return new S2CBloodSplatPacket(
                new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble()),
                new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble()),
                buffer.readInt());
    }

    public static void handle(S2CBloodSplatPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> BloodRenderManager.spawnBloodSpray(msg.pos, msg.dir, msg.bloods));
        ctx.get().setPacketHandled(true);
    }
}
