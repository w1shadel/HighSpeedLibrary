package com.maxwell.highspeedlib.common.items;

import com.maxwell.highspeedlib.HighSpeedLib;
import com.maxwell.highspeedlib.init.ModItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.List;

@Mod.EventBusSubscriber(modid = HighSpeedLib.MODID)
public class WingsLogicHandler {
    public static boolean hasWings(Player player) {
        return CuriosApi.getCuriosHelper().findFirstCurio(player, ModItems.V1_WINGS.get()).isPresent();
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) return;
        Player player = event.player;
        if (hasWings(player)) {
            for (EquipmentSlot slot : List.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET)) {
                ItemStack armor = player.getItemBySlot(slot);
                if (!armor.isEmpty()) {
                    player.drop(armor.copy(), false);
                    player.setItemSlot(slot, ItemStack.EMPTY);
                    player.displayClientMessage(Component.literal("§c防具は血を通さない (V1の羽根装備中は防具不可)"), true);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onBloodIsFuel(LivingHurtEvent event) {
        if (event.getSource().getEntity() instanceof Player player && hasWings(player)) {
            LivingEntity victim = event.getEntity();
            if (player.distanceTo(victim) <= 4.0) {
                float healAmount = event.getAmount() * 0.5f;
                player.heal(healAmount);
                if (player.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.DAMAGE_INDICATOR,
                            victim.getX(), victim.getY() + 1.0, victim.getZ(),
                            5, 0.1, 0.1, 0.1, 0.05);
                }
            }
        }
    }
}