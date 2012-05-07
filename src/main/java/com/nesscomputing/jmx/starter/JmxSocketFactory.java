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
package com.nesscomputing.jmx.starter;

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
