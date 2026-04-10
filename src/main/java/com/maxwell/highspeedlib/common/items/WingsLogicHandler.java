package com.maxwell.highspeedlib.common.items;

import com.maxwell.highspeedlib.HighSpeedLib;
import com.maxwell.highspeedlib.common.network.PacketHandler;
import com.maxwell.highspeedlib.common.network.packets.S2CBloodSplatPacket;
import com.maxwell.highspeedlib.init.ModItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.UUID;

@SuppressWarnings("removal")
@Mod.EventBusSubscriber(modid = HighSpeedLib.MODID)
public class WingsLogicHandler {
    private static final UUID ARMOR_NULL_UUID = UUID.fromString("6d58498c-87f4-4166-990d-2e16ee9e038a");
    private static final UUID TOUGHNESS_NULL_UUID = UUID.fromString("9a4f6a96-686f-4796-b035-22e16ee9e039");

    public static boolean hasWings(Player player) {
        return CuriosApi.getCuriosHelper().findFirstCurio(player, ModItems.V1_WINGS.get()).isPresent();
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) return;
        Player player = event.player;
        AttributeInstance armorAttr = player.getAttribute(Attributes.ARMOR);
        AttributeInstance toughnessAttr = player.getAttribute(Attributes.ARMOR_TOUGHNESS);
        if (hasWings(player)) {
            if (armorAttr != null && armorAttr.getModifier(ARMOR_NULL_UUID) == null) {
                armorAttr.addTransientModifier(new AttributeModifier(ARMOR_NULL_UUID, "V1 Wings Armor Penalty", -1.0, AttributeModifier.Operation.MULTIPLY_TOTAL));
            }
            if (toughnessAttr != null && toughnessAttr.getModifier(TOUGHNESS_NULL_UUID) == null) {
                toughnessAttr.addTransientModifier(new AttributeModifier(TOUGHNESS_NULL_UUID, "V1 Wings Toughness Penalty", -1.0, AttributeModifier.Operation.MULTIPLY_TOTAL));
            }
        } else {
            if (armorAttr != null && armorAttr.getModifier(ARMOR_NULL_UUID) != null) {
                armorAttr.removeModifier(ARMOR_NULL_UUID);
            }
            if (toughnessAttr != null && toughnessAttr.getModifier(TOUGHNESS_NULL_UUID) != null) {
                toughnessAttr.removeModifier(TOUGHNESS_NULL_UUID);
            }
        }
    }

    @SubscribeEvent
    public static void onBloodIsFuel(LivingHurtEvent event) {
        if (event.getSource().getEntity() instanceof Player player && hasWings(player)) {
            LivingEntity victim = event.getEntity();
            if (player.level() instanceof ServerLevel serverLevel) {
                Vec3 sprayDir = player.getEyePosition().subtract(victim.position()).normalize();
                serverLevel.sendParticles(new net.minecraft.core.particles.BlockParticleOption(
                                ParticleTypes.BLOCK, net.minecraft.world.level.block.Blocks.NETHER_WART_BLOCK.defaultBlockState()),
                        victim.getX(), victim.getY() + 1.0, victim.getZ(),
                        20, 0.2, 0.2, 0.2, 0.15);
                for (int i = 0; i < 2; i++) {
                    Vec3 splatPos = victim.position().add(
                            (serverLevel.random.nextDouble() - 0.5) * 1.2,
                            0.05,
                            (serverLevel.random.nextDouble() - 0.5) * 1.2
                    );
                    PacketHandler.INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> victim),
                            new S2CBloodSplatPacket(splatPos, sprayDir, 10));
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