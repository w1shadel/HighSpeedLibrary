package com.maxwell.highspeedlib.agent;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

public class AbsoluteAgent {
    public static void agentmain(String agentArgs, Instrumentation inst) {
        Thread watcherThread = new Thread(() -> {
            try {
                System.out.println("[AbsoluteAgent] Ultra-late mode: Waiting 45 seconds for other mods to finish...");
                Thread.sleep(35000);
                ClassLoader forgeLoader = null;
                for (Thread t : Thread.getAllStackTraces().keySet()) {
                    ClassLoader cl = t.getContextClassLoader();
                    if (cl != null) {
                        try {
                            cl.loadClass("com.maxwell.highspeedlib.HighSpeedLib");
                            forgeLoader = cl;
                            break;
                        } catch (Throwable ignored) {
                        }
                    }
                }
                if (forgeLoader == null) {
                    System.err.println("[AbsoluteAgent] Forge ClassLoader not found! Fallback to context classloader.");
                    forgeLoader = Thread.currentThread().getContextClassLoader();
                } else {
                    System.out.println("[AbsoluteAgent] Forge ClassLoader successfully located.");
                }
                Class<?> eraserClass = Class.forName("com.maxwell.highspeedlib.agent.AbsoluteEraser");
                try {
                    java.lang.reflect.Method setLoaderMethod = eraserClass.getMethod("setForgeLoader", ClassLoader.class);
                    setLoaderMethod.invoke(null, forgeLoader);
                    System.out.println("[AbsoluteAgent] forgeLoader injected into AbsoluteEraser.");
                } catch (Throwable e) {
                    System.err.println("[AbsoluteAgent] Failed to inject forgeLoader.");
                    e.printStackTrace();
                }
                Object eraserInstance = eraserClass.getDeclaredConstructor().newInstance();
                java.lang.reflect.Method transformMethod = eraserClass.getMethod("transform", ClassLoader.class, String.class, Class.class, ProtectionDomain.class, byte[].class);
                inst.addTransformer(new ClassFileTransformer() {
                    @Override
                    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain pd, byte[] classfileBuffer) {
                        try {
                            return (byte[]) transformMethod.invoke(eraserInstance, loader, className, classBeingRedefined, pd, classfileBuffer);
                        } catch (Throwable t) {
                            return null;
                        }
                    }
                }, true);
                String[] targets = {
                        "net.minecraft.world.entity.LivingEntity",
                        "net.minecraft.world.entity.Entity",
                        "net.minecraft.world.entity.player.Player",
                        "net.minecraft.server.level.ServerPlayer",
                        "net.minecraft.network.syncher.SynchedEntityData",
                        "net.minecraft.server.players.PlayerList"};
                for (String targetName : targets) {
                    for (Class<?> clazz : inst.getAllLoadedClasses()) {
                        if (clazz.getName().equals(targetName)) {
                            try {
                                System.out.println("[AbsoluteAgent] Performing FINAL OVERWRITE on: " + targetName);
                                inst.retransformClasses(clazz);
                                System.out.println("[AbsoluteAgent] OVERWRITE SUCCESS: " + targetName);
                            } catch (Throwable t) {
                                System.err.println("[AbsoluteAgent] Overwrite failed: " + targetName);
                            }
                        }
                    }
                }
                System.out.println("[AbsoluteAgent] Absolute Defense System is now the LAST WORD.");

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        watcherThread.setDaemon(true);
        watcherThread.start();
    }
}