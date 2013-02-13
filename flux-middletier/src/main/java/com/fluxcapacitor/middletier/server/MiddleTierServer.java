package com.fluxcapacitor.middletier.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fluxcapacitor.core.AppConfiguration;
import com.fluxcapacitor.core.server.BaseNettyServer;
import com.fluxcapacitor.core.util.FluxModule;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.DiscoveryManager;
import com.netflix.governator.guice.LifecycleInjector;
import com.netflix.governator.lifecycle.LifecycleManager;

/**
 * @author cfregly
 */
public class MiddleTierServer extends BaseNettyServer {
    private static final Logger logger = LoggerFactory.getLogger(MiddleTierServer.class);
    
    @Inject
    public MiddleTierServer(AppConfiguration config) {
    	super(config);
    }
    
    public void start() {
        super.start();
        
        waitUntilFullyRegisteredWithEureka();        
    }

    private void waitUntilFullyRegisteredWithEureka() {
        // use applicationId as the vipAddress
        String vipAddress = System.getProperty("archaius.deployment.applicationId");

        InstanceInfo nextServerInfo = null;
        while (nextServerInfo == null) {
            try {
                nextServerInfo = DiscoveryManager.getInstance()
                .getDiscoveryClient()
                .getNextServerFromEureka(vipAddress, false);
            } catch (Throwable e) {
                System.out
                .println("Waiting for service to register with eureka..");

                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e1) {
                    logger.warn("Thread interrupted while sleeping during the wait for eureka service registration.  This is probably OK.");
                }
            }
        }
        
        logger.info("Registered middletier service with eureka: [{}]", nextServerInfo);
    }

    private void unRegisterWithEureka() {
        // Un register from eureka.
        DiscoveryManager.getInstance().shutdownComponent();
    }

    @Override
    public void close() {
    	super.close();

    	unRegisterWithEureka();
    }
        
    public static void main(String args[]) throws Exception {
    	System.setProperty("archaius.deployment.applicationId", "middletier");

    	Injector injector = LifecycleInjector.builder().withModules(new FluxModule()).createInjector();

    	LifecycleManager lifecycleManager = injector.getInstance(LifecycleManager.class);
    	lifecycleManager.start();
    	
    	MiddleTierServer middleTierServer = injector.getInstance(MiddleTierServer.class);
    	middleTierServer.start();
    }
}
