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
package com.fluxcapacitor.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.netflix.config.ConcurrentCompositeConfiguration;
import com.netflix.config.ConfigurationManager;
import com.netflix.config.DynamicBooleanProperty;
import com.netflix.config.DynamicIntProperty;
import com.netflix.config.DynamicLongProperty;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;
import com.netflix.governator.annotations.AutoBindSingleton;



/**
 * FluxConfiguration follows a hierarchy as follows: <appId>-<env>.properties
 * (optional: <env>=local|dev|qa|prod) <appId>.properties (default values)
 * System Properties (-D)
 * 
 * JMX: All properties can be viewed and updated (on a per instance basis) here:
 * Config-com.netflix.config.jmx.BaseConfigMBean
 * 
 * @author Chris Fregly (chris@fregly.com)
 */
@AutoBindSingleton(AppConfiguration.class)
public class FluxConfiguration implements AppConfiguration {
	private static final Logger logger = LoggerFactory
			.getLogger(FluxConfiguration.class);

	public FluxConfiguration() {
		String app = ConfigurationManager.getDeploymentContext().getApplicationId();
		String env = ConfigurationManager.getDeploymentContext().getDeploymentEnvironment();

		logger.info(
				"Configuration set to application [{}] and env [{}]", app, env);
		
		if (Strings.isNullOrEmpty(app)) {
			logger.warn("System property 'archaius.deployment.applicationId' is empty.  Configuration may be invalid.");
		}
		
		if (Strings.isNullOrEmpty(env)) {
			logger.warn("System property 'archaius.deployment.environment' is empty.  Configuration may be invalid.  Set APP_ENV environment variable to populate the environment appropriately.");
		}
	}

	@Override
	public String getString(String key, String defaultValue) {
		final DynamicStringProperty property = DynamicPropertyFactory
				.getInstance().getStringProperty(key, defaultValue);
		return property.get();
	}

	@Override
	public int getInt(String key, int defaultValue) {
		final DynamicIntProperty property = DynamicPropertyFactory
				.getInstance().getIntProperty(key, defaultValue);
		return property.get();
	}

	@Override
	public long getLong(String key, int defaultValue) {
		final DynamicLongProperty property = DynamicPropertyFactory
				.getInstance().getLongProperty(key, defaultValue);
		return property.get();
	}

	@Override
	public boolean getBoolean(String key, boolean defaultValue) {
		final DynamicBooleanProperty property = DynamicPropertyFactory
				.getInstance().getBooleanProperty(key, defaultValue);
		return property.get();
	}

	@Override
	@VisibleForTesting
	public void setOverrideProperty(String key, Object value) {
		((ConcurrentCompositeConfiguration) ConfigurationManager
				.getConfigInstance()).setOverrideProperty(key, value);
	}

	@Override
	public void close() {
	}
}
