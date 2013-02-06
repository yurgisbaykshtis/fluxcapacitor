package com.fluxcapacitor.middletier.test.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fluxcapacitor.core.util.FluxModule;
import com.fluxcapacitor.middletier.server.MiddleTierServer;
import com.google.common.io.Closeables;
import com.google.inject.Injector;
import com.netflix.governator.guice.LifecycleInjector;
import com.netflix.governator.lifecycle.LifecycleManager;

/**
 * @author cfregly
 */
public class EmbeddedMiddleTierForTests {
    private static final Logger logger = LoggerFactory.getLogger(EmbeddedMiddleTierForTests.class);

    public  MiddleTierServer middleTierServer;
    
    public EmbeddedMiddleTierForTests() {
    }
    
    public void setUp() throws Exception {
    	System.setProperty("archaius.deployment.applicationId", "middletier");
    	System.setProperty("archaius.deployment.environment", "ci");
        
    	Injector injector = LifecycleInjector.builder().withModules(new FluxModule()).createInjector();

    	LifecycleManager lifecycleManager = injector.getInstance(LifecycleManager.class);
    	lifecycleManager.start();

    	middleTierServer = injector.getInstance(MiddleTierServer.class);
    	middleTierServer.start();
    }

    public void tearDown() throws Exception {
        Closeables.closeQuietly(middleTierServer);
    }
}
