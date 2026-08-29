package com.maxwell.highspeedlib.common.network;

import com.maxwell.highspeedlib.common.network.packets.action.C2SKeyInputPacket;
import com.maxwell.highspeedlib.common.network.packets.action.S2CStartPunchAnimationPacket;
import com.maxwell.highspeedlib.common.network.packets.action.S2CStartTossAnimationPacket;
import com.maxwell.highspeedlib.common.network.packets.effect.*;
import com.maxwell.highspeedlib.common.network.packets.sync.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public final class PacketHandler {
    private PacketHandler() {}

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(PacketHandler::registerPayloads);
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("1");
        registrar.playToServer(C2SKeyInputPacket.TYPE, C2SKeyInputPacket.STREAM_CODEC, C2SKeyInputPacket::handle);
        registrar.playToClient(S2CParryPacket.TYPE, S2CParryPacket.STREAM_CODEC, S2CParryPacket::handle);
        registrar.playToClient(S2CSpeedEffectPacket.TYPE, S2CSpeedEffectPacket.STREAM_CODEC, S2CSpeedEffectPacket::handle);
        registrar.playToClient(S2CStartPunchAnimationPacket.TYPE, S2CStartPunchAnimationPacket.STREAM_CODEC, S2CStartPunchAnimationPacket::handle);
        registrar.playToClient(S2CScreenShakePacket.TYPE, S2CScreenShakePacket.STREAM_CODEC, S2CScreenShakePacket::handle);
        registrar.playToClient(S2CSyncSlidePacket.TYPE, S2CSyncSlidePacket.STREAM_CODEC, S2CSyncSlidePacket::handle);
        registrar.playToClient(S2CStartTossAnimationPacket.TYPE, S2CStartTossAnimationPacket.STREAM_CODEC, S2CStartTossAnimationPacket::handle);
        registrar.playToClient(S2CSyncArmPacket.TYPE, S2CSyncArmPacket.STREAM_CODEC, S2CSyncArmPacket::handle);
        registrar.playToClient(S2CSyncStaminaPacket.TYPE, S2CSyncStaminaPacket.STREAM_CODEC, S2CSyncStaminaPacket::handle);
        registrar.playToClient(S2CSyncPunchEnergyPacket.TYPE, S2CSyncPunchEnergyPacket.STREAM_CODEC, S2CSyncPunchEnergyPacket::handle);
        registrar.playToClient(S2CSyncWhiplashPacket.TYPE, S2CSyncWhiplashPacket.STREAM_CODEC, S2CSyncWhiplashPacket::handle);
        registrar.playToClient(S2CSyncAbilitiesPacket.TYPE, S2CSyncAbilitiesPacket.STREAM_CODEC, S2CSyncAbilitiesPacket::handle);
        registrar.playToClient(S2CSyncCoinStockPacket.TYPE, S2CSyncCoinStockPacket.STREAM_CODEC, S2CSyncCoinStockPacket::handle);
        registrar.playToClient(S2CSyncSlamPacket.TYPE, S2CSyncSlamPacket.STREAM_CODEC, S2CSyncSlamPacket::handle);
        registrar.playToClient(S2CSyncMobModePacket.TYPE, S2CSyncMobModePacket.STREAM_CODEC, S2CSyncMobModePacket::handle);
        registrar.playToClient(S2CBloodSplatPacket.TYPE, S2CBloodSplatPacket.STREAM_CODEC, S2CBloodSplatPacket::handle);
        registrar.playToClient(S2CRenderTextPacket.TYPE, S2CRenderTextPacket.STREAM_CODEC, S2CRenderTextPacket::handle);
    }
}
