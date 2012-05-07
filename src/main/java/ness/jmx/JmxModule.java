package ness.jmx;

import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;

import org.weakref.jmx.MBeanExporter;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.nesscomputing.lifecycle.Lifecycle;
import com.nesscomputing.lifecycle.LifecycleListener;
import com.nesscomputing.lifecycle.LifecycleStage;
import com.nesscomputing.logging.Log;

/**
 * Sets up JMX bindings.  Specifically, binds an {@link MBeanExporter}
 * that records all bindings made and will automagically unbind them at
 * {@link Lifecycle} STOP_STAGE.
 * @author steven
 */

public class JmxModule extends AbstractModule
{
    private static final Log LOG = Log.findLog();

    @Override
    protected void configure()
    {
        bind(MBeanServer.class).toInstance(ManagementFactory.getPlatformMBeanServer());
    }

    @Provides
    @Singleton
    public MBeanExporter getMBeanExporter(final Lifecycle lifecycle, final MBeanServer mbs)
    {
        final MBeanExporter exporter = new MBeanExporter(mbs);

        lifecycle.addListener(LifecycleStage.STOP_STAGE, new LifecycleListener()
        {
            @Override
            public void onStage(final LifecycleStage lifecycleStage) {
                LOG.info("Unexporting all exported MBeans.");
                exporter.unexportAllAndReportMissing();
            }
        });

        return exporter;
    }
}
