package com.maxwell.highspeedlib.init;

import com.maxwell.highspeedlib.HighSpeedLib;
import com.maxwell.highspeedlib.common.entity.ThrownCoinEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, HighSpeedLib.MODID);
    public static final RegistryObject<EntityType<ThrownCoinEntity>> TCOIN = ENTITIES.register("thr_coin",
            () -> EntityType.Builder.<ThrownCoinEntity>of(ThrownCoinEntity::new, MobCategory.MISC)
                    .sized(0.8F, 0.8F)
                    .clientTrackingRange(10)
                    .updateInterval(20)
                    .build("thr_coin"));
}