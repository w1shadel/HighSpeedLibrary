package com.maxwell.highspeedlib.common.network.packets;

import com.maxwell.highspeedlib.client.network.ClientPacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2CSyncStaminaPacket {
    private final double stamina;
    private final double maxStamina;

    public S2CSyncStaminaPacket(double stamina, double maxStamina) {
        this.stamina = stamina;
        this.maxStamina = maxStamina;
    }

    public static void encode(S2CSyncStaminaPacket msg, FriendlyByteBuf buffer) {
        buffer.writeDouble(msg.stamina);
        buffer.writeDouble(msg.maxStamina);
    }

    public static S2CSyncStaminaPacket decode(FriendlyByteBuf buffer) {
        return new S2CSyncStaminaPacket(buffer.readDouble(), buffer.readDouble());
    }

    public static void handle(S2CSyncStaminaPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientPacketHandler.handleStaminaSync(msg.stamina, msg.maxStamina));
        });
        ctx.get().setPacketHandled(true);
    }
}