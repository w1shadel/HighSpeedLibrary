package com.maxwell.highspeedlib.agent;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class AbsoluteHelper {
    private static Method isParryingMethod = null;
    private static Method setHealthMethod = null;
    private static Method getMaxHealthMethod = null;
    private static Method unsetRemovedMethod = null;
    private static Field removalReasonField = null;
    private static Field deathTimeField = null;

    public static boolean isParrying(Object entity) {
        try {
            if (isParryingMethod == null) {
                ClassLoader loader = entity.getClass().getClassLoader();
                Class<?> managerCls = Class.forName("com.maxwell.highspeedlib.common.logic.ServerArmManager", true, loader);
                Class<?> livingCls = Class.forName("net.minecraft.world.entity.LivingEntity", false, loader);
                isParryingMethod = managerCls.getMethod("isPlayerParrying", livingCls);
            }
            return (boolean) isParryingMethod.invoke(null, entity);
        } catch (Throwable t) {
            return false;
        }
    }

    public static void forceReconcile(Object entity) {
        try {
            if (setHealthMethod == null) {
                ClassLoader loader = entity.getClass().getClassLoader();
                Class<?> livingCls = Class.forName("net.minecraft.world.entity.LivingEntity", false, loader);
                Class<?> entityCls = Class.forName("net.minecraft.world.entity.Entity", false, loader);
                setHealthMethod = livingCls.getMethod("m_21153_", float.class);
                getMaxHealthMethod = livingCls.getMethod("m_21233_");
                unsetRemovedMethod = entityCls.getMethod("m_146914_");
                removalReasonField = entityCls.getDeclaredField("f_19851_");
                removalReasonField.setAccessible(true);
                deathTimeField = livingCls.getDeclaredField("f_20919_");
                deathTimeField.setAccessible(true);
            }
            float maxH = (float) getMaxHealthMethod.invoke(entity);
            setHealthMethod.invoke(entity, maxH);
            deathTimeField.set(entity, 0);
            if (removalReasonField.get(entity) != null) {
                removalReasonField.set(entity, null);
                unsetRemovedMethod.invoke(entity);
            }

        } catch (Throwable t) {
        }
    }
}