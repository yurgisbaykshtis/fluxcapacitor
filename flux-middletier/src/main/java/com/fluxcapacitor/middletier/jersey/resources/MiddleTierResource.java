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
package com.fluxcapacitor.middletier.jersey.resources;

import java.net.URLDecoder;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fluxcapacitor.middletier.store.AppStore;
import com.fluxcapacitor.middletier.store.dynamodb.FluxDynamoDbStore;
import com.google.common.base.Charsets;
import com.google.gson.Gson;
import com.netflix.servo.DefaultMonitorRegistry;
import com.netflix.servo.monitor.BasicCounter;
import com.netflix.servo.monitor.Counter;
import com.netflix.servo.monitor.MonitorConfig;
import com.netflix.servo.monitor.StatsTimer;
import com.netflix.servo.monitor.Stopwatch;
import com.netflix.servo.stats.StatsConfig;

/**
 * @author Chris Fregly (chris@fregly.com)
 */
@Path("middletier")
public class MiddleTierResource {
    private static final Logger logger = LoggerFactory.getLogger(MiddleTierResource.class);

    // JMX:  com.netflix.servo.COUNTER.MiddleTierResource_requestCounter
    private static Counter requestCounter = new BasicCounter(MonitorConfig.builder("MiddleTierResource_requestCounter").build());

    // JMX:  com.netflix.servo.COUNTER.MiddleTierResource_requestCounter
    private static Counter errorCounter = new BasicCounter(MonitorConfig.builder("MiddleTierResource_errorCounter").build());
    
    // JMX:  com.netflix.servo.COUNTER.MiddleTierResource_statsTimer
    // JMX:  com.netflix.servo.MiddleTierResource_statsTimer (95th and 99th percentile)
    private static StatsTimer statsTimer = new StatsTimer(MonitorConfig.builder("MiddleTierResource_statsTimer").build(), new StatsConfig.Builder().build());
        
    private final AppStore store;
    private final Gson gson = new Gson();
    
    static {
    	DefaultMonitorRegistry.getInstance().register(requestCounter);
    	DefaultMonitorRegistry.getInstance().register(errorCounter);
    	DefaultMonitorRegistry.getInstance().register(statsTimer);
    }

    public MiddleTierResource() {
    	store = new FluxDynamoDbStore();    	

    	// TODO:  avoid calling start() from the constructor.
    	// 		  it would be nice to use Guice/Governator to control the lifecycle of these jersey resources
    	store.start();    	
    }
    
    @GET
    @Path("/v1/logs/{key}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getLogs(final @PathParam("key") String key) {
    	Stopwatch stopwatch = statsTimer.start();
    	try {
        	// increment request counter
        	requestCounter.increment();
        	            
            List<String> logs = store.getLogs(key);
            
            logger.debug("retrieved key={} logs={}", key, logs);
            
            return Response.ok(gson.toJson(logs)).build();            
        } catch (Exception ex) {
        	// increment error counter
        	errorCounter.increment();

            logger.error("Error processing the get request.", ex);
            
            return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).build();
        } finally {
            stopwatch.stop();
            
            statsTimer.record(stopwatch.getDuration(TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS);
        }
    }
    
    @POST
    @Path("/v1/log/{key}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response addLog(final @PathParam("key") String key, final @QueryParam("log") String log) {    
    	Stopwatch stopwatch = statsTimer.start();
    	try {
        	// increment request counter
        	requestCounter.increment();
        	
        	String decodedLog = URLDecoder.decode(log, Charsets.UTF_8.name());
        	            
            long timestamp = store.addLog(key, decodedLog);
            
            logger.debug("added key={} log={} timestamp={}", key, decodedLog, timestamp); 
            
            // return response
            return Response.ok(gson.toJson(String.valueOf(timestamp))).build();            
        } catch (Exception ex) {
        	// increment error counter
        	errorCounter.increment();

            logger.error("Error processing the add request.", ex);
            
            return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).build();
        } finally {
            stopwatch.stop();
            
            statsTimer.record(stopwatch.getDuration(TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS);
        }
    }
}
