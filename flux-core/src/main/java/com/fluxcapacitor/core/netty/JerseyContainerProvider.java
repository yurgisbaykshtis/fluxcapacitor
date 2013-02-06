package com.fluxcapacitor.core.netty;

import com.sun.jersey.api.container.ContainerException;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.spi.container.ContainerProvider;
import com.sun.jersey.spi.container.WebApplication;

/**
 * This class is referenced in the following jersey configuration file:
 * 
 * src/main/resources/META-INF/services/com.sun.jersey.spi.container.
 * ContainerProvider
 * 
 * @author cfregly
 */
public class JerseyContainerProvider implements
		ContainerProvider<NettyHandlerContainer> {

	public NettyHandlerContainer createContainer(
			Class<NettyHandlerContainer> clazz, ResourceConfig config,
			WebApplication webApp) throws ContainerException {
		if (clazz != NettyHandlerContainer.class) {
			return null;
		}
		return new NettyHandlerContainer(webApp, config);
	}
}