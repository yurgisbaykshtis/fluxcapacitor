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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fluxcapacitor.core.server.BaseJettyServer;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.discovery.DiscoveryManager;

/**
 * @author Chris Fregly (chris@fregly.com)
 */
public class MiddleTierServer extends BaseJettyServer {
    private static final Logger logger = LoggerFactory.getLogger(MiddleTierServer.class);
    
    public MiddleTierServer() {
    }
    
    public void start() {
        super.start();
        
        waitUntilFullyRegisteredWithEureka();        
    }

    private void waitUntilFullyRegisteredWithEureka() {
        // using applicationId as the vipAddress
        String vipAddress = System.getProperty("archaius.deployment.applicationId");

        InstanceInfo nextServerInfo = null;
        while (nextServerInfo == null) {
            try {
                nextServerInfo = DiscoveryManager.getInstance()
                		.getDiscoveryClient()
                		.getNextServerFromEureka(vipAddress, false);
            } catch (Throwable e) {
                logger.info("Waiting for service to register with eureka..");

                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e1) {
                    logger.warn("Thread interrupted while sleeping during the wait for eureka service registration.  This is probably OK.");
                }
            }
        }
        
        logger.info("Registered middletier service with eureka: [{}]", nextServerInfo);
    }
        
    public static void main(String args[]) throws Exception {
		// This must be set before karyonServer.initialize() otherwise the
		// archaius properties will not be available in JMX/jconsole
		System.setProperty(DynamicPropertyFactory.ENABLE_JMX, "true");

    	MiddleTierServer middleTierServer = new MiddleTierServer();
    	middleTierServer.start();    	
    }
}
