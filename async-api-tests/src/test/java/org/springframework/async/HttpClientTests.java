package org.springframework.async;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Test;
import org.springframework.async.http.HttpClient;

/**
 * @author Jon Brisbin <jon@jbrisbin.com>
 */
public class HttpClientTests {

	@Test
	public void testHttpClient() throws URISyntaxException {
		HttpClient client = new HttpClient(new URI("http://localhost:8098/riak/status"));
		client.get();
	}
	
}
