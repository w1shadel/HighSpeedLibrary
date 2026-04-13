package com.maxwell.highspeedlib.agent;

import org.objectweb.asm.*;
import org.objectweb.asm.commons.AdviceAdapter;
import org.objectweb.asm.commons.Method;

import java.lang.instrument.ClassFileTransformer;
import java.lang.reflect.Field;
import java.security.ProtectionDomain;

public class AbsoluteEraser implements ClassFileTransformer {
    private static final ThreadLocal<Boolean> IN_AGENT = ThreadLocal.withInitial(() -> false);
    private static ClassLoader forgeLoader = null;
    private static java.lang.reflect.Method parryMethodCache = null;
    private static Field entityDataFieldCache = null;

    public static void setForgeLoader(ClassLoader cl) { forgeLoader = cl; }

    public static boolean checkParry(Object entity) {
        if (entity == null || IN_AGENT.get()) return false;
        IN_AGENT.set(true);
        try {
            ClassLoader loader = forgeLoader != null ? forgeLoader : entity.getClass().getClassLoader();
            if (parryMethodCache == null) {
                Class<?> livingClass = Class.forName("net.minecraft.world.entity.LivingEntity", false, loader);
                Class<?> manager = Class.forName("com.maxwell.highspeedlib.common.logic.combat.ServerArmManager", true, loader);
                parryMethodCache = manager.getMethod("isPlayerParrying", livingClass);
                parryMethodCache.setAccessible(true);
            }
            return (boolean) parryMethodCache.invoke(null, entity);
        } catch (Throwable t) { return false; }
        finally { IN_AGENT.set(false); }
    }

    public static boolean isOwnerParrying(Object dataInstance) {
        try {
            if (entityDataFieldCache == null) {
                Class<?> cls = dataInstance.getClass();
                while (cls != null) {
                    for (Field f : cls.getDeclaredFields()) {
                        if (f.getType().getName().endsWith(".Entity")) {
                            f.setAccessible(true);
                            entityDataFieldCache = f;
                            break;
                        }
                    }
                    cls = cls.getSuperclass();
                }
            }
            if (entityDataFieldCache != null) return checkParry(entityDataFieldCache.get(dataInstance));
        } catch (Exception e) {}
        return false;
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain pd, byte[] classfileBuffer) {
        if (className == null) return null;
        String normalized = className.replace('.', '/');

        boolean isEntity = normalized.equals("net/minecraft/world/entity/LivingEntity")
                || normalized.equals("net/minecraft/world/entity/player/Player")
                || normalized.equals("net/minecraft/server/level/ServerPlayer");
        boolean isData = normalized.equals("net/minecraft/network/syncher/SynchedEntityData");
        boolean isPlayerList = normalized.equals("net/minecraft/server/players/PlayerList");

        if (isEntity || isData || isPlayerList) {
            try {
                ClassReader cr = new ClassReader(classfileBuffer);
                ClassWriter cw = new SafeClassWriter(loader, cr, ClassWriter.COMPUTE_MAXS);
                cr.accept(new ClassVisitor(Opcodes.ASM9, cw) {
                    @Override
                    public MethodVisitor visitMethod(int access, String name, String desc, String sig, String[] exceptions) {
                        MethodVisitor mv = super.visitMethod(access, name, desc, sig, exceptions);
                        return new AdviceAdapter(Opcodes.ASM9, mv, access, name, desc) {

                            private void loadIsParrying() {
                                if (isData) {
                                    loadThis();
                                    invokeStatic(Type.getType(AbsoluteEraser.class), Method.getMethod("boolean isOwnerParrying(Object)"));
                                } else if (isPlayerList) {
                                    loadArg(0);
                                    invokeStatic(Type.getType(AbsoluteEraser.class), Method.getMethod("boolean checkParry(Object)"));
                                } else {
                                    loadThis();
                                    invokeStatic(Type.getType(AbsoluteEraser.class), Method.getMethod("boolean checkParry(Object)"));
                                }
                            }

                            @Override
                            protected void onMethodEnter() {
                                if (name.equals("respawn") || name.equals("m_11299_") ||
                                        name.equals("discard") || name.equals("m_146870_") ||
                                        name.equals("remove") || name.equals("m_142687_") ||
                                        name.equals("setRemoved") || name.equals("m_20260_") ||
                                        name.equals("kill") || name.equals("m_21232_") ||
                                        name.equals("die") || name.equals("m_21014_")) {

                                    Label labelContinue = new Label();
                                    loadIsParrying();
                                    ifZCmp(EQ, labelContinue);

                                    getStatic(Type.getType(System.class), "out", Type.getType(java.io.PrintStream.class));
                                    push("[Absolute-Defense] BLOCKED ACTION: " + normalized + "#" + name);
                                    invokeVirtual(Type.getType(java.io.PrintStream.class), Method.getMethod("void println(String)"));

                                    if (name.contains("respawn")) loadArg(0);
                                    else if (Type.getReturnType(desc).getSort() == Type.BOOLEAN) push(false);
                                    returnValue();
                                    mark(labelContinue);
                                }
                            }

                            @Override
                            protected void onMethodExit(int opcode) {
                                if (name.equals("getHealth") || name.equals("m_21223_") ||
                                        name.equals("getMaxHealth") || name.equals("m_21233_") ||
                                        name.equals("isAlive") || name.equals("m_6084_") ||
                                        name.equals("isDeadOrDying") || name.equals("m_21224_") ||
                                        name.equals("isRemoved") || name.equals("m_213877_")) {

                                    Label labelEnd = new Label();
                                    loadThis(); invokeStatic(Type.getType(AbsoluteEraser.class), Method.getMethod("boolean checkParry(Object)"));
                                    ifZCmp(EQ, labelEnd);

                                    if (name.contains("Health")) { pop(); push(20.0f); }
                                    else if (name.contains("Alive") || name.equals("m_6084_")) { pop(); push(true); }
                                    else { pop(); push(false); }
                                    mark(labelEnd);
                                }
                            }
                        };
                    }
                }, ClassReader.EXPAND_FRAMES);
                return cw.toByteArray();
            } catch (Throwable t) { return null; }
        }
        return null;
    }

    private static class SafeClassWriter extends ClassWriter {
        private final ClassLoader loader;
        public SafeClassWriter(ClassLoader loader, ClassReader cr, int flags) {
            super(cr, flags);
            this.loader = (loader != null) ? loader : ClassLoader.getSystemClassLoader();
        }
        @Override protected String getCommonSuperClass(String t1, String t2) { return "java/lang/Object"; }
    }
}