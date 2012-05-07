package com.nesscomputing.jmx.starter.guice;

import java.io.IOException;
import java.net.InetAddress;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.nesscomputing.galaxy.GalaxyConfig;
import com.nesscomputing.jmx.starter.JmxExporterConfig;

public class JmxExporterConfigProvider implements Provider<JmxExporterConfig>
{
    private final JmxExporterConfig jmxExporterConfig;

    @Inject
    public JmxExporterConfigProvider(final JmxStarterConfig jmxStarterConfig, final GalaxyConfig galaxyConfig)
        throws IOException
    {
        final String host = jmxStarterConfig.isBindInternal() ? galaxyConfig.getInternalIp().getIp()
                                                      : galaxyConfig.getExternalIp().getIp();
        final int port = galaxyConfig.getPrivate().getPortJmx();

        this.jmxExporterConfig = JmxExporterConfig.defaultJmxExporterConfig(InetAddress.getByName(host), port);
    }

    @Override
    public JmxExporterConfig get()
    {
        return jmxExporterConfig;
    }
}
