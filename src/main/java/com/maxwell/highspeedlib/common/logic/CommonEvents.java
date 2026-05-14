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
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(modid = HighSpeedLib.MODID)
public class CommonEvents {
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            TimeManager.tick();
        }
    }
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) return;
        ServerPlayer player = (ServerPlayer) event.player;
        PlayerMovementState state = PlayerStateManager.getState(player).getMovement();

        if (player.onGround()) {
            if (state.slamStorageActive) {
                state.slamStorageTimer = 15;
                state.slamStorageActive = false;
            }

            if (state.slamStorageTimer > 0) {
                state.slamStorageTimer--;
                if (state.slamStorageTimer == 0) {
                    PacketHandler.INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
                            new S2CSyncSlamPacket(player.getId(), false, false));
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
            player.setDeltaMovement(motion.x, 6.2D, motion.z);
            player.connection.send(new ClientboundSetEntityMotionPacket(player));

            state.slamStorageTimer = 0;
            player.level().playSound(null, player.blockPosition(),
                    SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.0f, 1.2f);
            state.slamStorageTimer = 0;
            PacketHandler.INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
                    new S2CSyncSlamPacket(player.getId(), false, false)); 
        }
    }
}
