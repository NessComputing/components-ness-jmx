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
package com.nesscomputing.jmx.jolokia;

import java.util.Map;

import org.apache.commons.configuration.Configuration;
import org.jolokia.backend.BackendManager;
import org.jolokia.http.HttpRequestHandler;
import org.jolokia.restrictor.AllowAllRestrictor;
import org.jolokia.restrictor.Restrictor;
import org.jolokia.util.ConfigKey;
import org.jolokia.util.LogHandler;

import com.google.common.collect.Maps;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import com.google.inject.servlet.ServletModule;
import com.nesscomputing.config.Config;
import com.nesscomputing.logging.Log;

/**
 * Bind the jolokia servlet in the Trumpet context.
 */
public class JolokiaModule extends ServletModule
{
    @Override
    public void configureServlets()
    {
        bind(JolokiaServlet.class).in(Scopes.SINGLETON);
        serve("/jolokia/*").with(JolokiaServlet.class);
        configureRestrictor();
    }

    /**
     * Override this method to implement access restrictions.
     */
    protected void configureRestrictor()
    {
        bind(Restrictor.class).to(AllowAllRestrictor.class).in(Scopes.SINGLETON);
    }

    @Provides
    @Singleton
    BackendManager getBackendManager(final Config config, final LogHandler logHandler, final Restrictor restrictor)
    {
        final Configuration jolokiaConfig = config.getConfiguration("ness.jolokia");
        final Map<ConfigKey, String> configMap = Maps.newEnumMap(ConfigKey.class);

        for (final ConfigKey key : ConfigKey.values()) {
            if (key.isGlobalConfig()) {
                configMap.put(key, jolokiaConfig.getString(key.getKeyValue(), key.getDefaultValue()));
            }
        }

        return new BackendManager(configMap, logHandler, restrictor);
    }

    @Provides
    @Singleton
    HttpRequestHandler getHttpRequestHandler(final LogHandler logHandler, final BackendManager backendManager)
    {
        return new HttpRequestHandler(backendManager, logHandler);
    }

    @Provides
    @Singleton
    LogHandler getLogHandler()
    {
        final Log jolokiaLog = Log.forName("jolokia");

        return new LogHandler() {

            @Override
            public void debug(final String message)
            {
                jolokiaLog.debug(message);
            }

            @Override
            public void info(final String message)
            {
                jolokiaLog.info(message);
            }

            @Override
            public void error(final String message, final Throwable t)
            {
                jolokiaLog.error(t, message);
            }
        };
    }
}
