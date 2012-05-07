/**
 * Copyright (C) 2012 Ness Computing, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nesscomputing.jmx.starter;

import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.registry.LocateRegistry;
import java.util.Map;

import javax.management.MBeanServer;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.nesscomputing.jmx.starter.guice.JmxStarterModule;
import com.nesscomputing.lifecycle.LifecycleStage;
import com.nesscomputing.lifecycle.guice.OnStage;
import com.nesscomputing.logging.Log;

/**
 * Exports an MBean Server through RMI and makes sure that the RMI server only binds to the Hostname and Port given.
 */
public class JmxExporter
{
    private static final Log LOG = Log.findLog();

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

    public boolean isActive()
    {
        return connectorServer.isActive();
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
        LOG.info("Started exporter on port %d", config.getRmiRegistryPort());
    }


    @OnStage(LifecycleStage.STOP)
    public void stop() throws IOException
    {
        connectorServer.stop();
        LOG.info("Stopped exporter on port %d", config.getRmiRegistryPort());
    }
}
