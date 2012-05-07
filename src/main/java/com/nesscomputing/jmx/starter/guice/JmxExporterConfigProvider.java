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

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.nesscomputing.galaxy.GalaxyConfig;
import com.nesscomputing.jmx.starter.JmxExporterConfig;
import com.nesscomputing.jmx.starter.NetUtils;

public class JmxExporterConfigProvider implements Provider<JmxExporterConfig>
{
    private final JmxExporterConfig jmxExporterConfig;

    @Inject
    public JmxExporterConfigProvider(final JmxStarterConfig jmxStarterConfig, final GalaxyConfig galaxyConfig)
        throws IOException
    {
        final String host = jmxStarterConfig.isBindInternal() ? galaxyConfig.getInternalIp().getIp()
                                                      : galaxyConfig.getExternalIp().getIp();
        final int port = galaxyConfig.getPrivate().getPortJmx() == 0 ? NetUtils.findUnusedPort()
                                                                     : galaxyConfig.getPrivate().getPortJmx();

        this.jmxExporterConfig = JmxExporterConfig.defaultJmxExporterConfig(InetAddress.getByName(host), port);
    }

    @Override
    public JmxExporterConfig get()
    {
        return jmxExporterConfig;
    }
}
