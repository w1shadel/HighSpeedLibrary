package com.maxwell.highspeedlib.common.network.packets;

import com.maxwell.highspeedlib.client.renderer.UltraHudRenderer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2CSyncAbilitiesPacket {
    private final boolean dash;
    private final boolean punch;
    private final boolean whiplash;
    private final boolean sliding;
    private final boolean slam;
    private final boolean walljump;
    private final int maxCoins;

    public S2CSyncAbilitiesPacket(boolean dash, boolean punch, boolean whiplash, boolean sliding, boolean slam, boolean walljump, int maxConis) {
        this.dash = dash;
        this.punch = punch;
        this.whiplash = whiplash;
        this.sliding = sliding;
        this.slam = slam;
        this.walljump = walljump;
        this.maxCoins = maxConis;
    }

    public static void encode(S2CSyncAbilitiesPacket msg, FriendlyByteBuf buffer) {
        buffer.writeBoolean(msg.dash);
        buffer.writeBoolean(msg.punch);
        buffer.writeBoolean(msg.whiplash);
        buffer.writeBoolean(msg.sliding);
        buffer.writeBoolean(msg.slam);
        buffer.writeBoolean(msg.walljump);
        buffer.writeInt(msg.maxCoins);
    }

    public static S2CSyncAbilitiesPacket decode(FriendlyByteBuf buffer) {
        return new S2CSyncAbilitiesPacket(buffer.readBoolean(), buffer.readBoolean(), buffer.readBoolean(), buffer.readBoolean(), buffer.readBoolean(), buffer.readBoolean(), buffer.readInt());
    }

    public static void handle(S2CSyncAbilitiesPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            UltraHudRenderer.dashUnlocked = msg.dash;
            UltraHudRenderer.punchUnlocked = msg.punch;
            UltraHudRenderer.whiplashUnlocked = msg.whiplash;
            UltraHudRenderer.slidingUnlocked = msg.sliding;
            UltraHudRenderer.slamUnlocked = msg.slam;
            UltraHudRenderer.walljumpUnlocked = msg.walljump;
            UltraHudRenderer.setClientMaxCoins(msg.maxCoins);
        });
        ctx.get().setPacketHandled(true);
    }
}