package com.maxwell.highspeedlib.common.network.packets;

import com.maxwell.highspeedlib.client.network.ClientPacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2CParryPacket {
    public S2CParryPacket() {
    }

    public static void encode(S2CParryPacket msg, FriendlyByteBuf buffer) {
    }

    public static S2CParryPacket decode(FriendlyByteBuf buffer) {
        return new S2CParryPacket();
    }

    public static void handle(S2CParryPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientPacketHandler.handleParry());
        });
        ctx.get().setPacketHandled(true);
    }
}