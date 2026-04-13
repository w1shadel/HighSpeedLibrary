package com.maxwell.highspeedlib.common.network.packets.action;

import com.maxwell.highspeedlib.client.network.ClientPacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2CStartTossAnimationPacket {
    private final int entityId;

    public S2CStartTossAnimationPacket(int entityId) {
        this.entityId = entityId;
    }

    public static void encode(S2CStartTossAnimationPacket msg, FriendlyByteBuf buffer) {
        buffer.writeInt(msg.entityId);
    }

    public static S2CStartTossAnimationPacket decode(FriendlyByteBuf buffer) {
        return new S2CStartTossAnimationPacket(buffer.readInt());
    }

    public static void handle(S2CStartTossAnimationPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientPacketHandler.handleTossAnimation(msg.entityId));
        });
        ctx.get().setPacketHandled(true);
    }
}
