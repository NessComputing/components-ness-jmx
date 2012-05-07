package com.nesscomputing.jmx.starter;

import java.io.IOException;
import java.net.InetAddress;

/**
 * JMX Export configuration.
 */
public final class JmxExporterConfig
{
    public static JmxExporterConfig defaultJmxExporterConfig(final InetAddress hostname, final Integer port)
        throws IOException
    {
        return defaultJmxExporterConfig(hostname, port, null, true);
    }

    /**
     * Creates a default configuration object.
     *
     * @param hostname The hostname to use. If null, the localhost is used.
     * @param rmiRegistryPort The port for the JMX registry. This is where remote clients will connect to the MBean server.
     * @param rmiServerPort The port for the JMX Server. If null, a random port is used.
     * @param useRandomIds If true, use random ids for RMI.
     * @return A JmxExportConfig object.
     *
     * @throws IOException
     */
    public static JmxExporterConfig defaultJmxExporterConfig(final InetAddress hostname, final Integer rmiRegistryPort, final Integer rmiServerPort, final boolean useRandomIds)
        throws IOException
    {
        return new JmxExporterConfig(
            (hostname != null) ? hostname : InetAddress.getLocalHost(),
            (rmiRegistryPort != null) ? rmiRegistryPort : NetUtils.findUnusedPort(),
            (rmiServerPort != null) ? rmiServerPort : NetUtils.findUnusedPort(),
            useRandomIds);
    }

    private final InetAddress hostname;
    private final int rmiRegistryPort;
    private final int rmiServerPort;
    private final boolean useRandomIds;

    /**
     * Creates configuration object.
     *
     * @param hostname The hostname to use. Must not be null.
     * @param rmiRegistryPort The port for the JMX registry. This is where remote clients will connect to the MBean server. Must be > 0.
     * @param rmiServerPort The port for the JMX Server. Must be > 0.
     * @param useRandomIds If true, use random ids for RMI.
     * @return A JmxExportConfig object.
     *
     * @throws IOException
     */
    public JmxExporterConfig(final InetAddress hostname, final int rmiRegistryPort, final int rmiServerPort, final boolean useRandomIds)
    {
        this.hostname = hostname;
        this.rmiRegistryPort = rmiRegistryPort;
        this.rmiServerPort = rmiServerPort;
        this.useRandomIds = useRandomIds;
    }

    public Integer getRmiRegistryPort()
    {
        return rmiRegistryPort;
    }

    public Integer getRmiServerPort()
    {
        return rmiServerPort;
    }

    public InetAddress getHostname()
    {
        return hostname;
    }

    public boolean useRandomIds()
    {
        return useRandomIds;
    }
}
