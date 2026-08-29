package com.maxwell.highspeedlib.common.network.packets.sync;

import com.maxwell.highspeedlib.HighSpeedLib;

import com.maxwell.highspeedlib.api.main.mob.MobModeManager;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;




public class S2CSyncMobModePacket implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<S2CSyncMobModePacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(HighSpeedLib.MODID, "s2c_sync_mob_mode_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, S2CSyncMobModePacket> STREAM_CODEC = StreamCodec.of((RegistryFriendlyByteBuf buf, S2CSyncMobModePacket msg) -> S2CSyncMobModePacket.encode(msg, buf), S2CSyncMobModePacket::decode);

    @Override
    public CustomPacketPayload.Type<S2CSyncMobModePacket> type() { return TYPE; }
    private final int entityId;
    private final boolean enraged;
    private final int radianceTier;
    private final boolean isBoss;

    public S2CSyncMobModePacket(int id, boolean enraged, int tier, boolean isBoss) {
        this.entityId = id;
        this.enraged = enraged;
        this.radianceTier = tier;
        this.isBoss = isBoss;
    }

    public static void encode(S2CSyncMobModePacket msg, FriendlyByteBuf buffer) {
        buffer.writeInt(msg.entityId);
        buffer.writeBoolean(msg.enraged);
        buffer.writeInt(msg.radianceTier);
        buffer.writeBoolean(msg.isBoss);
    }

    public static S2CSyncMobModePacket decode(FriendlyByteBuf buffer) {
        return new S2CSyncMobModePacket(buffer.readInt(), buffer.readBoolean(), buffer.readInt(), buffer.readBoolean());
    }

    public static void handle(S2CSyncMobModePacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Entity entity = Minecraft.getInstance().level.getEntity(msg.entityId);
            if (entity instanceof LivingEntity living) {
                living.getPersistentData().putBoolean(MobModeManager.ENRAGE_TAG, msg.enraged);
                living.getPersistentData().putInt(MobModeManager.RADIANCE_TAG, msg.radianceTier);
                living.getPersistentData().putBoolean(MobModeManager.BOSS_TAG, msg.isBoss);
            }
        });
    }
}
