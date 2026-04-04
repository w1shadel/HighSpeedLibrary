package com.maxwell.highspeedlib.network.packets;

import com.maxwell.highspeedlib.client.ThirdPersonPunchManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2CStartPunchAnimationPacket {
    private final int entityId;

    public S2CStartPunchAnimationPacket(int entityId) {
        this.entityId = entityId;
    }

    public static void encode(S2CStartPunchAnimationPacket msg, FriendlyByteBuf buffer) {
        buffer.writeInt(msg.entityId);
    }

    public static S2CStartPunchAnimationPacket decode(FriendlyByteBuf buffer) {
        return new S2CStartPunchAnimationPacket(buffer.readInt());
    }

    public static void handle(S2CStartPunchAnimationPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ThirdPersonPunchManager.startAnimation(msg.entityId);
        });
        ctx.get().setPacketHandled(true);
    }
}