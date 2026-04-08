package com.maxwell.highspeedlib.common.logic;

import com.maxwell.highspeedlib.HighSpeedLib;
import com.maxwell.highspeedlib.common.network.PacketHandler;
import com.maxwell.highspeedlib.common.network.packets.S2CSyncStaminaPacket;
import com.maxwell.highspeedlib.init.ModEnchantments;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = HighSpeedLib.MODID)
public class StaminaManager {
    private static final Map<UUID, Double> playerStamina = new HashMap<>();

    public static double getStamina(Player player) {
        return playerStamina.getOrDefault(player.getUUID(), 3.0);
    }

    public static void setStamina(Player player, double value) {
        double max = getMaxStamina(player);
        playerStamina.put(player.getUUID(), Math.min(max, Math.max(0, value)));
        if (player instanceof ServerPlayer serverPlayer) {
            syncToClient(serverPlayer);
        }
    }

    public static double getMaxStamina(Player player) {
        AbilityAuthority.PlayerSettings settings = AbilityAuthority.get(player.getUUID());
        int enchantmentLevel = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.STAMINA_BOOST.get(), player.getItemBySlot(EquipmentSlot.LEGS));
        return settings.maxDashCount + enchantmentLevel;
    }

    public static boolean consumeStamina(Player player, double amount) {
        double current = getStamina(player);
        if (current >= amount) {
            setStamina(player, current - amount);
            return true;
        }
        return false;
    }

    public static void syncToClient(ServerPlayer player) {
        PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player),
                new S2CSyncStaminaPacket(getStamina(player), getMaxStamina(player)));
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) return;
        Player player = event.player;
        if (SlideManager.isSliding(player)) {
            return;
        }
        double current = getStamina(player);
        double max = getMaxStamina(player);
        if (current < max) {
            setStamina(player, current + 0.04);
        }
    }
}
