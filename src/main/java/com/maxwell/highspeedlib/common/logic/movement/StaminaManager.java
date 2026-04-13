package com.maxwell.highspeedlib.common.logic.movement;

import com.maxwell.highspeedlib.HighSpeedLib;
import com.maxwell.highspeedlib.api.config.HighSpeedServerConfig;
import com.maxwell.highspeedlib.common.logic.state.PlayerAbilityState;
import com.maxwell.highspeedlib.common.logic.state.PlayerMovementState;
import com.maxwell.highspeedlib.common.logic.state.PlayerStateManager;
import com.maxwell.highspeedlib.common.network.PacketHandler;
import com.maxwell.highspeedlib.common.network.packets.sync.S2CSyncStaminaPacket;
import com.maxwell.highspeedlib.init.ModEnchantments;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(modid = HighSpeedLib.MODID)
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
            setStamina(player, current + HighSpeedServerConfig.STAMINA_REGEN_PER_TICK.get());
        }
    }
}
