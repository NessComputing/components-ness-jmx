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
package com.nesscomputing.jmx.starter.guice;

import java.io.IOException;
import java.net.InetAddress;

import org.apache.commons.lang3.ObjectUtils;

import com.google.inject.Inject;
import com.nesscomputing.galaxy.GalaxyConfig;
import com.nesscomputing.jmx.starter.JmxExporterConfig;
import com.nesscomputing.jmx.starter.NetUtils;

public class JmxExporterConfigProvider implements IOExceptionProvider<JmxExporterConfig>
{
    private final InetAddress configuredHost;
    private final Integer configuredPort;

    private InetAddress galaxyHost = null;
    private Integer galaxyPort = null;

    @Inject
    public JmxExporterConfigProvider(final JmxStarterConfig jmxStarterConfig)
        throws IOException
    {
        this.configuredPort = jmxStarterConfig.getBindPort();
        this.configuredHost = jmxStarterConfig.getBindAddress() == null ? null : InetAddress.getByName(jmxStarterConfig.getBindAddress());
    }

    @Inject(optional=true)
    void injectGalaxyConfig(final GalaxyConfig galaxyConfig)
        throws IOException
    {
        this.galaxyPort = galaxyConfig.getPrivate().getPortJmx() == 0 ? null : galaxyConfig.getPrivate().getPortJmx();
        final String host =galaxyConfig.getInternalIp().getIp();
        if (host != null) {
            this.galaxyHost = InetAddress.getByName(host);
        }
    }

    @Override
    public JmxExporterConfig get()
        throws IOException
    {
        Integer port = ObjectUtils.firstNonNull(galaxyPort, configuredPort, NetUtils.findUnusedPort());
        final InetAddress hostAddr = ObjectUtils.firstNonNull(galaxyHost, configuredHost);
        return JmxExporterConfig.defaultJmxExporterConfig(hostAddr, port);
    }
}
