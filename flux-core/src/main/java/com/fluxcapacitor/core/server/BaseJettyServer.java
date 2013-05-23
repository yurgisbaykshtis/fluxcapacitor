/*
 * 	Copyright 2012 Chris Fregly
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.fluxcapacitor.core.server;

import java.io.Closeable;

import org.apache.jasper.servlet.JspServlet;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fluxcapacitor.core.util.InetAddressUtils;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.hystrix.contrib.metrics.eventstream.HystrixMetricsStreamServlet;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.spi.container.servlet.ServletContainer;

/**
 * @author Chris Fregly (chris@fregly.com)
 */
public class BaseJettyServer extends BaseServer implements Closeable {
	private static final Logger logger = LoggerFactory
			.getLogger(BaseJettyServer.class);

	public Server jettyServer;

	public BaseJettyServer() {	
	}

	@Override
	public void start() {
		super.start();
		
		port = DynamicPropertyFactory.getInstance()
				.getIntProperty("jetty.http.port", Integer.MIN_VALUE).get();
		host = InetAddressUtils.getBestReachableIp();

		jettyServer = new Server(port);

		// NOTE: make sure any changes made here are reflected in web.xml -->
		final Context context = new Context(jettyServer, "/", Context.SESSIONS);
		context.setResourceBase("webapp");
		context.setClassLoader(Thread.currentThread().getContextClassLoader());

		// enable jsp's
		context.addServlet(JspServlet.class, "/jsp/*.jsp");

		// enable Jersey REST endpoints
		final PackagesResourceConfig rcf = new PackagesResourceConfig(
				DynamicPropertyFactory
						.getInstance()
						.getStringProperty("jersey.resources.package",
								"not-found-in-configuration").get());
		
		final ServletContainer container = new ServletContainer(rcf);
		context.addServlet(new ServletHolder(container), "/service/*");

		// enable hystrix.stream
		context.addServlet(HystrixMetricsStreamServlet.class, "/hystrix.stream");
	
		jettyServer.setHandler(context);
		
		try {
			jettyServer.start();
		} catch (Exception exc) {
			logger.error("Error starting jetty.", exc);
			throw new RuntimeException("Error starting jetty.", exc);
		}
		
		logger.info("Started jetty server at {}:{}", host, port);
	}

	@Override
	public void close() {
		try {
			jettyServer.stop();
		} catch (Exception exc) {
			logger.error("Error shutting down jetty.", exc);
		}
		
		super.close();
	}
}
