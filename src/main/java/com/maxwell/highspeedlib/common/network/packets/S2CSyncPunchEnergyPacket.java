package com.maxwell.highspeedlib.common.network.packets;

import com.maxwell.highspeedlib.client.network.ClientPacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2CSyncPunchEnergyPacket {
    private final double energy;

    public S2CSyncPunchEnergyPacket(double energy) {
        this.energy = energy;
    }

    public static void encode(S2CSyncPunchEnergyPacket msg, FriendlyByteBuf buffer) {
        buffer.writeDouble(msg.energy);
    }

    public static S2CSyncPunchEnergyPacket decode(FriendlyByteBuf buffer) {
        return new S2CSyncPunchEnergyPacket(buffer.readDouble());
    }

    public static void handle(S2CSyncPunchEnergyPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientPacketHandler.handlePunchEnergySync(msg.energy));
        });
        ctx.get().setPacketHandled(true);
    }
}
