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

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fluxcapacitor.edge.hystrix.BasicFallbackMiddleTierCommand;
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

	public EdgeResource() {
	}

	@GET
	@Path("/get")
	public Response get() {
		Stopwatch stopwatch = statsTimer.start();

		try {
			// increment request counter
			requestCounter.increment();

			// invoke service through Hystrix
			HystrixCommand<String> getCommand = new BasicFallbackMiddleTierCommand();
			Future<String> future = getCommand.queue();
			String responseString = future.get();

			// increment the fallback counter if the response came from a
			// fallback
			// TODO: this isn't needed as the hystrix framework exports its own
			// metrics on a per-command basis.
			// this is here for demo purposes.
			if (getCommand.isResponseFromFallback()) {
				fallbackCounter.increment();
				responseString += " fallback=true";
			}

			// increment the timeout counter if the response timed out and
			// triggered fallback
			// TODO: this isn't needed as the hystrix framework exports its own
			// metrics on a per-command basis.
			// this is here for demo purposes.
			if (getCommand.isResponseTimedOut()) {
				timeoutCounter.increment();
				responseString += " timeout=true";
			}

			// append failed=true
			// TODO: this isn't needed as the hystrix framework exports its own
			// metrics on a per-command basis.
			// this is here for demo purposes.
			if (getCommand.isFailedExecution()) {
				responseString += " failed=true";
			}

			// return response
			return Response.ok(responseString, MediaType.TEXT_PLAIN).build();
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
}
