package com.nesscomputing.jmx.agent;


import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.jezhumble.javasysmon.JavaSysMon;
import com.sun.tools.attach.VirtualMachine;

import org.weakref.jmx.Managed;

@Singleton
public class AgentAttach
{
    private final AgentAttachConfig config;

    @Inject
    AgentAttach(AgentAttachConfig config)
    {
        this.config = config;
    }

    @Managed
    public void attachJVMAgent(String agentPath) throws Exception
    {
        attachJVMAgent(agentPath, null);
    }

    @Managed
    public void attachJVMAgent(String agentPath, String agentOptions) throws Exception
    {
        Preconditions.checkState(config.isAgentAttachAllowed(), "Agent attach is administratively disallowed");
        Attacher.attach(agentPath, agentOptions);
    }

    // Keep this in a different class so that exporting JMX doesn't attempt to load the tooling
    // apis, but waits until someone actually uses it.
    private static class Attacher
    {
        static void attach(String agentPath, String agentOptions) throws Exception
        {
            final VirtualMachine vm = VirtualMachine.attach(Integer.toString(new JavaSysMon().currentPid()));
            if (agentOptions == null) {
                vm.loadAgentPath(agentPath);
            } else {
                vm.loadAgentPath(agentPath, agentOptions);
            }
        }
    }
}
