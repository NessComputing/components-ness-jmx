package ness.jmx.starter;

import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.registry.LocateRegistry;
import java.util.Map;

import javax.management.MBeanServer;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

import ness.jmx.starter.guice.JmxStarterModule;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.nesscomputing.lifecycle.LifecycleStage;
import com.nesscomputing.lifecycle.guice.OnStage;

/**
 * Exports an MBean Server through RMI and makes sure that the RMI server only binds to the Hostname and Port given.
 */
public class JmxExporter
{
    private final JMXServiceURL url;
    private final JMXConnectorServer connectorServer;
    private final JmxExporterConfig config;

    @Inject
    public JmxExporter(final MBeanServer server,
                       final JmxExporterConfig config,
                       @Named(JmxStarterModule.JMX_STARTER_NAME) final Map<String, String> env)
        throws IOException
    {
        this.config = config;

        try {
            final String hostName = config.getHostname().getHostAddress();
            url = new JMXServiceURL(String.format("service:jmx:rmi://%s:%d/jndi/rmi://%s:%d/jmxrmi",
                                                  hostName,
                                                  config.getRmiServerPort(),
                                                  hostName,
                                                  config.getRmiRegistryPort()));
        }
        catch (MalformedURLException e) {
            throw new IllegalStateException(e);
        }

        connectorServer = JMXConnectorServerFactory.newJMXConnectorServer(url, env, server);
    }

    public JMXServiceURL getURL()
    {
        return url;
    }

    @OnStage(LifecycleStage.START)
    public void start()
        throws IOException
    {
        System.setProperty("java.rmi.server.randomIDs", String.valueOf(config.useRandomIds()));
        System.setProperty("java.rmi.server.hostname", config.getHostname().getHostAddress());

        final JmxSocketFactory factory = new JmxSocketFactory(config.getHostname());
        LocateRegistry.createRegistry(config.getRmiRegistryPort(), factory, factory);

        connectorServer.start();
    }


    @OnStage(LifecycleStage.STOP)
    public void stop() throws IOException
    {
        connectorServer.stop();
    }
}
