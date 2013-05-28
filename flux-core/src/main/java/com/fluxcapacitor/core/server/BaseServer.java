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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fluxcapacitor.core.config.AppConfiguration;
import com.fluxcapacitor.core.metrics.AppMetrics;
import com.fluxcapacitor.core.zookeeper.ZooKeeperClientFactory;
import com.google.common.io.Closeables;
import com.google.inject.Injector;
import com.netflix.blitz4j.LoggingConfiguration;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.hystrix.Hystrix;
import com.netflix.hystrix.contrib.servopublisher.HystrixServoMetricsPublisher;
import com.netflix.hystrix.strategy.HystrixPlugins;
import com.netflix.karyon.server.KaryonServer;

/**
 * @author Chris Fregly (chris@fregly.com)
 */
public class BaseServer implements Closeable {
	private static final Logger logger = LoggerFactory
			.getLogger(BaseServer.class);

	public final KaryonServer karyonServer;

	public String host;
	public int port;

	protected final Injector injector;

	protected AppConfiguration config;
	protected AppMetrics metrics;

	public BaseServer() {
		// These must be set before karyonServer.initialize() otherwise the
		// archaius properties will not be available in JMX/jconsole
		System.setProperty(DynamicPropertyFactory.ENABLE_JMX, "true");

		this.karyonServer = new KaryonServer();
		this.injector = karyonServer.initialize();
	}
	
	public void start() {
		// This has to come after any System.setProperty() calls as the
		// configure() method triggers the initialization of the
		// ConfigurationManager
		LoggingConfiguration.getInstance().configure();

		try {
			karyonServer.start();
		} catch (Exception exc) {
			throw new RuntimeException("Cannot start karyon server.", exc);
		}

		// Note: after karyonServer.start(), the service will be marked as UP in
		// eureka discovery.
		// this is not ideal, but we need to call karyonServer.start() in order
		// to start the Guice LifecyleManager
		// to ultimately build the FluxConfiguration needed by later steps...
		config = injector.getInstance(AppConfiguration.class);
		metrics = injector.getInstance(AppMetrics.class);

		// Configure zookeeper config source
		try {
			ZooKeeperClientFactory.initializeAndStartZkConfigSource();
		} catch (Exception exc) {
			logger.warn(
					"Cannot initialize and start ZooKeeper configuration source.  ZooKeeper-based properties will not be available.", exc);
		}

		try {
			metrics.start();
		} catch (Exception exc) {
			logger.error("Error starting metrics publisher.", exc);
		}

		HystrixPlugins.getInstance().registerMetricsPublisher(
				HystrixServoMetricsPublisher.getInstance());
	}

	@Override
	public void close() {		
		Closeables.closeQuietly(karyonServer);
		Hystrix.reset();
		Closeables.closeQuietly(metrics);
		LoggingConfiguration.getInstance().stop();
	}
}
