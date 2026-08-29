package com.maxwell.highspeedlib.init;

import com.maxwell.highspeedlib.HighSpeedLib;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModAttributes {
    public static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(Registries.ATTRIBUTE, HighSpeedLib.MODID);
    public static final DeferredHolder<Attribute, Attribute> PUNCH_DAMAGE = ATTRIBUTES.register("punch_damage",
            () -> new RangedAttribute("attribute.name.highspeedlib.punch_damage", 4.0, 0.0, 1024.0).setSyncable(true));
    public static final DeferredHolder<Attribute, Attribute> SLAM_DAMAGE = ATTRIBUTES.register("slam_damage",
            () -> new RangedAttribute("attribute.name.highspeedlib.slam_damage", 2.0, 0.0, 1024.0).setSyncable(true));
}
