package com.maxwell.highspeedlib.common.network.packets;

import com.maxwell.highspeedlib.client.renderer.UltraHudRenderer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2CSyncCoinStockPacket {
    private final double coinStock;

    public S2CSyncCoinStockPacket(double coinStock) {
        this.coinStock = coinStock;
    }

    public static void encode(S2CSyncCoinStockPacket msg, FriendlyByteBuf buffer) {
        buffer.writeDouble(msg.coinStock);
    }

    public static S2CSyncCoinStockPacket decode(FriendlyByteBuf buffer) {
        return new S2CSyncCoinStockPacket(buffer.readDouble());
    }

    public static void handle(S2CSyncCoinStockPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            UltraHudRenderer.setClientCoinStock(msg.coinStock);
        });
        ctx.get().setPacketHandled(true);
    }
}