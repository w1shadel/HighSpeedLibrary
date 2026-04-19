package com.maxwell.highspeedlib.common.network;

import com.maxwell.highspeedlib.HighSpeedLib;
import com.maxwell.highspeedlib.common.network.packets.action.C2SKeyInputPacket;
import com.maxwell.highspeedlib.common.network.packets.action.S2CStartPunchAnimationPacket;
import com.maxwell.highspeedlib.common.network.packets.action.S2CStartTossAnimationPacket;
import com.maxwell.highspeedlib.common.network.packets.effect.*;
import com.maxwell.highspeedlib.common.network.packets.sync.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

@SuppressWarnings("removal")
public class PacketHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(HighSpeedLib.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );
    private static int nextId = 0;

    public static void register() {
        INSTANCE.registerMessage(nextId++, S2CParryPacket.class, S2CParryPacket::encode, S2CParryPacket::decode, S2CParryPacket::handle);
        INSTANCE.registerMessage(nextId++, S2CSpeedEffectPacket.class, S2CSpeedEffectPacket::encode, S2CSpeedEffectPacket::decode, S2CSpeedEffectPacket::handle);
        INSTANCE.registerMessage(nextId++, C2SKeyInputPacket.class, C2SKeyInputPacket::encode, C2SKeyInputPacket::decode, C2SKeyInputPacket::handle);
        INSTANCE.registerMessage(nextId++, S2CStartPunchAnimationPacket.class, S2CStartPunchAnimationPacket::encode, S2CStartPunchAnimationPacket::decode, S2CStartPunchAnimationPacket::handle);
        INSTANCE.registerMessage(nextId++, S2CScreenShakePacket.class, S2CScreenShakePacket::encode, S2CScreenShakePacket::decode, S2CScreenShakePacket::handle);
        INSTANCE.registerMessage(nextId++, S2CSyncSlidePacket.class, S2CSyncSlidePacket::encode, S2CSyncSlidePacket::decode, S2CSyncSlidePacket::handle);
        INSTANCE.registerMessage(nextId++, S2CStartTossAnimationPacket.class, S2CStartTossAnimationPacket::encode, S2CStartTossAnimationPacket::decode, S2CStartTossAnimationPacket::handle);
        INSTANCE.registerMessage(nextId++, S2CSyncArmPacket.class, S2CSyncArmPacket::encode, S2CSyncArmPacket::decode, S2CSyncArmPacket::handle);
        INSTANCE.registerMessage(nextId++, S2CSyncStaminaPacket.class, S2CSyncStaminaPacket::encode, S2CSyncStaminaPacket::decode, S2CSyncStaminaPacket::handle);
        INSTANCE.registerMessage(nextId++, S2CSyncPunchEnergyPacket.class, S2CSyncPunchEnergyPacket::encode, S2CSyncPunchEnergyPacket::decode, S2CSyncPunchEnergyPacket::handle);
        INSTANCE.registerMessage(nextId++, S2CSyncWhiplashPacket.class, S2CSyncWhiplashPacket::encode, S2CSyncWhiplashPacket::decode, S2CSyncWhiplashPacket::handle);
        INSTANCE.registerMessage(nextId++, S2CSyncAbilitiesPacket.class, S2CSyncAbilitiesPacket::encode, S2CSyncAbilitiesPacket::decode, S2CSyncAbilitiesPacket::handle);
        INSTANCE.registerMessage(nextId++, S2CSyncCoinStockPacket.class, S2CSyncCoinStockPacket::encode, S2CSyncCoinStockPacket::decode, S2CSyncCoinStockPacket::handle);
        INSTANCE.registerMessage(nextId++, S2CSyncSlamPacket.class, S2CSyncSlamPacket::encode, S2CSyncSlamPacket::decode, S2CSyncSlamPacket::handle);
        INSTANCE.registerMessage(nextId++, S2CSyncMobModePacket.class, S2CSyncMobModePacket::encode, S2CSyncMobModePacket::decode, S2CSyncMobModePacket::handle);
        INSTANCE.registerMessage(nextId++, S2CBloodSplatPacket.class, S2CBloodSplatPacket::encode, S2CBloodSplatPacket::decode, S2CBloodSplatPacket::handle);
        INSTANCE.registerMessage(nextId++, S2CRenderTextPacket.class, S2CRenderTextPacket::encode, S2CRenderTextPacket::decode, S2CRenderTextPacket::handle);

    }

}