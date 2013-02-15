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
package com.fluxcapacitor.core.netty;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fluxcapacitor.core.netty.NettyServer.ClientPipelineFactory;
import com.fluxcapacitor.core.netty.NettyServer.PipelineFactory;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

public class NettyClient {
	private static final Logger logger = LoggerFactory
			.getLogger(NettyClient.class);
	
	/**
	 * @return Builder object which will help build the client and server
	 */
	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private String host;
		private int port = 0; // default is any port

		private Map<String, ChannelHandler> handlers = Maps.newHashMap();

		private ChannelHandler encoder = new HttpResponseEncoder();
		private ChannelHandler decoder = new HttpRequestDecoder();

		public Builder host(String host) {
			this.host = host;
			return this;
		}

		public Builder port(int port) {
			this.port = port;
			return this;
		}

		public Builder addHandler(String name, ChannelHandler handler) {
			Preconditions.checkNotNull(handler);
			handlers.put(name, handler);
			return this;
		}

		public Builder encoder(ChannelHandler encoder) {
			this.encoder = encoder;
			return this;
		}

		public Builder decoder(ChannelHandler decoder) {
			this.decoder = decoder;
			return this;
		}
	
		/**
		 * Creates a connection to the given host and port
		 */
		public Channel buildClient() {
			PipelineFactory pipelinefactory = new ClientPipelineFactory(
					handlers, encoder, decoder);
	
			ChannelFactory nioServer = new NioClientSocketChannelFactory(
					Executors.newCachedThreadPool(),
					Executors.newCachedThreadPool());
	
			ClientBootstrap bootstrap = new ClientBootstrap(nioServer);
			bootstrap.setOption("reuseAddress", true);
			bootstrap.setOption("keepAlive", true);
			bootstrap.setOption("child.tcpNoDelay", true);
			bootstrap.setOption("child.keepAlive", true);
			bootstrap.setPipelineFactory(pipelinefactory);
	
			InetSocketAddress address = new InetSocketAddress(host, port);
	
			ChannelFuture channelFuture = bootstrap.connect(address);
			channelFuture.addListener(createChannelFutureListener(bootstrap,
					address));
	
			boolean isConnected = channelFuture.awaitUninterruptibly()
					.isSuccess();
			if (!isConnected) {
				throw new RuntimeException("Cannot connect to " + host + ":"
						+ port);
			}
	
			logger.info("Created outgoing connection to {}:{}", host, port);
	
			return channelFuture.getChannel();
		}
	}
	
	/**
	 * http://stackoverflow.com/questions/9873237/netty-clientbootstrap-connect-retries
	 * 
	 * @param bs
	 * @param address
	 * 
	 * @return
	 */
	private static final int maxretries = 5;

	private static ChannelFutureListener createChannelFutureListener(
			final ClientBootstrap bs, final InetSocketAddress address) {

		final AtomicInteger retryCount = new AtomicInteger();

		return new ChannelFutureListener() {
			@Override
			public void operationComplete(ChannelFuture future)
					throws Exception {
				if (!future.isSuccess()) {
					if (retryCount.incrementAndGet() > maxretries) {
						future.getChannel().close();
					} else {
						bs.connect(address).addListener(this);
					}
				}
			}
		};
	}
}