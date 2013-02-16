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
package com.fluxcapacitor.edge.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fluxcapacitor.core.server.BaseJettyServer;
import com.netflix.config.ConfigurationManager;
import com.netflix.karyon.spi.PropertyNames;

/**
 * @author cfregly
 */
public class EdgeServer extends BaseJettyServer {
	private static final Logger logger = LoggerFactory
			.getLogger(EdgeServer.class);
	
	public EdgeServer() {
	}

	public static void main(final String[] args) throws Exception {
		System.setProperty("archaius.deployment.applicationId", "edge");
        System.setProperty(PropertyNames.SERVER_BOOTSTRAP_BASE_PACKAGES_OVERRIDE, "com.fluxcapacitor");

		// populate the eureka-specific properties
		System.setProperty("eureka.client.props", ConfigurationManager
				.getDeploymentContext().getApplicationId());
		System.setProperty("eureka.environment", ConfigurationManager
				.getDeploymentContext().getDeploymentEnvironment());

		EdgeServer edgeServer = new EdgeServer();
		edgeServer.start();
	}
}