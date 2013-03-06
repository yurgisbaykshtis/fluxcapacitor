package com.fluxcapacitor.core.metrics;

import java.io.Closeable;

public interface AppMetrics extends Closeable {
	public void start() throws Exception;
}