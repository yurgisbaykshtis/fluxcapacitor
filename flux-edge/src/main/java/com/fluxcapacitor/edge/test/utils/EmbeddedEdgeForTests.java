package com.fluxcapacitor.edge.test.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fluxcapacitor.core.util.FluxModule;
import com.fluxcapacitor.edge.server.EdgeServer;
import com.google.common.io.Closeables;
import com.google.inject.Injector;
import com.netflix.governator.guice.LifecycleInjector;
import com.netflix.governator.lifecycle.LifecycleManager;

/**
 * @author cfregly
 */
public class EmbeddedEdgeForTests {
	private static final Logger logger = LoggerFactory
			.getLogger(EmbeddedEdgeForTests.class);

	public EdgeServer edgeServer;

	public EmbeddedEdgeForTests() {
	}

	public void setUp() throws Exception {
		System.setProperty("archaius.deployment.applicationId", "edge");
		System.setProperty("archaius.deployment.environment", "ci");

		Injector injector = LifecycleInjector.builder()
				.withModules(new FluxModule()).createInjector();

		LifecycleManager lifecycleManager = injector
				.getInstance(LifecycleManager.class);
		lifecycleManager.start();

		edgeServer = injector.getInstance(EdgeServer.class);
		edgeServer.start();
	}

	public void tearDown() throws Exception {
		Closeables.closeQuietly(edgeServer);
	}
}
