package com.maxwell.highspeedlib.common.network.packets.sync;

import com.maxwell.highspeedlib.agent.AbsoluteState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2CSyncAbsoluteStatePacket {
    private final int entityId;
    private final float health;
    private final boolean isErased;

    public S2CSyncAbsoluteStatePacket(int entityId, float health, boolean isErased) {
        this.entityId = entityId;
        this.health = health;
        this.isErased = isErased;
    }

    public static void encode(S2CSyncAbsoluteStatePacket msg, FriendlyByteBuf buffer) {
        buffer.writeInt(msg.entityId);
        buffer.writeFloat(msg.health);
        buffer.writeBoolean(msg.isErased);
    }

    public static S2CSyncAbsoluteStatePacket decode(FriendlyByteBuf buffer) {
        return new S2CSyncAbsoluteStatePacket(
                buffer.readInt(),
                buffer.readFloat(),
                buffer.readBoolean()
        );
    }

    public static void handle(S2CSyncAbsoluteStatePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {

            if (FMLEnvironment.dist.isClient()) {

                AbsoluteState.idHealthMap.put(msg.entityId, msg.health);
                if (msg.isErased) {
                    AbsoluteState.idRemovedMap.put(msg.entityId, true);
                } else {
                    AbsoluteState.idRemovedMap.remove(msg.entityId);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}