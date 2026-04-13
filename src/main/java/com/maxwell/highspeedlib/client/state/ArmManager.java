package com.maxwell.highspeedlib.client.state;

import com.maxwell.highspeedlib.common.logic.combat.ArmType;
import com.maxwell.highspeedlib.common.network.PacketHandler;
import com.maxwell.highspeedlib.common.network.packets.sync.S2CSyncArmPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.PacketDistributor;

import java.util.UUID;

public class ArmManager {

    public static ArmType getArm(Player player) {
        ArmType current = ClientStateManager.getPlayerState(player).currentArm;
        if (current == null) current = ArmType.FEEDBACKER;
        return current;
    }

    public static void switchArm(ServerPlayer player) {
        ArmType current = getArm(player);
        ArmType next = (current == ArmType.FEEDBACKER) ? ArmType.KNUCKLEBLASTER : ArmType.FEEDBACKER;
        ClientStateManager.getPlayerState(player.getUUID()).currentArm = next;
        PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new S2CSyncArmPacket(next));
    }
}