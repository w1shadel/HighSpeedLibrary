package com.maxwell.highspeedlib.common.network.packets.sync;

import com.maxwell.highspeedlib.HighSpeedLib;

import com.maxwell.highspeedlib.client.renderer.UltraHudRenderer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;




public class S2CSyncCoinStockPacket implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<S2CSyncCoinStockPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(HighSpeedLib.MODID, "s2c_sync_coin_stock_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, S2CSyncCoinStockPacket> STREAM_CODEC = StreamCodec.of((RegistryFriendlyByteBuf buf, S2CSyncCoinStockPacket msg) -> S2CSyncCoinStockPacket.encode(msg, buf), S2CSyncCoinStockPacket::decode);

    @Override
    public CustomPacketPayload.Type<S2CSyncCoinStockPacket> type() { return TYPE; }
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

    public static void handle(S2CSyncCoinStockPacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            UltraHudRenderer.setClientCoinStock(msg.coinStock);
        });
    }
}
