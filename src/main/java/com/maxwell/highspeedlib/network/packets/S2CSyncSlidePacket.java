package com.maxwell.highspeedlib.network.packets;

import com.maxwell.highspeedlib.client.ClientSlideHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2CSyncSlidePacket {
    private final boolean sliding;

    public S2CSyncSlidePacket(boolean sliding) {
        this.sliding = sliding;
    }

    public static void encode(S2CSyncSlidePacket msg, FriendlyByteBuf buffer) {
        buffer.writeBoolean(msg.sliding);
    }

    public static S2CSyncSlidePacket decode(FriendlyByteBuf buffer) {
        return new S2CSyncSlidePacket(buffer.readBoolean());
    }

    public static void handle(S2CSyncSlidePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientSlideHandler.setSliding(msg.sliding));
        });
        ctx.get().setPacketHandled(true);
    }
}
