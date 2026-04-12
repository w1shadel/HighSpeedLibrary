package com.maxwell.highspeedlib.agent;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AbsoluteLauncher {
    public static void main(String[] args) throws Exception {
        String jarPath = new File("AbsoluteEraser.jar").getAbsolutePath();
        List<String> fullCommand = new ArrayList<>();
        fullCommand.add(System.getProperty("java.home") + "/bin/java");
        fullCommand.add("-Djdk.attach.allowAttachSelf=true");
        fullCommand.add("--add-opens=java.base/jdk.internal.misc=ALL-UNNAMED");
        fullCommand.add("--add-modules=jdk.attach");
        fullCommand.add("-javaagent:" + jarPath + "=" + jarPath);
        fullCommand.addAll(Arrays.asList(args));
        ProcessBuilder pb = new ProcessBuilder(fullCommand);
        pb.inheritIO();
        Process p = pb.start();
        p.waitFor();
    }
}