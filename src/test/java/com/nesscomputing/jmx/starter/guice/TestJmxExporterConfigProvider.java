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

import java.net.InetAddress;

import javax.management.MBeanServer;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Scopes;
import com.google.inject.Stage;

import com.nesscomputing.config.Config;
import com.nesscomputing.config.ConfigModule;
import com.nesscomputing.galaxy.GalaxyConfigModule;
import com.nesscomputing.jmx.starter.JmxExporterConfig;

import org.junit.Assert;
import org.junit.Test;
import org.kitei.testing.lessio.AllowNetworkListen;
import org.weakref.jmx.testing.TestingMBeanServer;

@AllowNetworkListen(ports={0})
public class TestJmxExporterConfigProvider
{
    @Inject
    private IOExceptionProvider<JmxExporterConfig> configProvider = null;

    @Test
    public void testNoConfig() throws Exception
    {
        final Config config = Config.getEmptyConfig();
        prepareGuice(config);

        final JmxExporterConfig jmxConfig = configProvider.get();
        Assert.assertEquals(InetAddress.getByName("127.0.0.1"), jmxConfig.getHostname());
        Assert.assertTrue(jmxConfig.getRmiRegistryPort() > 0);
        Assert.assertTrue(jmxConfig.getRmiServerPort() > 0);
    }

    @Test
    public void testExplictConfig() throws Exception
    {
        final Config config = Config.getFixedConfig("ness.jmx.bind-address", "1.2.3.4",
                                                    "ness.jmx.bind-port", "65432");
        prepareGuice(config);

        final JmxExporterConfig jmxConfig = configProvider.get();
        Assert.assertEquals(InetAddress.getByName("1.2.3.4"), jmxConfig.getHostname());
        Assert.assertEquals(65432, jmxConfig.getRmiRegistryPort().intValue());
        Assert.assertTrue(jmxConfig.getRmiServerPort() > 0);
    }

    @Test
    public void testGalaxyConfig() throws Exception
    {
        final Config config = Config.getFixedConfig("galaxy.internal.ip", "4.8.15.16",
                                                    "galaxy.private.port.jmx", "2342");
        prepareGuice(config, new GalaxyConfigModule());

        final JmxExporterConfig jmxConfig = configProvider.get();
        Assert.assertEquals(InetAddress.getByName("4.8.15.16"), jmxConfig.getHostname());
        Assert.assertEquals(2342, jmxConfig.getRmiRegistryPort().intValue());
        Assert.assertTrue(jmxConfig.getRmiServerPort() > 0);
    }

    @Test
    public void testGalaxyConfigWins() throws Exception
    {
        final Config config = Config.getFixedConfig("galaxy.internal.ip", "4.8.15.16",
                                                    "galaxy.private.port.jmx", "2342",
                                                    "ness.jmx.bind-address", "1.2.3.4",
                                                    "ness.jmx.bind-port", "65432");
        prepareGuice(config, new GalaxyConfigModule());

        final JmxExporterConfig jmxConfig = configProvider.get();
        Assert.assertEquals(InetAddress.getByName("4.8.15.16"), jmxConfig.getHostname());
        Assert.assertEquals(2342, jmxConfig.getRmiRegistryPort().intValue());
        Assert.assertTrue(jmxConfig.getRmiServerPort() > 0);
    }


    private void prepareGuice(final Config config, final Module ... modules)
    {
        final Injector inj = Guice.createInjector(Stage.PRODUCTION,
                                                  new ConfigModule(config),
                                                  new JmxStarterModule(config),
                                                  new Module() {
                                                      @Override
                                                      public void configure(final Binder binder) {
                                                          binder.disableCircularProxies();
                                                          binder.requireExplicitBindings();
                                                          binder.bind(MBeanServer.class).to(TestingMBeanServer.class).in(Scopes.SINGLETON);
                                                          for (final Module module : modules) {
                                                              binder.install(module);
                                                          }
                                                      }
                                                  });

        inj.injectMembers(this);
    }
}

