package com.maxwell.highspeedlib.init;

import com.maxwell.highspeedlib.HighSpeedLib;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModAttributes {
    public static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(ForgeRegistries.ATTRIBUTES, HighSpeedLib.MODID);
    public static final RegistryObject<Attribute> PUNCH_DAMAGE = ATTRIBUTES.register("punch_damage",
            () -> new RangedAttribute("attribute.name.highspeedlib.punch_damage", 4.0, 0.0, 1024.0).setSyncable(true));
    public static final RegistryObject<Attribute> SLAM_DAMAGE = ATTRIBUTES.register("slam_damage",
            () -> new RangedAttribute("attribute.name.highspeedlib.slam_damage", 2.0, 0.0, 1024.0).setSyncable(true));
}
