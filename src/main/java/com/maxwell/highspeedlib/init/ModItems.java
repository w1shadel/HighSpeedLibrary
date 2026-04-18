package com.maxwell.highspeedlib.init;

import com.maxwell.highspeedlib.HighSpeedLib;
import com.maxwell.highspeedlib.common.items.BossBarApplierItem;
import com.maxwell.highspeedlib.common.items.TruthSeeker;
import com.maxwell.highspeedlib.common.items.V1WingsItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, HighSpeedLib.MODID);
    public static final RegistryObject<Item> V1_WINGS = ITEMS.register("v1_wings",
            () -> new V1WingsItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> BOSS_BAR_APPLIER = ITEMS.register("boss_bar_applier",
            () -> new BossBarApplierItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> TRUTHSEEKER = ITEMS.register("truthseeker",
            () -> new TruthSeeker(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));
}
