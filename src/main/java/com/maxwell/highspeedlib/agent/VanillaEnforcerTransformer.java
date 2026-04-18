package com.maxwell.highspeedlib.agent;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

public class VanillaEnforcerTransformer implements ClassFileTransformer {
    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        if (className == null) return null;

        // クラス名の正規化
        String internalName = className.replace('.', '/');
        boolean isLivingEntity = internalName.equals("net/minecraft/world/entity/LivingEntity");
        boolean isEntity = internalName.equals("net/minecraft/world/entity/Entity");
        boolean isData = internalName.equals("net/minecraft/network/syncher/SynchedEntityData");

        if (!isLivingEntity && !isEntity && !isData) return null;

        try {
            ClassReader cr = new ClassReader(classfileBuffer);
            ClassNode cn = new ClassNode();
            cr.accept(cn, 0);
            boolean modified = false;

            for (MethodNode mn : cn.methods) {
                if (isTarget(internalName, mn)) {
                    injectVanillaHead(internalName, mn);
                    modified = true;
                }
            }

            if (modified) {
                // COMPUTE_FRAMES を使い、loader を渡す
                ClassWriter cw = new SafeClassWriter(cr, ClassWriter.COMPUTE_FRAMES, loader);
                cn.accept(cw);
                System.out.println("[VanillaEnforcer] Surgical purification complete: " + internalName);
                return cw.toByteArray();
            }
        } catch (Throwable t) {
            System.err.println("[VanillaEnforcer] Error during surgical transform of " + internalName);
            t.printStackTrace();
        }
        return null;
    }

    private boolean isTarget(String className, MethodNode mn) {
        String n = mn.name;
        if (className.contains("LivingEntity")) {
            // getHealth, setHealth, die (Mojang & SRG 1.20.1)
            return n.equals("getHealth") || n.equals("m_21223_") ||
                    n.equals("setHealth") || n.equals("m_21153_") ||
                    n.equals("die") || n.equals("m_6469_");
        }
        if (className.contains("SynchedEntityData")) {
            // set (Mojang & SRG)
            return n.equals("set") || n.equals("m_135381_");
        }
        if (className.equals("net/minecraft/world/entity/Entity")) {
            // setRemoved (Mojang & SRG 1.20.1)
            return n.equals("setRemoved") || n.equals("m_142687_");
        }
        return false;
    }

    private void injectVanillaHead(String className, MethodNode mn) {
        InsnList head = new InsnList();
        String name = mn.name;

        // getHealth / m_21223_
        if (name.equals("getHealth") || name.equals("m_21223_")) {
            head.add(new VarInsnNode(Opcodes.ALOAD, 0));
            head.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "net/minecraft/world/entity/LivingEntity", "m_20088_", "()Lnet/minecraft/network/syncher/SynchedEntityData;", false));
            head.add(new FieldInsnNode(Opcodes.GETSTATIC, "net/minecraft/world/entity/LivingEntity", "f_20961_", "Lnet/minecraft/network/syncher/EntityDataAccessor;"));
            head.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "net/minecraft/network/syncher/SynchedEntityData", "m_135370_", "(Lnet/minecraft/network/syncher/EntityDataAccessor;)Ljava/lang/Object;", false));
            head.add(new TypeInsnNode(Opcodes.CHECKCAST, "java/lang/Float"));
            head.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Float", "floatValue", "()F", false));
            head.add(new InsnNode(Opcodes.FRETURN));
        }
        // setHealth / m_21153_
        else if (name.equals("setHealth") || name.equals("m_21153_")) {
            head.add(new VarInsnNode(Opcodes.ALOAD, 0));
            head.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "net/minecraft/world/entity/LivingEntity", "m_20088_", "()Lnet/minecraft/network/syncher/SynchedEntityData;", false));
            head.add(new FieldInsnNode(Opcodes.GETSTATIC, "net/minecraft/world/entity/LivingEntity", "f_20961_", "Lnet/minecraft/network/syncher/EntityDataAccessor;"));
            head.add(new VarInsnNode(Opcodes.FLOAD, 1));
            head.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;", false));
            head.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "net/minecraft/network/syncher/SynchedEntityData", "m_135381_", "(Lnet/minecraft/network/syncher/EntityDataAccessor;Ljava/lang/Object;)V", false));
            head.add(new InsnNode(Opcodes.RETURN));
        }
        // SynchedEntityData.set
        else if (className.contains("SynchedEntityData") && (name.equals("set") || name.equals("m_135381_"))) {
            head.add(new VarInsnNode(Opcodes.ALOAD, 0));
            head.add(new VarInsnNode(Opcodes.ALOAD, 1));
            // INVOKESPECIAL を INVOKEVIRTUAL に変更（安全のため。privateでもvirtualで呼べる場合が多い）
            head.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "net/minecraft/network/syncher/SynchedEntityData", "m_135354_", "(Lnet/minecraft/network/syncher/EntityDataAccessor;)Lnet/minecraft/network/syncher/SynchedEntityData$DataItem;", false));
            head.add(new VarInsnNode(Opcodes.ALOAD, 2));
            head.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "net/minecraft/network/syncher/SynchedEntityData$DataItem", "m_135395_", "(Ljava/lang/Object;)V", false));
            head.add(new VarInsnNode(Opcodes.ALOAD, 0));
            head.add(new InsnNode(Opcodes.ICONST_1));
            head.add(new FieldInsnNode(Opcodes.PUTFIELD, "net/minecraft/network/syncher/SynchedEntityData", "f_135349_", "Z"));
            head.add(new InsnNode(Opcodes.RETURN));
        }
        // die / m_6469_
        else if (name.equals("die") || name.equals("m_6469_")) {
            head.add(new VarInsnNode(Opcodes.ALOAD, 0));
            head.add(new InsnNode(Opcodes.ICONST_1));
            head.add(new FieldInsnNode(Opcodes.PUTFIELD, "net/minecraft/world/entity/LivingEntity", "f_20919_", "Z"));
        }

        if (head.size() > 0) {
            mn.instructions.insertBefore(mn.instructions.getFirst(), head);
        }
    }

    private static class SafeClassWriter extends ClassWriter {
        private final ClassLoader loader;

        public SafeClassWriter(ClassReader cr, int flags, ClassLoader loader) {
            super(cr, flags);
            this.loader = (loader != null) ? loader : ClassLoader.getSystemClassLoader();
        }

        @Override
        protected String getCommonSuperClass(String type1, String type2) {
            try {
                Class<?> c = Class.forName(type1.replace('/', '.'), false, loader);
                Class<?> d = Class.forName(type2.replace('/', '.'), false, loader);
                if (c.isAssignableFrom(d)) return type1;
                if (d.isAssignableFrom(c)) return type2;
                if (c.isInterface() || d.isInterface()) return "java/lang/Object";
                do {
                    c = c.getSuperclass();
                } while (!c.isAssignableFrom(d));
                return c.getName().replace('.', '/');
            } catch (Exception e) {
                return "java/lang/Object";
            }
        }
    }
}