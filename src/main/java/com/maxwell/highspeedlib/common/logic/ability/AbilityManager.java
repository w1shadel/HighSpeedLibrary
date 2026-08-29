package com.maxwell.highspeedlib.common.logic.ability;

import com.maxwell.highspeedlib.api.config.HighSpeedServerConfig;
import com.maxwell.highspeedlib.common.logic.state.PlayerAbilityState;
import com.maxwell.highspeedlib.common.logic.state.PlayerStateManager;
import com.maxwell.highspeedlib.common.network.PacketHandler;
import com.maxwell.highspeedlib.common.network.packets.sync.S2CSyncAbilitiesPacket;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.UUID;

public class AbilityManager {
    public static boolean canDash(UUID uuid) {
        return HighSpeedServerConfig.ABILITY_DASH.get() && PlayerStateManager.getState(uuid).getAbility().dash;
    }

    public static boolean canPunch(UUID uuid) {
        return HighSpeedServerConfig.ABILITY_PUNCH.get() && PlayerStateManager.getState(uuid).getAbility().punch;
    }

    public static boolean canWhiplash(UUID uuid) {
        return HighSpeedServerConfig.ABILITY_WHIPLASH.get() && PlayerStateManager.getState(uuid).getAbility().whiplash;
    }

    public static boolean canWallJump(UUID uuid) {
        return HighSpeedServerConfig.ABILITY_WALLJUMP.get() && PlayerStateManager.getState(uuid).getAbility().wallJump;
    }

    public static boolean canSlide(UUID uuid) {
        return HighSpeedServerConfig.ABILITY_SLIDE.get() && PlayerStateManager.getState(uuid).getAbility().sliding;
    }

    public static boolean canSlam(UUID uuid) {
        return HighSpeedServerConfig.ABILITY_SLAM.get() && PlayerStateManager.getState(uuid).getAbility().slam;
    }

    public static void sync(ServerPlayer player) {
        PlayerAbilityState s = PlayerStateManager.getState(player).getAbility();
        PacketDistributor.sendToPlayer(player, new S2CSyncAbilitiesPacket(
                        HighSpeedServerConfig.ABILITY_DASH.get() && s.dash,
                        HighSpeedServerConfig.ABILITY_PUNCH.get() && s.punch,
                        HighSpeedServerConfig.ABILITY_WHIPLASH.get() && s.whiplash,
                        HighSpeedServerConfig.ABILITY_SLIDE.get() && s.sliding,
                        HighSpeedServerConfig.ABILITY_SLAM.get() && s.slam,
                        HighSpeedServerConfig.ABILITY_WALLJUMP.get() && s.wallJump,
                        s.maxCoinCount
                ));
    }
}
