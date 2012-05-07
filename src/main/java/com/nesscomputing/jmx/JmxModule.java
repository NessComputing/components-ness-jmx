package com.nesscomputing.jmx;

import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;

import org.weakref.jmx.MBeanExporter;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import com.nesscomputing.lifecycle.Lifecycle;

/**
 * Sets up JMX bindings.  Specifically, binds an {@link MBeanExporter}
 * that records all bindings made and will automagically unbind them at
 * {@link Lifecycle} STOP_STAGE.
 * @author steven
 */

public class JmxModule extends AbstractModule
{
    @Override
    protected void configure()
    {
        bind(MBeanExporter.class).to(LifecycledMBeanExporter.class).in(Scopes.SINGLETON);
    }

    @Provides
    @Singleton
    public MBeanServer getPlatformMBeanServer()
    {
        return ManagementFactory.getPlatformMBeanServer();
    }
}
