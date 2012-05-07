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
        final Config config = Config.getEmptyConfig();
        final Injector inj = Guice.createInjector(Stage.PRODUCTION, boilerplate(config), new JmxStarterModule(config), new GalaxyConfigModule());
        inj.injectMembers(this);
        Assert.assertNotNull(jmxExporter);
    }

    @Test
    public void testLifecycle()
        throws Exception
    {
        final Config config = Config.getEmptyConfig();
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
