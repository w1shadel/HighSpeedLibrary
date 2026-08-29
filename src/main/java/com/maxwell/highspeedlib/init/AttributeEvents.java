package com.maxwell.highspeedlib.init;

import com.maxwell.highspeedlib.HighSpeedLib;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

@SuppressWarnings("removal")
@EventBusSubscriber(modid = HighSpeedLib.MODID, bus = EventBusSubscriber.Bus.MOD)
public class AttributeEvents {
    @SubscribeEvent
    public static void onAttributeModification(EntityAttributeModificationEvent event) {
        event.add(EntityType.PLAYER, ModAttributes.PUNCH_DAMAGE);
        event.add(EntityType.PLAYER, ModAttributes.SLAM_DAMAGE);
        event.add(EntityType.PLAYER, Attributes.JUMP_STRENGTH);
    }
}