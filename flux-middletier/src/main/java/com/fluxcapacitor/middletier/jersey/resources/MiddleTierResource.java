package com.fluxcapacitor.middletier.jersey.resources;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.servo.DefaultMonitorRegistry;
import com.netflix.servo.monitor.BasicCounter;
import com.netflix.servo.monitor.Counter;
import com.netflix.servo.monitor.MonitorConfig;
import com.netflix.servo.monitor.StatsTimer;
import com.netflix.servo.monitor.Stopwatch;
import com.netflix.servo.stats.StatsConfig;

/**
 * @author cfregly
 */
@Path("/middletier")
public class MiddleTierResource {
    private static final Logger logger = LoggerFactory.getLogger(MiddleTierResource.class);

    // JMX:  com.netflix.servo.COUNTER.MiddleTierResource_requestCounter
    private static Counter requestCounter = new BasicCounter(MonitorConfig.builder("MiddleTierResource_requestCounter").build());

    // JMX:  com.netflix.servo.COUNTER.MiddleTierResource_requestCounter
    private static Counter errorCounter = new BasicCounter(MonitorConfig.builder("MiddleTierResource_errorCounter").build());
    
    // JMX:  com.netflix.servo.COUNTER.MiddleTierResource_statsTimer
    // JMX:  com.netflix.servo.MiddleTierResource_statsTimer (95th and 99th percentile)
    private static StatsTimer statsTimer = new StatsTimer(MonitorConfig.builder("MiddleTierResource_statsTimer").build(), new StatsConfig.Builder().build());

    private static Random random = new Random();
    
    static {
    	DefaultMonitorRegistry.getInstance().register(requestCounter);
    	DefaultMonitorRegistry.getInstance().register(errorCounter);
    	DefaultMonitorRegistry.getInstance().register(statsTimer);
    }

    public MiddleTierResource() {
    }
    
    @GET
    @Path("/get")
    public Response get() {
    	Stopwatch stopwatch = statsTimer.start();
    	try {
        	// increment request counter
        	requestCounter.increment();
        	            
        	// intentionally multiplying by 2000ms in order to straddle the default hystrix command timeout of 1000ms.
        	// this will cause the calling hystrix command to succeed in some cases and timeout in other cases (triggering client-side fallback).
        	int delayMillis = (int)(random.nextFloat() * 2000);
            Thread.sleep(delayMillis);

            // return response
            String responseString = "Fraggle Rock! serverDelay=" + delayMillis; 
            
            return Response.ok(responseString, MediaType.TEXT_PLAIN).build();            
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
}
