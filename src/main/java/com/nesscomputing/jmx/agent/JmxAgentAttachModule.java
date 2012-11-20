package com.nesscomputing.jmx.agent;

import com.google.inject.AbstractModule;

import org.weakref.jmx.guice.MBeanModule;

import com.nesscomputing.config.ConfigProvider;

public final class JmxAgentAttachModule extends AbstractModule
{
    @Override
    protected void configure()
    {
        bind (AgentAttachConfig.class).toProvider(ConfigProvider.of(AgentAttachConfig.class));
        bind (AgentAttach.class).asEagerSingleton();
        MBeanModule.newExporter(binder()).export(AgentAttach.class).as("ness.management:name=AttachJVMAgent");
    }

    @Override
    public boolean equals(Object obj)
    {
        return obj != null && obj.getClass().equals(getClass());
    }

    @Override
    public int hashCode()
    {
        return getClass().hashCode();
    }
}
