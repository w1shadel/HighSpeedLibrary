package com.maxwell.highspeedlib.common.network.packets;

import com.maxwell.highspeedlib.client.network.ClientPacketHandler;
import com.maxwell.highspeedlib.common.logic.ArmType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2CSyncArmPacket {
    private final int armOrdinal;

    public S2CSyncArmPacket(ArmType arm) {
        this.armOrdinal = arm.ordinal();
    }

    public S2CSyncArmPacket(int ordinal) {
        this.armOrdinal = ordinal;
    }

    public static void encode(S2CSyncArmPacket msg, FriendlyByteBuf buffer) {
        buffer.writeInt(msg.armOrdinal);
    }

    public static S2CSyncArmPacket decode(FriendlyByteBuf buffer) {
        return new S2CSyncArmPacket(buffer.readInt());
    }

    public static void handle(S2CSyncArmPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientPacketHandler.handleArmSync(ArmType.values()[msg.armOrdinal]));
        });
        ctx.get().setPacketHandled(true);
    }
}