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
package com.fluxcapacitor.edge;

import javax.ws.rs.core.Response;

import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fluxcapacitor.core.test.BaseAppTest;
import com.fluxcapacitor.edge.jersey.resources.EdgeResource;

@Ignore
public class EdgeTest extends BaseAppTest {
	private static final Logger logger = LoggerFactory
			.getLogger(EdgeTest.class);

	private EdgeResource edge = new EdgeResource();
	
	@BeforeClass
	public static void setUp() throws Exception {
		baseSetUp();
	}

	// TODO:  fix this and cleanup properly
	@Test
	@Ignore
	public void test1() throws InterruptedException {
		Response response = edge.addLogWithPost("1234", "Fraggle Rock!");
		Assert.assertEquals(200, response.getStatus());

		response = edge.getLogs("1234");
		Assert.assertEquals(200, response.getStatus());
		Assert.assertEquals("Fraggle Rock!", response.getEntity().toString());
	}

	@AfterClass
	public static void tearDown() throws Exception {
		baseTearDown();
	}
}
