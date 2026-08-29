package com.maxwell.highspeedlib.common.items;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.UUID;

public class V1WingsItem extends Item implements ICurioItem {
    private static final UUID GRAVITY_MODIFIER_UUID = UUID.fromString("7f4f6a96-686f-4796-b035-22e16ee9e038");

    public V1WingsItem(Properties props) {
        super(props);
    }

    @Override
    public Multimap<Holder<Attribute>, AttributeModifier> getAttributeModifiers(SlotContext slotContext, ResourceLocation id, ItemStack stack) {
        Multimap<Holder<Attribute>, AttributeModifier> modifiers = HashMultimap.create();
        modifiers.put(
                Attributes.GRAVITY,
                new AttributeModifier(id, -0.3, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
        );
        return modifiers;
    }
}
