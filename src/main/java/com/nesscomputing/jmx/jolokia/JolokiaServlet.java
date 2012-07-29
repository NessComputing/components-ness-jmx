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

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import javax.management.RuntimeMBeanException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jolokia.backend.BackendManager;
import org.jolokia.http.HttpRequestHandler;
import org.jolokia.util.ConfigKey;
import org.json.simple.JSONAware;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Expose Jolokia REST APIs through a Guice driven servlet.
 */
@Singleton
public class JolokiaServlet extends HttpServlet
{
    private static final long serialVersionUID = 42L;

    // POST- and GET- HttpRequestHandler
    private transient final ServletRequestHandler httpGetHandler;
    private transient final ServletRequestHandler httpPostHandler;

    // Backend dispatcher
    private transient final BackendManager backendManager;

    // Request handler for parsing request parameters and building up a response
    private transient final HttpRequestHandler requestHandler;

    @Inject
    public JolokiaServlet(final BackendManager backendManager,
                          final HttpRequestHandler requestHandler)
    {
        this.backendManager = backendManager;
        this.requestHandler = requestHandler;

        this.httpGetHandler = new HttpGetHandler();
        this.httpPostHandler = new HttpPostHandler();
    }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException
    {
        handle(httpGetHandler, req, resp);
    }

    @Override
    protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException
    {
        handle(httpPostHandler, req, resp);
    }

    private void handle(final ServletRequestHandler reqHandler, final HttpServletRequest req, final HttpServletResponse resp) throws IOException
    {
        JSONAware json = null;
        try {
            // Check access policy
            requestHandler.checkClientIPAccess(req.getRemoteHost(), req.getRemoteAddr());

            // Dispatch for the proper HTTP request method
            json = reqHandler.handleRequest(req, resp);

            if (backendManager.isDebug()) {
                backendManager.debug("Response: " + json);
            }
        } catch (RuntimeMBeanException rme) {
            json = requestHandler.handleThrowable(rme.getTargetException());
        } catch (Throwable exp) {
            json = requestHandler.handleThrowable(exp);
        } finally {
            final String callback = req.getParameter(ConfigKey.CALLBACK.getKeyValue());
            if (callback != null) {
                // Send a JSONP response
                sendResponse(resp, "text/javascript", callback + "(" + json.toJSONString() +  ");");
            } else {
                sendResponse(resp, "application/json", json.toJSONString());
            }
        }
    }

    private void sendResponse(final HttpServletResponse resp, final String contentType, final String jsonTxt) throws IOException {
        resp.setCharacterEncoding("utf-8");
        resp.addHeader("Access-Control-Allow-Origin", "*");
        resp.setContentType(contentType);
        resp.setStatus(HttpServletResponse.SC_OK);
        final PrintWriter writer = resp.getWriter();
        writer.write(jsonTxt);
        writer.flush();
    }

    private static interface ServletRequestHandler
    {
        JSONAware handleRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException;
    }

    private final class HttpPostHandler implements ServletRequestHandler
    {
        @Override
        public JSONAware handleRequest(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
            final String encoding = req.getCharacterEncoding();
            final InputStream is = req.getInputStream();
            return requestHandler.handlePostRequest(req.getRequestURI(),is, encoding, req.getParameterMap());
        }
    }

    private final class HttpGetHandler implements ServletRequestHandler
    {
        @Override
        public JSONAware handleRequest(final HttpServletRequest req, final HttpServletResponse resp) {
            return requestHandler.handleGetRequest(req.getRequestURI(),req.getPathInfo(),req.getParameterMap());
        }
    }
}
