package com.maxwell.highspeedlib.common.logic;

import com.maxwell.highspeedlib.common.entity.ThrownCoinEntity;
import com.maxwell.highspeedlib.common.network.PacketHandler;
import com.maxwell.highspeedlib.common.network.packets.S2CStartTossAnimationPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;

public class CoinManager {
    public static void throwCoin(ServerPlayer player) {
        ThrownCoinEntity coin = new ThrownCoinEntity(player.level(), player);
        Vec3 look = player.getLookAngle();
        coin.shoot(look.x, look.y + 0.2, look.z, 0.5f, 0f);
        Vec3 playerVelocity = player.getDeltaMovement();
        coin.setDeltaMovement(coin.getDeltaMovement().add(playerVelocity.x, 0, playerVelocity.z));
        player.level().addFreshEntity(coin);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.5f, 2.0f);
        PacketHandler.INSTANCE.send(
                PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
                new S2CStartTossAnimationPacket(player.getId())
        );
    }
}