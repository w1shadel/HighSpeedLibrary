package com.maxwell.highspeedlib.agent;

import java.lang.instrument.Instrumentation;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class AbsoluteAgent {
    private static final Set<String> TARGET_CLASSES = Set.of(
            "net.minecraft.world.entity.LivingEntity",
            "net.minecraft.world.entity.Entity"
    );

    public static void agentmain(String args, Instrumentation inst) {
        System.out.println("[VanillaEnforcer] Initializing Absolute Purification...");
        inst.addTransformer(new VanillaEnforcerTransformer(), true);
        List<Class<?>> targets = new ArrayList<>();
        for (Class<?> clazz : inst.getAllLoadedClasses()) {
            if (TARGET_CLASSES.contains(clazz.getName())) {
                if (inst.isModifiableClass(clazz)) targets.add(clazz);
            }
        }
        try {
            if (!targets.isEmpty()) inst.retransformClasses(targets.toArray(new Class<?>[0]));
            System.out.println("[VanillaEnforcer] Purification complete.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}