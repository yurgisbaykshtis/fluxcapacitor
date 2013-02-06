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

	@BeforeClass
	public static void setUp() throws Exception {
		baseSetUp();
	}

	@Test
	public void test1() throws InterruptedException {
		Response response = new EdgeResource().get();
		Assert.assertEquals(200, response.getStatus());
		Assert.assertEquals("Fraggle Rock!", response.getEntity().toString());
	}

	@AfterClass
	public static void tearDown() throws Exception {
		baseTearDown();
	}
}
