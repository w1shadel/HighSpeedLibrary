package com.maxwell.highspeedlib.common.network.packets.effect;

import com.maxwell.highspeedlib.HighSpeedLib;
import com.maxwell.highspeedlib.client.network.ClientPacketHandler;
import com.maxwell.highspeedlib.client.state.TextData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class S2CRenderTextPacket implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<S2CRenderTextPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(HighSpeedLib.MODID, "s2c_render_text_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, S2CRenderTextPacket> STREAM_CODEC = StreamCodec.of((RegistryFriendlyByteBuf buf, S2CRenderTextPacket msg) -> S2CRenderTextPacket.encode(msg, buf), S2CRenderTextPacket::decode);
    private final TextData.Type type;
    private final Component text;
    private final double x, y;
    private final int color;
    private final int duration;
    private final float scale;
    public S2CRenderTextPacket(TextData.Type type, Component text, double x, double y, int color, int duration, float scale) {
        this.type = type;
        this.text = text;
        this.x = x;
        this.y = y;
        this.color = color;
        this.duration = duration;
        this.scale = scale;
    }

    public static void encode(S2CRenderTextPacket msg, FriendlyByteBuf buf) {
        buf.writeEnum(msg.type);
        buf.writeJsonWithCodec(ComponentSerialization.CODEC, msg.text);
        buf.writeDouble(msg.x);
        buf.writeDouble(msg.y);
        buf.writeInt(msg.color);
        buf.writeInt(msg.duration);
        buf.writeFloat(msg.scale);
    }

    public static S2CRenderTextPacket decode(FriendlyByteBuf buf) {
        return new S2CRenderTextPacket(
                buf.readEnum(TextData.Type.class),
                buf.readJsonWithCodec(ComponentSerialization.CODEC),
                buf.readDouble(),
                buf.readDouble(),
                buf.readInt(),
                buf.readInt(),
                buf.readFloat()
        );
    }

    public static void handle(S2CRenderTextPacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ClientPacketHandler.handleRenderText(msg.type, msg.text, msg.x, msg.y, msg.color, msg.duration, msg.scale);
        });
    }

    @Override
    public CustomPacketPayload.Type<S2CRenderTextPacket> type() {
        return TYPE;
    }
}