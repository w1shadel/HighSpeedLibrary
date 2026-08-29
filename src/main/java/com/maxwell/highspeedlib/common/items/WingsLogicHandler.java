package com.maxwell.highspeedlib.common.items;

import com.maxwell.highspeedlib.HighSpeedLib;
import com.maxwell.highspeedlib.common.network.packets.effect.S2CBloodSplatPacket;
import com.maxwell.highspeedlib.init.ModItems;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import top.theillusivec4.curios.api.CuriosApi;
@SuppressWarnings("removal")
@EventBusSubscriber(modid = HighSpeedLib.MODID)
public class WingsLogicHandler {

    private static final ResourceLocation ARMOR_PENALTY_ID = ResourceLocation.fromNamespaceAndPath(HighSpeedLib.MODID, "v1_wings_armor_penalty");
    private static final ResourceLocation TOUGHNESS_PENALTY_ID = ResourceLocation.fromNamespaceAndPath(HighSpeedLib.MODID, "v1_wings_toughness_penalty");

    public static boolean hasWings(Player player) {
        return CuriosApi.getCuriosHelper().findFirstCurio(player, ModItems.V1_WINGS.get()).isPresent();
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity().level().isClientSide) return;
        Player player = event.getEntity();
        AttributeInstance armorAttr = player.getAttribute(Attributes.ARMOR);
        AttributeInstance toughnessAttr = player.getAttribute(Attributes.ARMOR_TOUGHNESS);
        if (hasWings(player)) {
            if (armorAttr != null && armorAttr.getModifier(ARMOR_PENALTY_ID) == null) {

                armorAttr.addTransientModifier(new AttributeModifier(ARMOR_PENALTY_ID, -1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            }
            if (toughnessAttr != null && toughnessAttr.getModifier(TOUGHNESS_PENALTY_ID) == null) {
                toughnessAttr.addTransientModifier(new AttributeModifier(TOUGHNESS_PENALTY_ID, -1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            }
        } else {
            if (armorAttr != null && armorAttr.getModifier(ARMOR_PENALTY_ID) != null) {
                armorAttr.removeModifier(ARMOR_PENALTY_ID);
            }
            if (toughnessAttr != null && toughnessAttr.getModifier(TOUGHNESS_PENALTY_ID) != null) {
                toughnessAttr.removeModifier(TOUGHNESS_PENALTY_ID);
            }
        }
    }

    @SubscribeEvent
    public static void onBloodIsFuel(LivingIncomingDamageEvent event) {
        if (event.getSource().getEntity() instanceof Player player && hasWings(player)) {
            LivingEntity victim = event.getEntity();
            if (player.level() instanceof ServerLevel serverLevel) {
                Vec3 sprayDir = player.getEyePosition().subtract(victim.position()).normalize();
                serverLevel.sendParticles(new BlockParticleOption(
                                ParticleTypes.BLOCK, Blocks.NETHER_WART_BLOCK.defaultBlockState()),
                        victim.getX(), victim.getY() + 1.0, victim.getZ(),
                        20, 0.2, 0.2, 0.2, 0.15);
                for (int i = 0; i < 2; i++) {
                    Vec3 splatPos = victim.position().add(
                            (serverLevel.random.nextDouble() - 0.5) * 1.2,
                            0.05,
                            (serverLevel.random.nextDouble() - 0.5) * 1.2
                    );
                    PacketDistributor.sendToPlayersTrackingEntityAndSelf(victim, new S2CBloodSplatPacket(splatPos, sprayDir, 10));
                }
            }
            if (player.distanceTo(victim) <= 4.0) {
                int armorCount = 0;
                for (ItemStack armor : player.getArmorSlots()) {
                    if (!armor.isEmpty()) armorCount++;
                }
                float efficiency = switch (armorCount) {
                    case 0 -> 1.0f;
                    case 1 -> 0.75f;
                    case 2 -> 0.5f;
                    case 3 -> 0.25f;
                    case 4 -> 0.05f;
                    default -> 1.0f;
                };
                float baseHeal = event.getAmount() * 0.5f;
                player.heal(baseHeal * efficiency);
            }
        }
    }
}