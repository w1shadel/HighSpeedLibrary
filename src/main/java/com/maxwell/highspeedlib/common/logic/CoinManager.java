package com.maxwell.highspeedlib.common.logic;

import com.maxwell.highspeedlib.HighSpeedLib;
import com.maxwell.highspeedlib.api.HighSpeedAbilityEvent;
import com.maxwell.highspeedlib.common.entity.ThrownCoinEntity;
import com.maxwell.highspeedlib.common.network.PacketHandler;
import com.maxwell.highspeedlib.common.network.packets.S2CStartTossAnimationPacket;
import com.maxwell.highspeedlib.common.network.packets.S2CSyncCoinStockPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = HighSpeedLib.MODID)
public class CoinManager {
    private static final Map<UUID, Double> coinStocks = new HashMap<>();

    public static void throwCoin(ServerPlayer player) {
        AbilityAuthority.PlayerSettings settings = AbilityAuthority.get(player.getUUID());
        if (MinecraftForge.EVENT_BUS.post(new HighSpeedAbilityEvent.CoinToss(player))) return;
        double current = coinStocks.getOrDefault(player.getUUID(), (double) settings.maxCoinCount);
        if (current < 1.0) {
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.DISPENSER_FAIL, SoundSource.PLAYERS, 0.5f, 2.0f);
            return;
        }
        List<ThrownCoinEntity> myCoins = player.level().getEntitiesOfClass(
                ThrownCoinEntity.class,
                player.getBoundingBox().inflate(128.0),
                coin -> coin.getOwner() == player && !coin.isRemoved()
        );
        if (myCoins.size() >= settings.maxCoinCount) {
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.DISPENSER_FAIL, SoundSource.PLAYERS, 0.5f, 2.0f);
            return;
        }
        coinStocks.put(player.getUUID(), current - 1.0);
        syncCoinStock(player);
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

    @SubscribeEvent
    public static void onServerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) return;
        ServerPlayer player = (ServerPlayer) event.player;
        AbilityAuthority.PlayerSettings settings = AbilityAuthority.get(player.getUUID());
        double current = coinStocks.getOrDefault(player.getUUID(), (double) settings.maxCoinCount);
        if (current < settings.maxCoinCount) {
            double next = Math.min(settings.maxCoinCount, current + 0.016);
            coinStocks.put(player.getUUID(), next);
            if (player.tickCount % 5 == 0) {
                syncCoinStock(player);
            }
        } else if (player.tickCount % 100 == 0) {
            syncCoinStock(player);
        }
    }

    private static void syncCoinStock(ServerPlayer player) {
        double current = coinStocks.getOrDefault(player.getUUID(), 0.0);
        PacketHandler.INSTANCE.send(
                PacketDistributor.PLAYER.with(() -> player),
                new S2CSyncCoinStockPacket(current)
        );
    }
}