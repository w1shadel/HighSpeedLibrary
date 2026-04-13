package com.maxwell.highspeedlib.common.items;

import com.maxwell.highspeedlib.api.main.mob.MobModeManager;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class BossBarApplierItem extends Item {
    public BossBarApplierItem(Properties props) {
        super(props);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (!player.level().isClientSide) {
            if (player.isShiftKeyDown()) {
                MobModeManager.applyEnrage(target, !MobModeManager.isEnraged(target));
                return InteractionResult.SUCCESS;
            }
            if (!MobModeManager.isBoss(target)) {
                MobModeManager.setBoss(target, true);
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.sidedSuccess(player.level().isClientSide);
    }
}
