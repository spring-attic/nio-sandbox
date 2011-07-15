/*
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.async.web.client.grizzly;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import org.glassfish.grizzly.Connection;
import org.glassfish.grizzly.EmptyCompletionHandler;
import org.glassfish.grizzly.attributes.Attribute;
import org.glassfish.grizzly.attributes.DefaultAttributeBuilder;
import org.glassfish.grizzly.filterchain.BaseFilter;
import org.glassfish.grizzly.filterchain.FilterChain;
import org.glassfish.grizzly.filterchain.FilterChainBuilder;
import org.glassfish.grizzly.filterchain.FilterChainContext;
import org.glassfish.grizzly.filterchain.NextAction;
import org.glassfish.grizzly.filterchain.TransportFilter;
import org.glassfish.grizzly.http.HttpClientFilter;
import org.glassfish.grizzly.http.HttpContent;
import org.glassfish.grizzly.http.HttpRequestPacket;
import org.glassfish.grizzly.http.Method;
import org.glassfish.grizzly.http.Protocol;
import org.glassfish.grizzly.nio.transport.TCPNIOTransport;
import org.glassfish.grizzly.nio.transport.TCPNIOTransportBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.async.CompletionHandler;
import org.springframework.async.http.client.AbstractClientHttpRequest;
import org.springframework.async.http.client.ClientHttpResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

/**
 * @author Jon Brisbin <jon@jbrisbin.com>
 */
public class GrizzlyClientHttpRequest extends AbstractClientHttpRequest {

	private final static Attribute<GrizzlyClientHttpResponse> RESPONSE = DefaultAttributeBuilder.DEFAULT_ATTRIBUTE_BUILDER.createAttribute("pending-response");

	private final Logger log = LoggerFactory.getLogger(getClass());

	private FilterChain chain;
	private TCPNIOTransport transport;
	private Connection connection;
	private HttpMethod method;
	private URI uri;
	private GrizzlyClientHttpResponse response = new GrizzlyClientHttpResponse();
	private LinkedBlockingQueue<GrizzlyClientHttpRequest> requests = new LinkedBlockingQueue<GrizzlyClientHttpRequest>();

	public GrizzlyClientHttpRequest(URI uri, HttpMethod method) throws IOException {
		this.uri = uri;
		this.method = method;

		FilterChainBuilder filterChainBuilder = FilterChainBuilder.stateless();
		filterChainBuilder.add(new TransportFilter());
		filterChainBuilder.add(new HttpClientFilter());
		filterChainBuilder.add(new RequestResponseFilter());
		chain = filterChainBuilder.build();

		TCPNIOTransportBuilder transportBuilder = TCPNIOTransportBuilder.newInstance();
		transportBuilder.setKeepAlive(true);
		transportBuilder.setTcpNoDelay(true);
		transportBuilder.setProcessor(chain);

		transport = transportBuilder.build();
		transport.start();

		transport.connect(new InetSocketAddress(uri.getHost(), uri.getPort()), new EmptyCompletionHandler<Connection>() {
			@SuppressWarnings({"unchecked"})
			@Override public void completed(Connection result) {
				connection = result;
				List<GrizzlyClientHttpRequest> pendingRequests = new ArrayList<GrizzlyClientHttpRequest>();
				requests.drainTo(pendingRequests);
				for (GrizzlyClientHttpRequest request : pendingRequests) {
					try {
						connection.write(request);
					} catch (IOException e) {
						log.error(e.getMessage(), e);
					}
				}
			}
		});

	}

	@SuppressWarnings({"unchecked"})
	@Override protected ClientHttpResponse executeInternal(HttpHeaders headers) throws IOException {
		if (null == connection) {
			requests.add(this);
		} else {
			connection.write(this);
		}
		return response;
	}

	@Override public HttpMethod getMethod() {
		return method;
	}

	@Override public URI getURI() {
		return uri;
	}

	private class RequestResponseFilter extends BaseFilter {
		@Override public NextAction handleRead(FilterChainContext ctx) throws IOException {
			HttpContent httpContent = ctx.getMessage();
			response.addContent(httpContent);

			log.debug("handleRead(): " + ctx);
			return super.handleRead(ctx);
		}

		@Override public NextAction handleWrite(FilterChainContext ctx) throws IOException {
			HttpRequestPacket.Builder requestBuilder = HttpRequestPacket.builder()
					.protocol(Protocol.HTTP_1_1)
					.uri(uri.toASCIIString());
			switch (method) {
				case GET:
					requestBuilder.method(Method.GET);
					break;
				case PUT:
					requestBuilder.method(Method.PUT);
					break;
				case POST:
					requestBuilder.method(Method.POST);
					break;
				case DELETE:
					requestBuilder.method(Method.DELETE);
					break;
			}
			
			ctx.write(requestBuilder.build());

			log.debug("handleWrite(): " + ctx);
			return ctx.getStopAction();
		}
	}
}
