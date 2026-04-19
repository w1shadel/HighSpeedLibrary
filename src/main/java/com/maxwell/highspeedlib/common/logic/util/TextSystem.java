package com.maxwell.highspeedlib.common.logic.util;

import com.maxwell.highspeedlib.client.state.TextData;
import com.maxwell.highspeedlib.common.network.PacketHandler;
import com.maxwell.highspeedlib.common.network.packets.effect.S2CRenderTextPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;

public class TextSystem {
    public static void send(ServerPlayer player, TextData.Type type, String text, double x, double y, int color, int duration, float scale) {
        PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new S2CRenderTextPacket(type, Component.literal(text), x, y, color, duration, scale));
    }

    public static void sendSubtitle(ServerPlayer player, String message) {
        send(player, TextData.Type.SUBTITLE, message, 0.5, 0.8, 0xFFFFFFFF, 60, 1.1f);
    }

    public static void sendBossTitle(ServerPlayer player, String topText, String bottomText) {
        send(player, TextData.Type.TITLE, topText, 0.5, 0.4, 0xFFFFFFFF, 100, 1.5f);
        send(player, TextData.Type.TITLE, bottomText, 0.5, 0.5, 0xFFFFFFFF, 100, 4.0f);
    }
}