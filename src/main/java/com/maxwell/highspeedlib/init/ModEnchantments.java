package com.maxwell.highspeedlib.init;

import com.maxwell.highspeedlib.HighSpeedLib;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModEnchantments {

    public static final ResourceKey<Enchantment> STAMINA_BOOST = ResourceKey.create(
            Registries.ENCHANTMENT,
            ResourceLocation.fromNamespaceAndPath(HighSpeedLib.MODID, "stamina_boost")
    );
}