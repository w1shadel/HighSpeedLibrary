package com.maxwell.highspeedlib.common.network.packets.sync;

import com.maxwell.highspeedlib.HighSpeedLib;
import com.maxwell.highspeedlib.client.network.ClientPacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class S2CSyncStaminaPacket implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<S2CSyncStaminaPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(HighSpeedLib.MODID, "s2c_sync_stamina_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, S2CSyncStaminaPacket> STREAM_CODEC = StreamCodec.of((RegistryFriendlyByteBuf buf, S2CSyncStaminaPacket msg) -> S2CSyncStaminaPacket.encode(msg, buf), S2CSyncStaminaPacket::decode);
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

    public static void handle(S2CSyncStaminaPacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ClientPacketHandler.handleStaminaSync(msg.stamina, msg.maxStamina);
        });
    }

    @Override
    public CustomPacketPayload.Type<S2CSyncStaminaPacket> type() {
        return TYPE;
    }
}
