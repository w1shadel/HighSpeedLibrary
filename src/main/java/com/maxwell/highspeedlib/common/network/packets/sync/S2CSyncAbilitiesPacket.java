package com.maxwell.highspeedlib.common.network.packets.sync;

import com.maxwell.highspeedlib.HighSpeedLib;
import com.maxwell.highspeedlib.client.renderer.UltraHudRenderer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class S2CSyncAbilitiesPacket implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<S2CSyncAbilitiesPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(HighSpeedLib.MODID, "s2c_sync_abilities_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, S2CSyncAbilitiesPacket> STREAM_CODEC = StreamCodec.of((RegistryFriendlyByteBuf buf, S2CSyncAbilitiesPacket msg) -> S2CSyncAbilitiesPacket.encode(msg, buf), S2CSyncAbilitiesPacket::decode);
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

    public static void handle(S2CSyncAbilitiesPacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            UltraHudRenderer.dashUnlocked = msg.dash;
            UltraHudRenderer.punchUnlocked = msg.punch;
            UltraHudRenderer.whiplashUnlocked = msg.whiplash;
            UltraHudRenderer.slidingUnlocked = msg.sliding;
            UltraHudRenderer.slamUnlocked = msg.slam;
            UltraHudRenderer.walljumpUnlocked = msg.walljump;
            UltraHudRenderer.setMaxCoins(msg.maxCoins);
        });
    }

    @Override
    public CustomPacketPayload.Type<S2CSyncAbilitiesPacket> type() {
        return TYPE;
    }
}
