package com.fluxcapacitor.core.util;

import com.fluxcapacitor.core.AppConfiguration;
import com.fluxcapacitor.core.FluxConfiguration;
import com.google.inject.AbstractModule;

public class FluxModule extends AbstractModule {
	@Override
	protected void configure() {
		bind(AppConfiguration.class).to(FluxConfiguration.class);
	}
}