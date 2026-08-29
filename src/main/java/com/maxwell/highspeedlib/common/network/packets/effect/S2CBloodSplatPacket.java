package com.maxwell.highspeedlib.common.network.packets.effect;

import com.maxwell.highspeedlib.HighSpeedLib;

import com.maxwell.highspeedlib.client.renderer.BloodRenderManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.minecraft.world.phys.Vec3;




public class S2CBloodSplatPacket implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<S2CBloodSplatPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(HighSpeedLib.MODID, "s2c_blood_splat_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, S2CBloodSplatPacket> STREAM_CODEC = StreamCodec.of((RegistryFriendlyByteBuf buf, S2CBloodSplatPacket msg) -> S2CBloodSplatPacket.encode(msg, buf), S2CBloodSplatPacket::decode);

    @Override
    public CustomPacketPayload.Type<S2CBloodSplatPacket> type() { return TYPE; }
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

    public static void handle(S2CBloodSplatPacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> BloodRenderManager.spawnBloodSpray(msg.pos, msg.dir, msg.bloods));
    }
}
