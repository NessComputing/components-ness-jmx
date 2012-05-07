package com.nesscomputing.jmx;

import javax.management.MBeanServer;

import org.weakref.jmx.MBeanExporter;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.nesscomputing.lifecycle.LifecycleStage;
import com.nesscomputing.lifecycle.guice.OnStage;
import com.nesscomputing.logging.Log;


@Singleton
public class LifecycledMBeanExporter extends MBeanExporter
{
    private static final Log LOG = Log.findLog();

    @Inject
    public LifecycledMBeanExporter(final MBeanServer mbeanServer)
    {
        super(mbeanServer);
    }

    @OnStage(LifecycleStage.STOP)
    public void stop()
    {
        LOG.info("Unexporting all exported MBeans.");
        unexportAllAndReportMissing();
    }
}
