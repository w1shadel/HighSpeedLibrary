package com.maxwell.highspeedlib.common.network.packets.effect;

import com.maxwell.highspeedlib.client.network.ClientPacketHandler;
import com.maxwell.highspeedlib.client.state.TextData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2CRenderTextPacket {
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
        buf.writeComponent(msg.text);
        buf.writeDouble(msg.x);
        buf.writeDouble(msg.y);
        buf.writeInt(msg.color);
        buf.writeInt(msg.duration);
        buf.writeFloat(msg.scale);
    }

    public static S2CRenderTextPacket decode(FriendlyByteBuf buf) {
        return new S2CRenderTextPacket(
                buf.readEnum(TextData.Type.class),
                buf.readComponent(),
                buf.readDouble(),
                buf.readDouble(),
                buf.readInt(),
                buf.readInt(),
                buf.readFloat()
        );
    }

    public static void handle(S2CRenderTextPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientPacketHandler.handleRenderText(msg.type, msg.text, msg.x, msg.y, msg.color, msg.duration, msg.scale));
        });
        ctx.get().setPacketHandled(true);
    }
}