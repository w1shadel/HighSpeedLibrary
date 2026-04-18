package com.maxwell.highspeedlib.agent.onrun;

import com.sun.tools.attach.VirtualMachine;

public class Attacher {
    public static void main(String[] args) {
        if (args.length < 2) return;
        String targetPid = args[0];
        String agentJarPath = args[1];
        try {
            VirtualMachine vm = VirtualMachine.attach(targetPid);
            vm.loadAgent(agentJarPath);
            vm.detach();
            System.out.println("[VanillaEnforcer-Attacher] Successfully attached to " + targetPid);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}