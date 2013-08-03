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
package com.fluxcapacitor.edge.jersey.resources;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.Consumes;
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

import com.fluxcapacitor.core.config.AppConfiguration;
import com.fluxcapacitor.edge.hystrix.AddLogCommand;
import com.fluxcapacitor.edge.hystrix.GetLogsCommand;
import com.google.common.base.Charsets;
import com.google.inject.Inject;
import com.netflix.hystrix.HystrixCommand;
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
@Path("edge")
public class EdgeResource {
	private static final Logger logger = LoggerFactory
			.getLogger(EdgeResource.class);

	// JMX: com.netflix.servo.COUNTER.EdgeResource_requestCounter
	private static Counter requestCounter = new BasicCounter(MonitorConfig
			.builder("EdgeResource_requestCounter").build());

	// JMX: com.netflix.servo.COUNTER.EdgeResource_errorCounter
	private static Counter errorCounter = new BasicCounter(MonitorConfig
			.builder("EdgeResource_errorCounter").build());

	// JMX: com.netflix.servo.COUNTER.EdgeResource_fallbackCounter
	private static Counter fallbackCounter = new BasicCounter(MonitorConfig
			.builder("EdgeResource_fallbackCounter").build());

	// JMX: com.netflix.servo.COUNTER.EdgeResource_timeoutCounter
	private static Counter timeoutCounter = new BasicCounter(MonitorConfig
			.builder("EdgeResource_timeoutCounter").build());

	// JMX: com.netflix.servo.COUNTER.EdgeResource_statsTimer
	// JMX: com.netflix.servo.EdgeResource_statsTimer (95th and 99th percentile)
	private static StatsTimer statsTimer = new StatsTimer(MonitorConfig
			.builder("EdgeResource_statsTimer").build(),
			new StatsConfig.Builder().build());

	static {
		DefaultMonitorRegistry.getInstance().register(requestCounter);
		DefaultMonitorRegistry.getInstance().register(errorCounter);
		DefaultMonitorRegistry.getInstance().register(fallbackCounter);
		DefaultMonitorRegistry.getInstance().register(timeoutCounter);
		DefaultMonitorRegistry.getInstance().register(statsTimer);
	}

	// This will be injected since FluxConfiguration is annotated with @Component
	private final AppConfiguration config;
	
	@Inject
	public EdgeResource(AppConfiguration config) {
		this.config = config;
	}

	@GET
	@Path("/v1/logs/{key}")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getLogs(final @PathParam("key") String key) {
		Stopwatch stopwatch = statsTimer.start();

		try {
			// increment request counter
			requestCounter.increment();

			// invoke service through Hystrix
			HystrixCommand<String> getCommand = new GetLogsCommand(key);
			Future<String> future = getCommand.queue();
			String responseString = future.get();

			// increment the fallback counter if the response came from a
			// fallback
			// TODO: this isn't needed as the hystrix framework exports its own
			// metrics on a per-command basis.
			// this is here for demo purposes.
			if (getCommand.isResponseFromFallback()) {
				fallbackCounter.increment();
			}

			// increment the timeout counter if the response timed out and
			// triggered fallback
			// TODO: this isn't needed as the hystrix framework exports its own
			// metrics on a per-command basis.
			// this is here for demo purposes.
			if (getCommand.isResponseTimedOut()) {
				timeoutCounter.increment();
			}

			// return response
			return Response.ok(responseString).build();
		} catch (Exception ex) {
			// add error counter
			errorCounter.increment();

			logger.error("Error processing the get request.", ex);

			return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).build();
		} finally {
			stopwatch.stop();

			statsTimer.record(stopwatch.getDuration(TimeUnit.MILLISECONDS),
					TimeUnit.MILLISECONDS);
		}
	}
	
	@POST
    @Path("/v1/log/{key}")
    @Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
    public Response addLogWithPost(final @PathParam("key") String key, final String log) {    
		return doAddLog(key, log);
	}
	
	@GET
    @Path("/v1/log/{key}")
    @Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
    public Response addLogWithGet(final @PathParam("key") String key, final @QueryParam("log") String log) {    		
		try {
			return doAddLog(key, URLDecoder.decode(log, Charsets.UTF_8.name()));
		} catch (UnsupportedEncodingException exc) {
			throw new RuntimeException("Cannot decode log '" + log + "'", exc);
		}
	}
	
	private Response doAddLog(final String key, final String log) {
		Stopwatch stopwatch = statsTimer.start();

		try {
			// increment request counter
			requestCounter.increment();

			// invoke service through Hystrix
			HystrixCommand<String> getCommand = new AddLogCommand(key, log);
			
			Future<String> future = getCommand.queue();
			
			String responseString = future.get();

			// increment the fallback counter if the response came from a
			// fallback
			// TODO: this isn't needed as the hystrix framework exports its own
			// metrics on a per-command basis.
			// this is here for demo purposes.
			if (getCommand.isResponseFromFallback()) {
				fallbackCounter.increment();
			}

			// increment the timeout counter if the response timed out and
			// triggered fallback
			// TODO: this isn't needed as the hystrix framework exports its own
			// metrics on a per-command basis.
			// this is here for demo purposes.
			if (getCommand.isResponseTimedOut()) {
				timeoutCounter.increment();
			}

			// return response
			return Response.ok(responseString).build();
		} catch (Exception ex) {
			// add error counter
			errorCounter.increment();

			logger.error("Error processing the add request.", ex);

			return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).build();
		} finally {
			stopwatch.stop();

			statsTimer.record(stopwatch.getDuration(TimeUnit.MILLISECONDS),
					TimeUnit.MILLISECONDS);
		}
	}

}