package com.maxwell.highspeedlib.agent.onrun;

import net.minecraftforge.fml.ModList;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Bootstrap {
    public static void initialize() {
        try {
            String pid = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
            Path path = ModList.get().getModFileById("highspeedlib").getFile().getFilePath();
            if (path == null) {
                throw new RuntimeException("[VanillaEnforcer] Could not find physical path for highspeedlib");
            }
            String jarPath = path.toAbsolutePath().toString();
            String javaHome = System.getProperty("java.home");
            String javaBin = javaHome + File.separator + "bin" + File.separator + "java";
            List<String> command = new ArrayList<>();
            command.add(javaBin);
            command.add("-cp");
            command.add(jarPath);
            command.add("com.maxwell.highspeedlib.agent.onrun.Attacher");
            command.add(pid);
            command.add(jarPath);
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.inheritIO();
            pb.start();
            System.out.println("[VanillaEnforcer] Spawning attacher... Physical Path: " + jarPath);

        } catch (Exception e) {
            System.err.println("[VanillaEnforcer] Failed to spawn attacher process.");
            e.printStackTrace();
        }
    }
}