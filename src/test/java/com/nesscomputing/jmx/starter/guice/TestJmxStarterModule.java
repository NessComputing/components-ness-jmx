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

import org.junit.Assert;
import org.junit.Test;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Stage;
import com.nesscomputing.config.Config;
import com.nesscomputing.config.ConfigModule;
import com.nesscomputing.galaxy.GalaxyConfigModule;
import com.nesscomputing.jmx.JmxModule;
import com.nesscomputing.jmx.starter.JmxExporter;
import com.nesscomputing.lifecycle.Lifecycle;
import com.nesscomputing.lifecycle.LifecycleStage;
import com.nesscomputing.lifecycle.guice.LifecycleModule;
import com.nesscomputing.testing.lessio.AllowNetworkAccess;
import com.nesscomputing.testing.lessio.AllowNetworkListen;

@AllowNetworkListen(ports={0})
@AllowNetworkAccess(endpoints={"0.0.0.0:*","127.0.0.1:*"})
public class TestJmxStarterModule
{
    @Inject
    private JmxExporter jmxExporter;

    @Test
    public void testDisabled()
    {
        final Config config = Config.getFixedConfig("ness.jmx.enabled", "false");
        final Injector inj = Guice.createInjector(Stage.PRODUCTION, boilerplate(config), new JmxStarterModule(config));

        Assert.assertNull(inj.getExistingBinding(Key.get(JmxExporter.class)));
    }

    @Test
    public void testDefault()
    {
        final Config config = Config.getFixedConfig("galaxy.private.port.jmx", "0");
        final Injector inj = Guice.createInjector(Stage.PRODUCTION, boilerplate(config), new JmxStarterModule(config), new GalaxyConfigModule());
        inj.injectMembers(this);
        Assert.assertNotNull(jmxExporter);
    }

    @Test
    public void testLifecycle()
        throws Exception
    {
        final Config config = Config.getFixedConfig("galaxy.private.port.jmx", "0");
        final Injector inj = Guice.createInjector(Stage.PRODUCTION, boilerplate(config), new LifecycleModule(), new JmxStarterModule(config), new GalaxyConfigModule());
        inj.injectMembers(this);

        final Lifecycle lifecycle = inj.getInstance(Lifecycle.class);

        Assert.assertNotNull(jmxExporter);

        Assert.assertFalse(jmxExporter.isActive());

        lifecycle.executeTo(LifecycleStage.START_STAGE);

        Assert.assertTrue(jmxExporter.isActive());

        lifecycle.executeTo(LifecycleStage.STOP_STAGE);
        Assert.assertFalse(jmxExporter.isActive());
    }

    @Test
    public void testWildcardOk()
        throws Exception
    {
        final Config config = Config.getFixedConfig("ness.jmx.bind-address", "0.0.0.0");
        final Injector inj = Guice.createInjector(Stage.PRODUCTION, boilerplate(config), new LifecycleModule(), new JmxStarterModule(config));
        inj.injectMembers(this);

        final Lifecycle lifecycle = inj.getInstance(Lifecycle.class);

        Assert.assertNotNull(jmxExporter);

        Assert.assertFalse(jmxExporter.isActive());

        lifecycle.executeTo(LifecycleStage.START_STAGE);

        Assert.assertTrue(jmxExporter.isActive());

        lifecycle.executeTo(LifecycleStage.STOP_STAGE);
        Assert.assertFalse(jmxExporter.isActive());
    }



    private Module boilerplate(final Config config)
    {
        return new Module() {
            @Override
            public void configure(final Binder binder) {
                binder.disableCircularProxies();
                binder.requireExplicitBindings();

                binder.install(new ConfigModule(config));
                binder.install(new JmxModule());
            }
        };
    }
}
