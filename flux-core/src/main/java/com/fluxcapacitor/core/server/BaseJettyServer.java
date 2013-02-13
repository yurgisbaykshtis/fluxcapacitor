package com.fluxcapacitor.core.server;

import java.io.Closeable;

import javax.ws.rs.core.Application;

import org.apache.jasper.servlet.JspServlet;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.bio.SocketConnector;
import org.mortbay.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fluxcapacitor.core.AppConfiguration;
import com.fluxcapacitor.core.util.InetAddressUtils;
import com.google.inject.Inject;
import com.netflix.appinfo.ApplicationInfoManager;
import com.netflix.appinfo.MyDataCenterInstanceConfig;
import com.netflix.appinfo.InstanceInfo.InstanceStatus;
import com.netflix.blitz4j.LoggingConfiguration;
import com.netflix.discovery.DefaultEurekaClientConfig;
import com.netflix.discovery.DiscoveryManager;
import com.netflix.hystrix.contrib.metrics.eventstream.HystrixMetricsStreamServlet;
import com.netflix.servo.publish.BasicMetricFilter;
import com.netflix.servo.publish.MonitorRegistryMetricPoller;
import com.netflix.servo.publish.PollCallable;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.spi.container.servlet.ServletContainer;

import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.DefaultServlet;
import org.mortbay.jetty.servlet.ServletHolder;

/**
 * @author cfregly
 */
public class BaseJettyServer implements Closeable {
	private static final Logger logger = LoggerFactory
			.getLogger(BaseJettyServer.class);

	public final Server server;
	public final String host;
	public final int port;
	protected final AppConfiguration config;

	@Inject
	public BaseJettyServer(AppConfiguration config) {
		this.config = config;

		server = new Server();

		port = config.getInt("jetty.http.port", Integer.MIN_VALUE);
		host = InetAddressUtils.getBestReachableIp();
	}

	public void start() {
		LoggingConfiguration.getInstance().configure();

		// NOTE: make sure any changes made here are reflected in web.xml -->

		// Thanks to Aaron Wirtz for this snippet:
		// 	
		final Context context = new Context(server, "/", Context.SESSIONS);
		context.setResourceBase("webapp");

		context.setClassLoader(Thread.currentThread().getContextClassLoader());

		context.addServlet(JspServlet.class, "*.jsp");

		// TODO:  enable Jersey
		//	PackagesResourceConfig rcf = new PackagesResourceConfig(
		//		config.getString("jersey.resources.package",
		//		"not-found-in-configuration"));
		// ServletContainer container = new ServletContainer(rcf);
		// context.addServlet(new ServletHolder(container), "/edge/*");

		// TODO:  enable hystrix.stream
		context.addServlet(HystrixMetricsStreamServlet.class, "/hystrix.stream");
		
		final Server server = new Server(port);
		server.setHandler(context);

		try {
			server.start();
		} catch (Exception exc) {
			logger.error("Error starting jetty.", exc);
		}

		// Enable eureka client functionality
		MyDataCenterInstanceConfig instanceConfig = new MyDataCenterInstanceConfig();

		ApplicationInfoManager.getInstance().initComponent(instanceConfig);
		ApplicationInfoManager.getInstance().setInstanceStatus(
				InstanceStatus.UP);

		// Note: If running locally on MacOS, you'll need to make sure your
		// /etc/hosts file contains the following entry:
		// 127.0.0.1 <hostname>.local
		// or else clients will likely not be able to connect.
		DiscoveryManager.getInstance().initComponent(instanceConfig,
				new DefaultEurekaClientConfig());		
	}

	@Override
	public void close() {
		try {
			server.stop();
		} catch (Exception exc) {
			logger.error("Error stopping jetty.", exc);
		}
		LoggingConfiguration.getInstance().stop();
	}
}
