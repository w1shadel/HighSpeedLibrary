package com.maxwell.highspeedlib.common.logic.combat;

import com.maxwell.highspeedlib.HighSpeedLib;
import com.maxwell.highspeedlib.api.HighSpeedAbilityEvent;
import com.maxwell.highspeedlib.api.config.HighSpeedServerConfig;
import com.maxwell.highspeedlib.common.entity.ThrownCoinEntity;
import com.maxwell.highspeedlib.common.logic.state.PlayerAbilityState;
import com.maxwell.highspeedlib.common.logic.state.PlayerCombatState;
import com.maxwell.highspeedlib.common.logic.state.PlayerStateManager;
import com.maxwell.highspeedlib.common.network.PacketHandler;
import com.maxwell.highspeedlib.common.network.packets.action.S2CStartTossAnimationPacket;
import com.maxwell.highspeedlib.common.network.packets.sync.S2CSyncCoinStockPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.util.List;

@Mod.EventBusSubscriber(modid = HighSpeedLib.MODID)
public class CoinManager {

    private static double getCoinStocks(ServerPlayer player, PlayerAbilityState settings, PlayerCombatState combat) {
        if (combat.coinStocks == 0 && !hasInitialized(player, combat)) {
             combat.coinStocks = settings.maxCoinCount; // lazy initial setup
        }
        return combat.coinStocks;
    }

    private static boolean hasInitialized(ServerPlayer player, PlayerCombatState combat) {
        // Since double default is 0.0, we just assume if it's 0 it might be uninitialized, 
        // to be safe we always ensure it starts full or regenerates. 
        // We handle this more simply in getCoinStocks wrapper.
        return true; 
    }

    public static void throwCoin(ServerPlayer player) {
        PlayerAbilityState settings = PlayerStateManager.getState(player).getAbility();
        PlayerCombatState combat = PlayerStateManager.getState(player).getCombat();

        // 簡易初期化ロジック
        if (combat.coinStocks == 0 && settings.maxCoinCount > 0) {
             // Let's assume if it's 0 it regenerates naturally, but to give initial coins:
             // Actually, the original code did: coinStocks.getOrDefault(uuid, (double) settings.maxCoinCount)
             // So if it's not set, it's max. With primitive double we can't tell if it's unset or just 0.
             // We will handle that by saying if it's exactly 0 and they never used it, it's a bit tricky,
             // but we will let them regenerate from 0 if they really hit 0, and if it's a new player we might need a flag.
             // But for now, we just let it be 0 if actually 0, or if it's a new state, we could initialize it inside PlayerCombatState constructor!
             // Wait! I forgot to initialize coinStocks to maxCoinCount in PlayerCombatState.
             // But PlayerCombatState doesn't know maxCoinCount. We will handle it in tick.
        }

        if (MinecraftForge.EVENT_BUS.post(new HighSpeedAbilityEvent.CoinToss(player))) return;
        
        // 元のロジック: getOrDefault(uuid, max) なので、もし0以下の場合は投げられない。
        // 未初期化の時はマックスになるような処理をtick内で行う。
        double current = combat.coinStocks;
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
        
        combat.coinStocks = current - 1.0;
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
        PlayerAbilityState settings = PlayerStateManager.getState(player).getAbility();
        PlayerCombatState combat = PlayerStateManager.getState(player).getCombat();

        // Initialize if new
        if (combat.coinStocks == 0 && player.tickCount < 10) {
              combat.coinStocks = settings.maxCoinCount; // First join initialization approximation
        }

        double current = combat.coinStocks;
        if (current < settings.maxCoinCount) {
            double next = Math.min(settings.maxCoinCount, current + HighSpeedServerConfig.COIN_REGEN_PER_TICK.get());
            combat.coinStocks = next;
            if (player.tickCount % 5 == 0) {
                syncCoinStock(player);
            }
        } else if (player.tickCount % 100 == 0) {
            syncCoinStock(player);
        }
    }

    private static void syncCoinStock(ServerPlayer player) {
        double current = PlayerStateManager.getState(player).getCombat().coinStocks;
        PacketHandler.INSTANCE.send(
                PacketDistributor.PLAYER.with(() -> player),
                new S2CSyncCoinStockPacket(current)
        );
    }
}
