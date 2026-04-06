package com.maxwell.highspeedlib.common.network.packets;

import com.maxwell.highspeedlib.client.state.ArmManager;
import com.maxwell.highspeedlib.common.logic.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class C2SKeyInputPacket {
    private final int keyType;
    private final float xInput;
    private final float zInput;

    public C2SKeyInputPacket(int keyType) {
        this(keyType, 0f, 0f);
    }

    public C2SKeyInputPacket(int keyType, float xInput, float zInput) {
        this.keyType = keyType;
        this.xInput = xInput;
        this.zInput = zInput;
    }

    public static void encode(C2SKeyInputPacket msg, FriendlyByteBuf buffer) {
        buffer.writeInt(msg.keyType);
        buffer.writeFloat(msg.xInput);
        buffer.writeFloat(msg.zInput);
    }

    public static C2SKeyInputPacket decode(FriendlyByteBuf buffer) {
        return new C2SKeyInputPacket(buffer.readInt(), buffer.readFloat(), buffer.readFloat());
    }

    public static void handle(C2SKeyInputPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            if (msg.keyType == 0) {
                if (StaminaManager.consumeStamina(player, 1.0)) {
                    float x = msg.xInput;
                    float z = msg.zInput;
                    Vec3 dashVec;
                    if (x == 0 && z == 0) {
                        dashVec = player.getLookAngle().multiply(1, 0, 1).normalize();
                    } else {
                        float yaw = player.getYRot();
                        float f1 = (float) Math.sin(yaw * (Math.PI / 180.0));
                        float f2 = (float) Math.cos(yaw * (Math.PI / 180.0));
                        dashVec = new Vec3(x * f2 - z * f1, 0, z * f2 + x * f1).normalize();
                    }
                    player.setDeltaMovement(dashVec.x * 1.8, 0.2, dashVec.z * 1.8);
                    player.hurtMarked = true;
                    DashManager.startDashInvulnerability(player, 10);
                }
            } else if (msg.keyType == 1) {
                ServerArmManager.attemptPunch(player);
            } else if (msg.keyType == 3) {
                if (player.onGround()) {
                    SlideManager.toggleSlide(player, true, msg.xInput, msg.zInput);
                } else {
                    SlamManager.startSlam(player);
                }
            } else if (msg.keyType == 4) {
                SlideManager.toggleSlide(player, false, 0, 0);
                SlamManager.stopSlam(player);
            } else if (msg.keyType == 5) {
                CoinManager.throwCoin(player);
            } else if (msg.keyType == 6) {
                WallJumpManager.performWallJump(player);
            } else if (msg.keyType == 7) {
                ArmManager.switchArm(player);
            } else if (msg.keyType == 8) {
                boolean alreadyHasHook = player.level().getEntitiesOfClass(com.maxwell.highspeedlib.common.entity.WhiplashHookEntity.class,
                        player.getBoundingBox().inflate(64), hook -> hook.getOwner() == player).size() > 0;
                if (!alreadyHasHook) {
                    com.maxwell.highspeedlib.common.entity.WhiplashHookEntity hook = new com.maxwell.highspeedlib.common.entity.WhiplashHookEntity(player.level(), player);
                    player.level().addFreshEntity(hook);
                    player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                            net.minecraft.sounds.SoundEvents.FISHING_BOBBER_THROW, net.minecraft.sounds.SoundSource.PLAYERS, 1.0f, 1.5f);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }

}