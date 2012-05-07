package ness.jmx.starter;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;

/**
 * Custom Socket factory that only returns sockets bound to the given InetAddress.
 */
public class JmxSocketFactory implements RMIServerSocketFactory, RMIClientSocketFactory
{
    private final InetAddress bindAddress;

    public JmxSocketFactory(final InetAddress bindAddress)
    {
        this.bindAddress = bindAddress;
    }

    @Override
    public ServerSocket createServerSocket(final int port)
        throws IOException
    {
        return new ServerSocket((port != 0) ? port : NetUtils.findUnusedPort(), 0, bindAddress);
    }

    @Override
    public Socket createSocket(final String host, final int port) throws IOException
    {
        return new Socket(host, port);
    }
}
