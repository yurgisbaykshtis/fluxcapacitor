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

public class FluxConstants {
	// edge constants
	public static String EDGE_WEB_RESOURCE_ROOT_PATH = "edge";
	public static String EDGE_WEB_RESOURCE_GET_PATH = "get";

	// middletier constants
	public static String MIDDLETIER_EUREKA_SERVICE_NAME = "middletier";

	public static String MIDDLETIER_HYSTRIX_GROUP = "MiddleTierGroup";
	public static String MIDDLETIER_HYSTRIX_COMMAND_KEY = "MiddleTier";
	public static String MIDDLETIER_HYSTRIX_THREAD_POOL = "MiddleTierThreadPool";

	public static String MIDDLETIER_WEB_RESOURCE_ROOT_PATH = "middletier";
	public static String MIDDLETIER_WEB_RESOURCE_GET_PATH = "get";
}
