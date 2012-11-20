package com.nesscomputing.jmx.agent;

import org.skife.config.Config;
import org.skife.config.Default;

interface AgentAttachConfig
{
    @Config("ness.jmx.agent-attach.enabled")
    @Default("true")
    boolean isAgentAttachAllowed();
}
