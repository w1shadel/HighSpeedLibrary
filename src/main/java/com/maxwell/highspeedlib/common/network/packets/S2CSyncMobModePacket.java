package com.maxwell.highspeedlib.common.network.packets;

import com.maxwell.highspeedlib.api.main.mob.MobModeManager;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2CSyncMobModePacket {
    private final int entityId;
    private final boolean enraged;
    private final int radianceTier;

    public S2CSyncMobModePacket(int id, boolean enraged, int tier) {
        this.entityId = id;
        this.enraged = enraged;
        this.radianceTier = tier;
    }

    public static void encode(S2CSyncMobModePacket msg, FriendlyByteBuf buffer) {
        buffer.writeInt(msg.entityId);
        buffer.writeBoolean(msg.enraged);
        buffer.writeInt(msg.radianceTier);
    }

    public static S2CSyncMobModePacket decode(FriendlyByteBuf buffer) {
        return new S2CSyncMobModePacket(buffer.readInt(), buffer.readBoolean(), buffer.readInt());
    }

    public static void handle(S2CSyncMobModePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Entity entity = Minecraft.getInstance().level.getEntity(msg.entityId);
            if (entity instanceof LivingEntity living) {
                living.getPersistentData().putBoolean(MobModeManager.ENRAGE_TAG, msg.enraged);
                living.getPersistentData().putInt(MobModeManager.RADIANCE_TAG, msg.radianceTier);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}