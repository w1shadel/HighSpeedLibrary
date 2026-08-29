package com.maxwell.highspeedlib.api.main.mob;

import com.maxwell.highspeedlib.common.network.packets.sync.S2CSyncMobModePacket;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation; // ★ UUIDの代わりにインポート
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.neoforge.network.PacketDistributor;

public class MobModeManager {
    public static final String ENRAGE_TAG = "hs_enraged";
    public static final String RADIANCE_TAG = "hs_radiance_tier";
    public static final String BOSS_TAG = "hs_is_boss";

    public static void setEnraged(LivingEntity entity, boolean value) {
        entity.getPersistentData().putBoolean(ENRAGE_TAG, value);
        sync(entity);
    }

    public static void setRadiance(LivingEntity entity, int tier) {
        entity.getPersistentData().putInt(RADIANCE_TAG, tier);
        sync(entity);
    }

    public static boolean isEnraged(LivingEntity entity) {
        return entity.getPersistentData().getBoolean(ENRAGE_TAG);
    }

    public static void setBoss(LivingEntity entity, boolean value) {
        entity.getPersistentData().putBoolean(BOSS_TAG, value);
        sync(entity);
    }

    public static boolean isBoss(LivingEntity entity) {
        return entity.getPersistentData().getBoolean(BOSS_TAG);
    }

    public static int getRadianceTier(LivingEntity entity) {
        return entity.getPersistentData().getInt(RADIANCE_TAG);
    }

    public static void sync(LivingEntity entity) {
        if (entity.level().isClientSide) return;
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, new S2CSyncMobModePacket(entity.getId(), isEnraged(entity), getRadianceTier(entity), isBoss(entity)));
    }

    public static void applyRadiance(LivingEntity entity, int tier, double hpFactor, double dmgFactor, double spdFactor) {
        if (entity.level().isClientSide) return;
        removeRadianceModifiers(entity);
        if (tier <= 0) {
            setRadiance(entity, 0);
            return;
        }
        double healthMult = (tier * 0.5) * hpFactor;
        double damageMult = (tier * 0.5) * dmgFactor;
        double speedMult = (tier * 0.2) * spdFactor;
        // ★ 第4引数の name 文字列は不要
        applyModifier(entity, Attributes.MAX_HEALTH, HighSpeedAttributes.RADIANCE_HEALTH_ID, healthMult);
        applyModifier(entity, Attributes.ATTACK_DAMAGE, HighSpeedAttributes.RADIANCE_DAMAGE_ID, damageMult);
        applyModifier(entity, Attributes.MOVEMENT_SPEED, HighSpeedAttributes.RADIANCE_SPEED_ID, speedMult);
        entity.setHealth(entity.getMaxHealth());
        setRadiance(entity, tier);
        sync(entity);
    }

    // ★ 引数を (..., ResourceLocation id, double value) に変更
    private static void applyModifier(LivingEntity entity, Holder<Attribute> attr, ResourceLocation id, double value) {
        var instance = entity.getAttribute(attr);
        if (instance != null) {
            instance.addPermanentModifier(new AttributeModifier(id, value, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
    }

    public static void removeRadianceModifiers(LivingEntity entity) {
        removeAttribute(entity, Attributes.MAX_HEALTH, HighSpeedAttributes.RADIANCE_HEALTH_ID);
        removeAttribute(entity, Attributes.ATTACK_DAMAGE, HighSpeedAttributes.RADIANCE_DAMAGE_ID);
        removeAttribute(entity, Attributes.MOVEMENT_SPEED, HighSpeedAttributes.RADIANCE_SPEED_ID);
    }

    // ★ 引数を ResourceLocation id に変更
    private static void removeAttribute(LivingEntity entity, Holder<Attribute> attr, ResourceLocation id) {
        var instance = entity.getAttribute(attr);
        if (instance != null && instance.getModifier(id) != null) {
            instance.removeModifier(id);
        }
    }

    public static void applyEnrage(LivingEntity entity, boolean enraged) {
        if (entity.level().isClientSide) return;
        removeAttribute(entity, Attributes.MOVEMENT_SPEED, HighSpeedAttributes.ENRAGE_SPEED_ID);
        if (enraged) {
            applyModifier(entity, Attributes.MOVEMENT_SPEED, HighSpeedAttributes.ENRAGE_SPEED_ID, 0.5);
            entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                    SoundEvents.ZOMBIE_VILLAGER_CONVERTED, SoundSource.HOSTILE, 1.5f, 0.8f);
        }
        setEnraged(entity, enraged);
        sync(entity);
    }
}