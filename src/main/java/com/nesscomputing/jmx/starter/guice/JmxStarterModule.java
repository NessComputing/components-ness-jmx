package com.nesscomputing.jmx.starter.guice;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import com.nesscomputing.config.Config;
import com.nesscomputing.jmx.starter.JmxExporter;
import com.nesscomputing.jmx.starter.JmxExporterConfig;
import com.nesscomputing.logging.Log;

public class JmxStarterModule extends AbstractModule
{
    public static final String JMX_STARTER_NAME = "_jmxStarter";
    public static final Named JMX_STARTER_NAMED = Names.named(JMX_STARTER_NAME);

    private static final Log LOG = Log.findLog();

    private final Config config;

    public JmxStarterModule(final Config config)
    {
        this.config = config;
    }

    @Override
    public void configure()
    {
        final JmxStarterConfig jmxStarterConfig = config.getBean(JmxStarterConfig.class);
        bind(JmxStarterConfig.class).toInstance(jmxStarterConfig);

        if (jmxStarterConfig.isEnabled()) {
            LOG.info("Exporting JMX...");

            final ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();

            if (StringUtils.isNotBlank(jmxStarterConfig.getPasswordFile())) {
                builder.put("jmx.remote.x.password.file", jmxStarterConfig.getPasswordFile());
            }
            if (StringUtils.isNotBlank(jmxStarterConfig.getAccessFile())) {
                builder.put("jmx.remote.x.access.file", jmxStarterConfig.getAccessFile());
            }

            bind(new TypeLiteral<Map<String, String>>() {}).annotatedWith(JMX_STARTER_NAMED).toInstance(builder.build());

            bind(JmxExporterConfig.class).toProvider(JmxExporterConfigProvider.class).in(Scopes.SINGLETON);
            bind(JmxExporter.class).asEagerSingleton();
        }
        else {
            LOG.info("Not exporting JMX.");
        }
    }
}
