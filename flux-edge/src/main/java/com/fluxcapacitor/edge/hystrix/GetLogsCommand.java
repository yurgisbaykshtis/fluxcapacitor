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
package com.fluxcapacitor.edge.hystrix;

import java.net.URI;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fluxcapacitor.core.FluxConstants;
import com.google.common.base.Charsets;
import com.netflix.client.ClientFactory;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixThreadPoolKey;
import com.netflix.niws.client.http.HttpClientRequest;
import com.netflix.niws.client.http.HttpClientRequest.Verb;
import com.netflix.niws.client.http.HttpClientResponse;
import com.netflix.niws.client.http.RestClient;

/**
 * Retrieve the logs.
 * 
 * Example of a basic command with fallback.
 * 
 * By default, timeout is set to 1000ms per the following configuration
 * property:
 * 
 *   	hystrix.command.default.execution.isolation.thread.timeoutInMilliseconds=1000
 * 
 * This timeout can be overridden using any archaius configuration source.
 * 
 * Uses Netflix Ribbon as the communication mechanism to the middletier service.
 * 
 * @author Chris Fregly (chris@fregly.com)
 */
public class GetLogsCommand extends HystrixCommand<String> {
	private static final Logger logger = LoggerFactory.getLogger(GetLogsCommand.class);

	private final String key;
	
	public GetLogsCommand(String key) {
		super(
				Setter.withGroupKey(
						HystrixCommandGroupKey.Factory
								.asKey(FluxConstants.MIDDLETIER_HYSTRIX_GROUP))
						.andCommandKey(
								HystrixCommandKey.Factory
										.asKey(FluxConstants.MIDDLETIER_HYSTRIX_GET_LOGS_COMMAND_KEY))
						.andThreadPoolKey(
								HystrixThreadPoolKey.Factory
										.asKey(FluxConstants.MIDDLETIER_HYSTRIX_THREAD_POOL)));
		
		this.key = key;
	}

	@Override
	protected String run() {
		try {
			// The named client param must match the prefix for the ribbon
			// configuration specified in the edge.properties file
			RestClient client = (RestClient) ClientFactory
					.getNamedClient("middletier-client");

			//MultivaluedMap<String, String> headers = new MultivaluedMapImpl();
			//headers.putSingle("Content-Type", "text/plain");

			// Note: If running locally on MacOS, you'll need to make sure your
			// /etc/hosts file contains the following entry:
			// 127.0.0.1 <hostname>.local
			// or else this will likely fail.
			HttpClientRequest request = HttpClientRequest
					.newBuilder()
					.setVerb(Verb.GET)
					.setUri(new URI("/service/"
							+ FluxConstants.MIDDLETIER_WEB_RESOURCE_ROOT_PATH
							+ "/"
							+ FluxConstants.MIDDLETIER_WEB_RESOURCE_VERSION
							+ "/"
							+ FluxConstants.MIDDLETIER_WEB_RESOURCE_GET_PATH
							+ "/"
							+ key))
					//.setHeaders(headers)
					.build();
			
			HttpClientResponse response = client
					.executeWithLoadBalancer(request);

			if (response.getStatus() != 200) {	
				logger.error("error status: {}", response.getStatus());
				throw new Exception("error status: " + response.getStatus());
			}
			
			return IOUtils.toString(response.getRawEntity(), Charsets.UTF_8);
		} catch (Exception exc) {
			logger.error("Exception calling middletier while retrieving logs.", exc);
			throw new RuntimeException("Exception", exc);
		}
	}

	@Override
	protected String getFallback() {
		return "Fallback:  Great Scott!";
	}
}
