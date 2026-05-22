package com.maxwell.highspeedlib.init;

import com.maxwell.highspeedlib.HighSpeedLib;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public class ModDamageTypes {
    public static final ResourceKey<DamageType> RICO_ATTACK = ResourceKey.create
            (Registries.DAMAGE_TYPE, HighSpeedLib.getResourceLocation("rico_shot"));
    public static final ResourceKey<DamageType> FEEDBUCKER_ATTACK = ResourceKey.create
            (Registries.DAMAGE_TYPE, HighSpeedLib.getResourceLocation("feedbucker"));
    public static final ResourceKey<DamageType> BLAST_ATTACK = ResourceKey.create
            (Registries.DAMAGE_TYPE, HighSpeedLib.getResourceLocation("blast"));

    public static DamageSource ricoAttack(Level level, Entity entity) {
        try {
            return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(RICO_ATTACK), entity);
        } catch (Exception e) {
            return entity.damageSources().lightningBolt();
        }
    }

    public static DamageSource feedbuckerAttack(Level level, Entity entity) {
        try {
            return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(FEEDBUCKER_ATTACK), entity);
        } catch (Exception e) {
            return entity.damageSources().lightningBolt();
        }
    }

    public static DamageSource blastAttack(Level level, Entity entity) {
        try {
            return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(BLAST_ATTACK), entity);
        } catch (Exception e) {
            return entity.damageSources().genericKill();
        }
    }
}