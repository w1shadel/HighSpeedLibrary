package com.maxwell.highspeedlib.common.logic.movement;

import com.maxwell.highspeedlib.api.config.HighSpeedServerConfig;
import com.maxwell.highspeedlib.common.logic.state.PlayerAbilityState;
import com.maxwell.highspeedlib.common.logic.state.PlayerMovementState;
import com.maxwell.highspeedlib.common.logic.state.PlayerStateManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class WallJumpManager {
    public static void performWallJump(ServerPlayer player) {
        PlayerAbilityState settings = PlayerStateManager.getState(player).getAbility();
        PlayerMovementState state = PlayerStateManager.getState(player).getMovement();
        if (net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new com.maxwell.highspeedlib.api.HighSpeedAbilityEvent.Walljump(player))) {
            return;
        }
        if (!settings.wallJump) return;
        if (player.isInWater() || player.isInLava()) return;
        if (!player.onGround() || player.fallDistance > 0.01) {
            if (state.wallJumpCount < settings.maxWallJumpCount) {
                Direction wallDir = getTouchingWall(player);
                if (wallDir != null) {
                    double baseJumpPower = player.getAttributeValue(Attributes.JUMP_STRENGTH);
                    double jumpBoostBonus = 0.0D;
                    if (player.hasEffect(MobEffects.JUMP)) {
                        jumpBoostBonus = (double) ((float) (player.getEffect(MobEffects.JUMP).getAmplifier() + 1) * 0.1F);
                    }
                    double finalJumpHeight = (baseJumpPower + jumpBoostBonus) * HighSpeedServerConfig.WALLJUMP_VERTICAL_MULT.get();
                    double hPower = HighSpeedServerConfig.WALLJUMP_HORIZONTAL_POWER.get();
                    Vec3 push = new Vec3(wallDir.getOpposite().getStepX(), 0.0, wallDir.getOpposite().getStepZ())
                            .normalize().scale(hPower);
                    player.setDeltaMovement(push.x, finalJumpHeight, push.z);
                    player.hurtMarked = true;
                    state.wallJumpCount++;
                    player.connection.send(new ClientboundSetEntityMotionPacket(player));
                    player.level().playSound(null, player.blockPosition(),
                            SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0f, 1.8f);
                }
            }
        }
    }

    public static Direction getTouchingWall(Player player) {
        double offset = 0.45D;
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockPos pos = BlockPos.containing(
                    player.getX() + dir.getStepX() * offset,
                    player.getY() + 0.8D,
                    player.getZ() + dir.getStepZ() * offset
            );
            BlockState state = player.level().getBlockState(pos);
            if (state.getFluidState().isEmpty() && !state.getCollisionShape(player.level(), pos).isEmpty()) {
                return dir;
            }
        }
        return null;
    }
}