package com.maxwell.highspeedlib.common.logic.ability;

import com.maxwell.highspeedlib.common.logic.state.PlayerAbilityState;
import com.maxwell.highspeedlib.common.logic.state.PlayerStateManager;
import com.maxwell.highspeedlib.common.network.PacketHandler;
import com.maxwell.highspeedlib.common.network.packets.sync.S2CSyncAbilitiesPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;

import java.util.UUID;

public class AbilityManager {
    public static boolean canDash(UUID uuid) {
        return PlayerStateManager.getState(uuid).getAbility().dash;
    }

    public static boolean canPunch(UUID uuid) {
        return PlayerStateManager.getState(uuid).getAbility().punch;
    }

    public static boolean canWhiplash(UUID uuid) {
        return PlayerStateManager.getState(uuid).getAbility().whiplash;
    }

    public static boolean canWallJump(UUID uuid) {
        return PlayerStateManager.getState(uuid).getAbility().wallJump;
    }

    public static boolean canSlide(UUID uuid) {
        return PlayerStateManager.getState(uuid).getAbility().sliding;
    }

    public static boolean canSlam(UUID uuid) {
        return PlayerStateManager.getState(uuid).getAbility().slam;
    }

    public static void sync(ServerPlayer player) {
        PlayerAbilityState s = PlayerStateManager.getState(player).getAbility();
        PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player),
                new S2CSyncAbilitiesPacket(
                        s.dash,
                        s.punch,
                        s.whiplash,
                        s.sliding,
                        s.slam,
                        s.wallJump,
                        s.maxCoinCount
                ));
    }
}
