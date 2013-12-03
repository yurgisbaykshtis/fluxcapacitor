package com.fluxcapacitor.core.metrics;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.fluxcapacitor.core.config.AppConfiguration;
import com.google.inject.Inject;
import com.netflix.governator.annotations.AutoBindSingleton;
import com.netflix.servo.publish.AsyncMetricObserver;
import com.netflix.servo.publish.BasicMetricFilter;
import com.netflix.servo.publish.CounterToRateMetricTransform;
import com.netflix.servo.publish.MetricObserver;
import com.netflix.servo.publish.MetricPoller;
import com.netflix.servo.publish.MonitorRegistryMetricPoller;
import com.netflix.servo.publish.PollRunnable;
import com.netflix.servo.publish.PollScheduler;
import com.netflix.servo.publish.cloudwatch.CloudWatchMetricObserver;
import com.netflix.servo.publish.graphite.GraphiteMetricObserver;
import com.netflix.servo.publish.stackdriver.StackdriverMetricObserver;

@AutoBindSingleton(AppMetrics.class)
public class FluxMetrics implements AppMetrics {
	
	private AppConfiguration config;
		
	@Inject
	public FluxMetrics(AppConfiguration config) {
		this.config = config;
	}
	
	public void start() throws Exception {
        final List<MetricObserver> observers = new ArrayList<MetricObserver>();
        observers.add(createGraphiteObserver());
        //observers.add(createCloudWatchObserver());

        final MetricPoller poller = new MonitorRegistryMetricPoller();
        final PollRunnable task = new PollRunnable(poller, BasicMetricFilter.MATCH_ALL, observers);

        PollScheduler.getInstance().start();
        PollScheduler.getInstance().addPoller(task, config.getLong("metrics.poll.interval", 30), TimeUnit.SECONDS);
	}
	
    private MetricObserver createCloudWatchObserver() {
    	return new CloudWatchMetricObserver("FluxMetricsObserver", "Flux",
    			new DefaultAWSCredentialsProviderChain());
    }

	private MetricObserver rateTransform(MetricObserver observer) {
        final long heartbeat = 2 * config.getLong("metrics.poll.interval", 5);
        return new CounterToRateMetricTransform(observer, heartbeat, TimeUnit.SECONDS);
    }

    private MetricObserver async(String name, MetricObserver observer) {
        final long expireTime = 2000 * config.getLong("metrics.poll.interval", 5);
        final int queueSize = 10;
        return new AsyncMetricObserver(name, observer, queueSize, expireTime);
    }

    private MetricObserver createGraphiteObserver() {
        final String prefix = config.getString("metrics.prefix", "not-found-in-flux-configuration");
        final String addr = config.getString("graphite.server.address", "not-found-in-flux-configuration");
        
        return rateTransform(async("graphite", new GraphiteMetricObserver(prefix, addr)));
    }

    @Override
    public void close() {
    }
}