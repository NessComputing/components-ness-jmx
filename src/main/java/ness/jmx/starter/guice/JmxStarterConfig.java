package ness.jmx.starter.guice;

import org.skife.config.Config;
import org.skife.config.Default;
import org.skife.config.DefaultNull;

public abstract class JmxStarterConfig
{
    @Config("ness.jmx.enabled")
    @Default("true")
    public boolean isEnabled()
    {
        return true;
    }

    @Config("ness.jmx.bind-internal")
    @Default("true")
    public boolean isBindInternal()
    {
        return true;
    }

    @Config("ness.jmx.access-file")
    @DefaultNull
    public String getAccessFile()
    {
        return null;
    }

    @Config("ness.jmx.password-file")
    @DefaultNull
    public String getPasswordFile()
    {
        return null;
    }
}
