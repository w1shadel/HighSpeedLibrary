package com.maxwell.highspeedlib.network.packets;

import com.maxwell.highspeedlib.logic.ParrySystem;
import com.maxwell.highspeedlib.logic.SlideManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class C2SKeyInputPacket {
    private final int keyType;

    public C2SKeyInputPacket(int keyType) {
        this.keyType = keyType;
    }

    public static void encode(C2SKeyInputPacket msg, FriendlyByteBuf buffer) {
        buffer.writeInt(msg.keyType);
    }

    public static C2SKeyInputPacket decode(FriendlyByteBuf buffer) {
        return new C2SKeyInputPacket(buffer.readInt());
    }

    public static void handle(C2SKeyInputPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            if (msg.keyType == 0) {
                Vec3 look = player.getLookAngle();
                player.setDeltaMovement(look.x * 1.8, 0.2, look.z * 1.8);
                player.hurtMarked = true;
                player.invulnerableTime = 10;
            } else if (msg.keyType == 1) {
                ParrySystem.attemptParry(player);
            } else if (msg.keyType == 3) {
                SlideManager.toggleSlide(player, true);
            } else if (msg.keyType == 4) {
                SlideManager.toggleSlide(player, false);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}