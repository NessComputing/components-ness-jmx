package com.nesscomputing.jmx.jolokia;

import org.skife.config.Config;
import org.skife.config.Default;

interface JolokiaConfig
{
    @Config("ness.jolokia.enabled")
    @Default("false")
    boolean isJolokiaEnabled();
}
