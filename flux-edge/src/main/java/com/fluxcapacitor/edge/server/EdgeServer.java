package com.fluxcapacitor.edge.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fluxcapacitor.core.AppConfiguration;
import com.fluxcapacitor.core.server.BaseJettyServer;
import com.fluxcapacitor.core.util.FluxModule;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.netflix.config.ConfigurationManager;
import com.netflix.governator.guice.LifecycleInjector;
import com.netflix.governator.lifecycle.LifecycleManager;

/**
 * @author cfregly
 */
public class EdgeServer extends BaseJettyServer {
	private static final Logger logger = LoggerFactory
			.getLogger(EdgeServer.class);

	@Inject
	public EdgeServer(AppConfiguration config) {
		super(config);

		// populate the eureka-specific properties
		System.setProperty("eureka.client.props", ConfigurationManager
				.getDeploymentContext().getApplicationId());
		System.setProperty("eureka.environment", ConfigurationManager
				.getDeploymentContext().getDeploymentEnvironment());
	}

	public static void main(final String[] args) throws Exception {
		System.setProperty("archaius.deployment.applicationId", "edge");

		Injector injector = LifecycleInjector.builder()
				.withModules(new FluxModule()).createInjector();

		LifecycleManager lifecycleManager = injector
				.getInstance(LifecycleManager.class);
		lifecycleManager.start();

		EdgeServer edgeServer = injector.getInstance(EdgeServer.class);
		edgeServer.start();
	}
}