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

import com.fluxcapacitor.core.netty.NettyHandlerContainer;
import com.fluxcapacitor.core.netty.NettyServer;
import com.google.common.io.Closeables;
import com.netflix.config.DynamicPropertyFactory;
import com.sun.jersey.api.container.ContainerFactory;
import com.sun.jersey.api.core.PackagesResourceConfig;

/**
 * @author Chris Fregly (chris@fregly.com)
 */
public class BaseNettyServer extends BaseServer implements Closeable {
	private static final Logger logger = LoggerFactory
			.getLogger(BaseNettyServer.class);

	public NettyServer nettyServer;

	public BaseNettyServer() {
	}

	@Override
	public void start() {
		super.start();
		
		// listen on any interface
		this.host = DynamicPropertyFactory.getInstance().getStringProperty("netty.http.host", "not-found-in-configuration").get();
		this.port = DynamicPropertyFactory.getInstance().getIntProperty("netty.http.port", Integer.MIN_VALUE).get();

		PackagesResourceConfig rcf = new PackagesResourceConfig(
				DynamicPropertyFactory.getInstance().getStringProperty("jersey.resources.package",
						"not-found-in-configuration").get());

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
		
		logger.info("Started netty server at {}:{}", host, port);
	}

	@Override
	public void close() {
		Closeables.closeQuietly(nettyServer);
		super.close();
	}
}
