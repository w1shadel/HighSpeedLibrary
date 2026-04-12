package com.maxwell.highspeedlib.agent;

import com.sun.tools.attach.VirtualMachine;

public class GhostAttacher {
    public static void main(String[] args) {
        try {
            if (args.length < 2) {
                return;
            }
            String targetPid = args[0];
            String agentJarPath = args[1];
            String agentArgs = args[2];
            VirtualMachine vm = com.sun.tools.attach.VirtualMachine.attach(targetPid);
            vm.loadAgent(agentJarPath, agentArgs);
            vm.detach();
        } catch (Exception e) {
        }
    }
}