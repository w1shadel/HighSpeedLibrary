package com.maxwell.highspeedlib.api.main.mob;

import com.maxwell.highspeedlib.common.network.PacketHandler;
import com.maxwell.highspeedlib.common.network.packets.S2CSyncMobModePacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.network.PacketDistributor;

import java.util.UUID;

public class MobModeManager {
    public static final String ENRAGE_TAG = "hs_enraged";
    public static final String RADIANCE_TAG = "hs_radiance_tier";

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

    public static int getRadianceTier(LivingEntity entity) {
        return entity.getPersistentData().getInt(RADIANCE_TAG);
    }

    public static void sync(LivingEntity entity) {
        if (entity.level().isClientSide) return;
        PacketHandler.INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity),
                new S2CSyncMobModePacket(entity.getId(), isEnraged(entity), getRadianceTier(entity)));
    }

    /**
     * @param tier      ティア (1-5)
     * @param hpFactor  体力の伸び率 (1.0 = 標準)
     * @param dmgFactor 攻撃力の伸び率
     * @param spdFactor 移動速度の伸び率
     */
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
        applyModifier(entity, Attributes.MAX_HEALTH, HighSpeedAttributes.RADIANCE_HEALTH_ID, "Radiance Health", healthMult);
        applyModifier(entity, Attributes.ATTACK_DAMAGE, HighSpeedAttributes.RADIANCE_DAMAGE_ID, "Radiance Damage", damageMult);
        applyModifier(entity, Attributes.MOVEMENT_SPEED, HighSpeedAttributes.RADIANCE_SPEED_ID, "Radiance Speed", speedMult);
        entity.setHealth(entity.getMaxHealth());
        setRadiance(entity, tier);
        sync(entity);
    }

    private static void applyModifier(LivingEntity entity, Attribute attr, UUID id, String name, double value) {
        var instance = entity.getAttribute(attr);
        if (instance != null) {
            instance.addPermanentModifier(new AttributeModifier(id, name, value, AttributeModifier.Operation.MULTIPLY_TOTAL));
        }
    }

    public static void removeRadianceModifiers(LivingEntity entity) {
        removeAttribute(entity, Attributes.MAX_HEALTH, HighSpeedAttributes.RADIANCE_HEALTH_ID);
        removeAttribute(entity, Attributes.ATTACK_DAMAGE, HighSpeedAttributes.RADIANCE_DAMAGE_ID);
        removeAttribute(entity, Attributes.MOVEMENT_SPEED, HighSpeedAttributes.RADIANCE_SPEED_ID);
    }

    private static void removeAttribute(LivingEntity entity, Attribute attr, UUID id) {
        var instance = entity.getAttribute(attr);
        if (instance != null && instance.getModifier(id) != null) {
            instance.removeModifier(id);
        }
    }

    public static void applyEnrage(LivingEntity entity, boolean enraged) {
        if (entity.level().isClientSide) return;
        removeAttribute(entity, Attributes.MOVEMENT_SPEED, HighSpeedAttributes.ENRAGE_SPEED_ID);
        if (enraged) {
            applyModifier(entity, Attributes.MOVEMENT_SPEED, HighSpeedAttributes.ENRAGE_SPEED_ID, "Enrage Speed", 0.5);
            entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                    SoundEvents.ZOMBIE_VILLAGER_CONVERTED, SoundSource.HOSTILE, 1.5f, 0.8f);
        }
        setEnraged(entity, enraged);
        sync(entity);
    }
}