package com.fluxcapacitor.edge;

import javax.ws.rs.core.MediaType;

import junit.framework.Assert;

import org.apache.commons.io.IOUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.fluxcapacitor.core.test.BaseAppTest;
import com.fluxcapacitor.edge.test.utils.EmbeddedEdgeForTests;
import com.fluxcapacitor.middletier.test.utils.EmbeddedMiddleTierForTests;
import com.google.common.base.Charsets;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;

/**
 * This is for integration testing
 */
@Ignore
public class EdgeIntegrationTest extends BaseAppTest {
	private static Client client;
	private static WebResource webResource;

	private static EmbeddedMiddleTierForTests middleTier;
	private static EmbeddedEdgeForTests edge;

	@BeforeClass
	public static void setup() throws Exception {
		baseSetUp();

		// Setup middle tier
		middleTier = new EmbeddedMiddleTierForTests();
		middleTier.setUp();

		// Setup edge
		edge = new EmbeddedEdgeForTests();
		edge.setUp();

		ClientConfig clientConfig = new DefaultClientConfig();
		client = Client.create(clientConfig);
		// TODO: clean this up
		webResource = client.resource("http://"
				+ edge.edgeServer.host + ":"
				+ edge.edgeServer.port);
	}

	@Test
	public void test1() throws Exception {
		ClientResponse response = get();
		Assert.assertEquals(200, response.getStatus());
		Assert.assertEquals("Fraggle Rock!", IOUtils.toString(
				response.getEntityInputStream(), Charsets.UTF_8));
	}

	@AfterClass
	public static void tearDown() throws Exception {
		if (edge != null) {
			edge.tearDown();
		}

		if (middleTier != null) {
			middleTier.tearDown();
		}

		baseTearDown();
	}

	private ClientResponse get() {
		Builder execute = webResource.path("edge").path("get")
				.type(MediaType.TEXT_PLAIN).accept(MediaType.TEXT_PLAIN);
		return execute.get(ClientResponse.class);
	}
}
