package com.maxwell.highspeedlib.common.logic.movement;

import com.maxwell.highspeedlib.HighSpeedLib;
import com.maxwell.highspeedlib.api.config.HighSpeedServerConfig;
import com.maxwell.highspeedlib.common.logic.state.PlayerAbilityState;
import com.maxwell.highspeedlib.common.logic.state.PlayerMovementState;
import com.maxwell.highspeedlib.common.logic.state.PlayerStateManager;
import com.maxwell.highspeedlib.common.network.packets.sync.S2CSyncStaminaPacket;
import com.maxwell.highspeedlib.init.ModEnchantments;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = HighSpeedLib.MODID)
public class StaminaManager {
    public static double getStamina(Player player) {
        PlayerMovementState state = PlayerStateManager.getState(player).getMovement();
        if (state.stamina == 0) {
            state.stamina = getMaxStamina(player);
        }
        return state.stamina;
    }

    public static void setStamina(Player player, double value) {
        double max = getMaxStamina(player);
        PlayerMovementState state = PlayerStateManager.getState(player).getMovement();
        state.stamina = Math.min(max, Math.max(0, value));
        if (player instanceof ServerPlayer serverPlayer) {
            syncToClient(serverPlayer);
        }
    }

    public static double getMaxStamina(Player player) {
        PlayerAbilityState settings = PlayerStateManager.getState(player).getAbility();
        int enchantmentLevel = player.level().registryAccess()
                .lookupOrThrow(Registries.ENCHANTMENT)
                .get(ModEnchantments.STAMINA_BOOST)
                .map(holder -> EnchantmentHelper.getItemEnchantmentLevel(holder, player.getItemBySlot(EquipmentSlot.LEGS)))
                .orElse(0);
        double enchantValue = HighSpeedServerConfig.STAMINA_BOOST_ENCHANT_VALUE.get();
        return settings.maxDashCount + (enchantmentLevel * enchantValue);
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
        PacketDistributor.sendToPlayer(player, new S2CSyncStaminaPacket(getStamina(player), getMaxStamina(player)));
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity().level().isClientSide) return;
        Player player = event.getEntity();
        if (SlideManager.isSliding(player)) {
            return;
        }
        double current = getStamina(player);
        double max = getMaxStamina(player);
        if (current < max) {
            setStamina(player, current + HighSpeedServerConfig.STAMINA_REGEN_PER_TICK.get());
        }
    }
}
