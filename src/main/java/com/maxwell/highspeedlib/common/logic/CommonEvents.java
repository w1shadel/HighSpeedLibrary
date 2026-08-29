package com.maxwell.highspeedlib.common.logic;

import com.maxwell.highspeedlib.HighSpeedLib;
import com.maxwell.highspeedlib.api.config.HighSpeedServerConfig;
import com.maxwell.highspeedlib.common.logic.movement.SlideManager;
import com.maxwell.highspeedlib.common.logic.movement.WallJumpManager;
import com.maxwell.highspeedlib.common.logic.state.PlayerMovementState;
import com.maxwell.highspeedlib.common.logic.state.PlayerStateManager;
import com.maxwell.highspeedlib.common.network.PacketHandler;
import com.maxwell.highspeedlib.common.network.packets.sync.S2CSyncSlamPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = HighSpeedLib.MODID)
public class CommonEvents {
    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Pre event) {
        {
            TimeManager.tick();
        }
    }
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity().level().isClientSide) return;
        ServerPlayer player = (ServerPlayer) event.getEntity();
        PlayerMovementState state = PlayerStateManager.getState(player).getMovement();
        if (state.slamImpactTimer > 0) {
            state.slamImpactTimer--;
        }
        if (player.onGround()) {
            if (state.slamStorageActive) {
                state.slamStorageTimer = 15;
                state.slamStorageActive = false;
            }

            if (state.slamStorageTimer > 0) {
                state.slamStorageTimer--;
                if (state.slamStorageTimer == 0) {
                    PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, new S2CSyncSlamPacket(player.getId(), false, false));
                }
            }
            WallJumpManager.tickWallJumpReseter(player);
        }
    }

    @SubscribeEvent
    public static void onLivingJump(LivingEvent.LivingJumpEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        PlayerMovementState state = PlayerStateManager.getState(player).getMovement();

        if (state.slamStorageTimer > 0) {
            Vec3 motion = player.getDeltaMovement();

            double verticalPower = HighSpeedServerConfig.SLAM_JUMP_VERTICAL_POWER.get();
            double hMult = HighSpeedServerConfig.SLAM_JUMP_HORIZONTAL_MULT.get();

            player.setDeltaMovement(motion.x * hMult, verticalPower, motion.z * hMult);
            player.connection.send(new ClientboundSetEntityMotionPacket(player));

            state.slamStorageTimer = 0;
            player.level().playSound(null, player.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.0f, 1.2f);
            PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, new S2CSyncSlamPacket(player.getId(), false, false));
        }
    }
}
