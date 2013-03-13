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
package com.fluxcapacitor.middletier.server;

import java.io.Closeable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fluxcapacitor.core.config.AppConfiguration;
import com.fluxcapacitor.core.metrics.AppMetrics;
import com.fluxcapacitor.core.netty.NettyHandlerContainer;
import com.fluxcapacitor.core.netty.NettyServer;
import com.fluxcapacitor.core.zookeeper.ZooKeeperClientFactory;
import com.google.common.io.Closeables;
import com.google.inject.Injector;
import com.netflix.blitz4j.LoggingConfiguration;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.karyon.server.KaryonServer;
import com.sun.jersey.api.container.ContainerFactory;
import com.sun.jersey.api.core.PackagesResourceConfig;

/**
 * @author Chris Fregly (chris@fregly.com)
 */
public class BaseNettyServer implements Closeable {
	private static final Logger logger = LoggerFactory
			.getLogger(BaseNettyServer.class);

	public NettyServer nettyServer;
	public final KaryonServer karyonServer;

	public String host;
	public int port;

	protected final Injector injector;

	protected AppConfiguration config;
	protected AppMetrics metrics;

	public BaseNettyServer() {
		// This must be set before karyonServer.initialize() otherwise the
		// archaius properties will not be available in JMX/jconsole
		System.setProperty(DynamicPropertyFactory.ENABLE_JMX, "true");

		this.karyonServer = new KaryonServer();
		this.injector = karyonServer.initialize();		
	}

	public void start() {
		LoggingConfiguration.getInstance().configure();
	
		try {
			karyonServer.start();
		} catch (Exception exc) {
			throw new RuntimeException("Cannot start karyon server.", exc);
		}
		
		// Note:  after karyonServer.start(), the server will be marked as UP in eureka discovery.
		//		  this is not ideal, but we need to call karyonServer.start() in order to start the Guice LifecyleManager 
		//			to ultimately get the FluxConfiguration in the next step...
		
		this.config = injector.getInstance(AppConfiguration.class);
		this.metrics = injector.getInstance(AppMetrics.class);

		// Configure zookeeper config source
		ZooKeeperClientFactory.initializeAndStartZkConfigSource();

		// listen on any interface
		this.host = config.getString("netty.http.host", "not-found-in-configuration");
		this.port = config.getInt("netty.http.port", Integer.MIN_VALUE);

		PackagesResourceConfig rcf = new PackagesResourceConfig(
				config.getString("jersey.resources.package",
						"not-found-in-configuration"));

		try {
			metrics.start();
		} catch (Exception exc) {
			logger.error("Error starting metrics publisher.", exc);
		}

		nettyServer = NettyServer
				.builder()
				.host(host)
				.port(port)
				.addHandler(
						"jerseyHandler",
						ContainerFactory.createContainer(
								NettyHandlerContainer.class, rcf))
				.numBossThreads(NettyServer.cpus)
				.numWorkerThreads(NettyServer.cpus * 4).build();
	}

	@Override
	public void close() {
		Closeables.closeQuietly(nettyServer);
		Closeables.closeQuietly(karyonServer);
		Closeables.closeQuietly(metrics);
		LoggingConfiguration.getInstance().stop();
	}
}
