package com.maxwell.highspeedlib.common.network.packets.sync;

import com.maxwell.highspeedlib.client.network.ClientPacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2CSyncSlidePacket {
    private final int entityId;
    private final boolean sliding;

    public S2CSyncSlidePacket(int entityId, boolean sliding) {
        this.entityId = entityId;
        this.sliding = sliding;
    }

    public static void encode(S2CSyncSlidePacket msg, FriendlyByteBuf buffer) {
        buffer.writeInt(msg.entityId);
        buffer.writeBoolean(msg.sliding);
    }

    public static S2CSyncSlidePacket decode(FriendlyByteBuf buffer) {
        return new S2CSyncSlidePacket(buffer.readInt(), buffer.readBoolean());
    }

    public static void handle(S2CSyncSlidePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientPacketHandler.handleSlideSync(msg.entityId, msg.sliding));
        });
        ctx.get().setPacketHandled(true);
    }
}
