package com.maxwell.highspeedlib.client.state;

import com.maxwell.highspeedlib.common.logic.ArmType;
import com.maxwell.highspeedlib.common.network.PacketHandler;
import com.maxwell.highspeedlib.common.network.packets.S2CSyncArmPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ArmManager {
    private static final Map<UUID, ArmType> playerArms = new HashMap<>();

    public static ArmType getArm(Player player) {
        return playerArms.getOrDefault(player.getUUID(), ArmType.FEEDBACKER);
    }

    public static void switchArm(ServerPlayer player) {
        ArmType current = getArm(player);
        ArmType next = (current == ArmType.FEEDBACKER) ? ArmType.KNUCKLEBLASTER : ArmType.FEEDBACKER;
        playerArms.put(player.getUUID(), next);
        PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new S2CSyncArmPacket(next));
    }
}