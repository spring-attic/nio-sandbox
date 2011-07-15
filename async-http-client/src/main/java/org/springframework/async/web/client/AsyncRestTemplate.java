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

package org.springframework.async.web.client;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.async.Promise;
import org.springframework.async.http.client.ClientHttpRequest;
import org.springframework.async.http.client.ClientHttpRequestFactory;
import org.springframework.async.http.client.ClientHttpResponse;
import org.springframework.async.http.client.support.HttpAccessor;
import org.springframework.async.http.converter.HttpMessageConverter;
import org.springframework.async.http.converter.StringHttpMessageConverter;
import org.springframework.async.web.client.grizzly.GrizzlyClientHttpRequestFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriTemplate;
import org.springframework.web.util.UriUtils;

/**
 * @author Jon Brisbin <jon@jbrisbin.com>
 */
public class AsyncRestTemplate extends HttpAccessor implements AsyncRestOperations {

	private static final boolean jaxb2Present = ClassUtils.isPresent("javax.xml.bind.Binder", AsyncRestTemplate.class.getClassLoader());
	private static final boolean jacksonPresent = ClassUtils.isPresent("org.codehaus.jackson.map.ObjectMapper", AsyncRestTemplate.class.getClassLoader())
			&& ClassUtils.isPresent("org.codehaus.jackson.JsonGenerator", AsyncRestTemplate.class.getClassLoader());
	private static final boolean romePresent = ClassUtils.isPresent("com.sun.syndication.feed.WireFeed", AsyncRestTemplate.class.getClassLoader());
	private static final boolean grizzlyPresent = ClassUtils.isPresent("org.glassfish.grizzly.Grizzly", AsyncRestTemplate.class.getClassLoader());

	private final ResponseExtractor<HttpHeaders> headersExtractor = new HeadersExtractor();
	private final Logger log = LoggerFactory.getLogger(getClass());

	private List<HttpMessageConverter<?>> messageConverters = new ArrayList<HttpMessageConverter<?>>();

	private ResponseErrorHandler errorHandler = new DefaultResponseErrorHandler();

	public AsyncRestTemplate() {
//		this.messageConverters.add(new ByteArrayHttpMessageConverter());
		this.messageConverters.add(new StringHttpMessageConverter());
//		this.messageConverters.add(new ResourceHttpMessageConverter());
//		this.messageConverters.add(new SourceHttpMessageConverter());
//		this.messageConverters.add(new XmlAwareFormHttpMessageConverter());
		if (jaxb2Present) {
//			this.messageConverters.add(new Jaxb2RootElementHttpMessageConverter());
		}
		if (jacksonPresent) {
//			this.messageConverters.add(new MappingJacksonHttpMessageConverter());
		}
		if (romePresent) {
//			this.messageConverters.add(new AtomFeedHttpMessageConverter());
//			this.messageConverters.add(new RssChannelHttpMessageConverter());
		}
		if (grizzlyPresent) {
			setRequestFactory(new GrizzlyClientHttpRequestFactory());
		} else {
			// TODO: simple version
		}
	}

	public AsyncRestTemplate(ClientHttpRequestFactory requestFactory) {
		this();
		setRequestFactory(requestFactory);
	}

	/**
	 * Set the message body converters to use. These converters are used to convert from and to HTTP requests and
	 * responses.
	 */
	public AsyncRestTemplate setMessageConverters(List<HttpMessageConverter<?>> messageConverters) {
		Assert.notEmpty(messageConverters, "'messageConverters' must not be empty");
		this.messageConverters = messageConverters;
		return this;
	}

	/**
	 * Returns the message body converters. These converters are used to convert from and to HTTP requests and responses.
	 */
	public List<HttpMessageConverter<?>> getMessageConverters() {
		return this.messageConverters;
	}

	/**
	 * Set the error handler.
	 */
	public AsyncRestTemplate setErrorHandler(ResponseErrorHandler errorHandler) {
		Assert.notNull(errorHandler, "'errorHandler' must not be null");
		this.errorHandler = errorHandler;
		return this;
	}

	/**
	 * Return the error handler. By default, this is the {@link DefaultResponseErrorHandler}.
	 */
	public ResponseErrorHandler getErrorHandler() {
		return this.errorHandler;
	}

	@Override
	public <T> Promise<T> getForObject(String url, Class<T> responseType, Object... uriVariables) throws RestClientException {
		AcceptHeaderRequestCallback requestCallback = new AcceptHeaderRequestCallback(responseType);
		HttpMessageConverterExtractor<T> extractor = new HttpMessageConverterExtractor<T>(responseType, messageConverters);
		return execute(url, HttpMethod.GET, requestCallback, extractor, uriVariables);
	}

	@Override
	public <T> Promise<T> getForObject(String url, Class<T> responseType, Map<String, ?> uriVariables) throws RestClientException {
		return null;
	}

	@Override public <T> Promise<T> getForObject(URI url, Class<T> responseType) throws RestClientException {
		return null;
	}

	@Override
	public <T> Promise<ResponseEntity<T>> getForEntity(String url, Class<T> responseType, Object... uriVariables) throws RestClientException {
		return null;
	}

	@Override
	public <T> Promise<ResponseEntity<T>> getForEntity(String url, Class<T> responseType, Map<String, ?> uriVariables) throws RestClientException {
		return null;
	}

	@Override
	public <T> Promise<ResponseEntity<T>> getForEntity(URI url, Class<T> responseType) throws RestClientException {
		return null;
	}

	@Override public Promise<HttpHeaders> headForHeaders(String url, Object... uriVariables) throws RestClientException {
		return null;
	}

	@Override
	public Promise<HttpHeaders> headForHeaders(String url, Map<String, ?> uriVariables) throws RestClientException {
		return null;
	}

	@Override public Promise<HttpHeaders> headForHeaders(URI url) throws RestClientException {
		return null;
	}

	@Override
	public Promise<URI> postForLocation(String url, Object request, Object... uriVariables) throws RestClientException {
		return null;
	}

	@Override
	public Promise<URI> postForLocation(String url, Object request, Map<String, ?> uriVariables) throws RestClientException {
		return null;
	}

	@Override public Promise<URI> postForLocation(URI url, Object request) throws RestClientException {
		return null;
	}

	@Override
	public <T> Promise<T> postForObject(String url, Object request, Class<T> responseType, Object... uriVariables) throws RestClientException {
		return null;
	}

	@Override
	public <T> Promise<T> postForObject(String url, Object request, Class<T> responseType, Map<String, ?> uriVariables) throws RestClientException {
		return null;
	}

	@Override
	public <T> Promise<T> postForObject(URI url, Object request, Class<T> responseType) throws RestClientException {
		return null;
	}

	@Override
	public <T> Promise<ResponseEntity<T>> postForEntity(String url, Object request, Class<T> responseType, Object... uriVariables) throws RestClientException {
		return null;
	}

	@Override
	public <T> Promise<ResponseEntity<T>> postForEntity(String url, Object request, Class<T> responseType, Map<String, ?> uriVariables) throws RestClientException {
		return null;
	}

	@Override
	public <T> Promise<ResponseEntity<T>> postForEntity(URI url, Object request, Class<T> responseType) throws RestClientException {
		return null;
	}

	@Override public Promise<Void> put(String url, Object request, Object... uriVariables) throws RestClientException {
//		HttpEntityRequestCallback requestCallback = new HttpEntityRequestCallback(request);
		execute(url, HttpMethod.PUT, null, null, uriVariables);
		return null;
	}

	@Override
	public Promise<Void> put(String url, Object request, Map<String, ?> uriVariables) throws RestClientException {
		return null;
	}

	@Override public Promise<Void> put(URI url, Object request) throws RestClientException {
		return null;
	}

	@Override public Promise<Void> delete(String url, Object... uriVariables) throws RestClientException {
		return null;
	}

	@Override public Promise<Void> delete(String url, Map<String, ?> uriVariables) throws RestClientException {
		return null;
	}

	@Override public Promise<Void> delete(URI url) throws RestClientException {
		return null;
	}

	@Override
	public Promise<Set<HttpMethod>> optionsForAllow(String url, Object... uriVariables) throws RestClientException {
		return null;
	}

	@Override
	public Promise<Set<HttpMethod>> optionsForAllow(String url, Map<String, ?> uriVariables) throws RestClientException {
		return null;
	}

	@Override public Promise<Set<HttpMethod>> optionsForAllow(URI url) throws RestClientException {
		return null;
	}

	@Override
	public <T> Promise<ResponseEntity<T>> exchange(String url, HttpMethod method, HttpEntity<?> requestEntity,
																								 Class<T> responseType, Object... uriVariables)
			throws RestClientException {
		return null;
	}

	@Override
	public <T> Promise<ResponseEntity<T>> exchange(String url, HttpMethod method, HttpEntity<?> requestEntity,
																								 Class<T> responseType, Map<String, ?> uriVariables)
			throws RestClientException {
		return null;
	}

	@Override
	public <T> Promise<ResponseEntity<T>> exchange(URI url, HttpMethod method, HttpEntity<?> requestEntity,
																								 Class<T> responseType) throws RestClientException {
		return null;
	}

	@Override
	public <T> Promise<T> execute(String url, HttpMethod method, RequestCallback requestCallback,
																ResponseExtractor<T> responseExtractor, Object... uriVariables)
			throws RestClientException {
		UriTemplate uriTemplate = new HttpUrlTemplate(url);
		URI expanded = uriTemplate.expand(uriVariables);
		return doExecute(expanded, method, requestCallback, responseExtractor);
	}

	@Override
	public <T> Promise<T> execute(String url, HttpMethod method, RequestCallback requestCallback,
																ResponseExtractor<T> responseExtractor, Map<String, ?> uriVariables)
			throws RestClientException {
		return null;
	}

	@Override
	public <T> Promise<T> execute(URI url, HttpMethod method, RequestCallback requestCallback,
																ResponseExtractor<T> responseExtractor) throws RestClientException {
		return null;
	}

	/**
	 * Execute the given method on the provided URI. The {@link ClientHttpRequest} is processed using the {@link
	 * RequestCallback}; the response with the {@link ResponseExtractor}.
	 *
	 * @param url							 the fully-expanded URL to connect to
	 * @param method						the HTTP method to execute (GET, POST, etc.)
	 * @param requestCallback	 object that prepares the request (can be <code>null</code>)
	 * @param responseExtractor object that extracts the return value from the response (can be <code>null</code>)
	 * @return an arbitrary object, as returned by the {@link ResponseExtractor}
	 */
	protected <T> Promise<T> doExecute(URI url, HttpMethod method, RequestCallback requestCallback,
																		 ResponseExtractor<T> responseExtractor) throws RestClientException {

		ClientHttpResponse response = null;
		try {
			ClientHttpRequest request = createRequest(url, method);
			if (requestCallback != null) {
				requestCallback.doWithRequest(request);
			}
			response = request.execute();
			if (!getErrorHandler().hasError(response)) {
				logResponseStatus(method, url, response);
			} else {
				handleResponseError(method, url, response);
			}
			if (responseExtractor != null) {
				return responseExtractor.extractData(response);
			} else {
				return null;
			}
		} catch (IOException e) {
			throw new RestClientException(e.getMessage(), e);
		}
	}

	private void logResponseStatus(HttpMethod method, URI url, ClientHttpResponse response) {
		if (log.isDebugEnabled()) {
			try {
				log.debug(
						method.name() + " request for \"" + url + "\" resulted in " + response.getStatusCode() + " (" +
								response.getStatusText() + ")");
			} catch (IOException e) {
				// ignore
			}
		}
	}

	private void handleResponseError(HttpMethod method, URI url, ClientHttpResponse response) throws IOException {
		if (log.isWarnEnabled()) {
			try {
				log.warn(
						method.name() + " request for \"" + url + "\" resulted in " + response.getStatusCode() + " (" +
								response.getStatusText() + "); invoking error handler");
			} catch (IOException e) {
				// ignore
			}
		}
		getErrorHandler().handleError(response);
	}

	/**
	 * Request callback implementation that prepares the request's accept headers.
	 */
	private class AcceptHeaderRequestCallback implements RequestCallback {

		private final Class<?> responseType;

		private AcceptHeaderRequestCallback(Class<?> responseType) {
			this.responseType = responseType;
		}

		@SuppressWarnings("unchecked")
		public void doWithRequest(ClientHttpRequest request) throws IOException {
			if (responseType != null) {
				final List<MediaType> allSupportedMediaTypes = new ArrayList<MediaType>();
				for (HttpMessageConverter<?> messageConverter : getMessageConverters()) {
					if (messageConverter.canRead(responseType, null)) {
						List<MediaType> supportedMediaTypes = messageConverter.getSupportedMediaTypes();
						for (MediaType supportedMediaType : supportedMediaTypes) {
							if (supportedMediaType.getCharSet() != null) {
								supportedMediaType =
										new MediaType(supportedMediaType.getType(), supportedMediaType.getSubtype());
							}
							allSupportedMediaTypes.add(supportedMediaType);
						}
					}
				}
				if (!allSupportedMediaTypes.isEmpty()) {
					MediaType.sortBySpecificity(allSupportedMediaTypes);
					if (log.isDebugEnabled()) {
						log.debug("Setting request Accept header to " + allSupportedMediaTypes);
					}
					request.getHeaders().setAccept(allSupportedMediaTypes);
				}
			}
		}
	}

	/**
	 * Request callback implementation that writes the given object to the request stream.
	 */
	private class HttpEntityRequestCallback extends AcceptHeaderRequestCallback {

		private final HttpEntity requestEntity;

		private HttpEntityRequestCallback(Object requestBody) {
			this(requestBody, null);
		}

		@SuppressWarnings("unchecked")
		private HttpEntityRequestCallback(Object requestBody, Class<?> responseType) {
			super(responseType);
			if (requestBody instanceof HttpEntity) {
				this.requestEntity = (HttpEntity) requestBody;
			} else if (requestBody != null) {
				this.requestEntity = new HttpEntity(requestBody);
			} else {
				this.requestEntity = HttpEntity.EMPTY;
			}
		}

		@Override
		@SuppressWarnings("unchecked")
		public void doWithRequest(ClientHttpRequest httpRequest) throws IOException {
			super.doWithRequest(httpRequest);
			if (!requestEntity.hasBody()) {
				HttpHeaders headers = httpRequest.getHeaders();
				HttpHeaders requestHeaders = requestEntity.getHeaders();
				if (!requestHeaders.isEmpty()) {
					headers.putAll(requestHeaders);
				}
				if (headers.getContentLength() == -1) {
					headers.setContentLength(0L);
				}
			} else {
				Object requestBody = requestEntity.getBody();
				Class<?> requestType = requestBody.getClass();
				HttpHeaders requestHeaders = requestEntity.getHeaders();
				MediaType requestContentType = requestHeaders.getContentType();
				for (HttpMessageConverter messageConverter : getMessageConverters()) {
					if (messageConverter.canWrite(requestType, requestContentType)) {
						if (!requestHeaders.isEmpty()) {
							httpRequest.getHeaders().putAll(requestHeaders);
						}
						if (log.isDebugEnabled()) {
							if (requestContentType != null) {
								log.debug("Writing [" + requestBody + "] as \"" + requestContentType +
										"\" using [" + messageConverter + "]");
							} else {
								log.debug("Writing [" + requestBody + "] using [" + messageConverter + "]");
							}

						}
						messageConverter.write(requestBody, requestContentType, httpRequest);
						return;
					}
				}
				String message = "Could not write request: no suitable HttpMessageConverter found for request type [" +
						requestType.getName() + "]";
				if (requestContentType != null) {
					message += " and content type [" + requestContentType + "]";
				}
				throw new RestClientException(message);
			}
		}
	}

	/**
	 * Response extractor that extracts the response {@link HttpHeaders}.
	 */
	private static class HeadersExtractor implements ResponseExtractor<HttpHeaders> {

		public Promise<HttpHeaders> extractData(ClientHttpResponse response) throws IOException {
			return response.getHeaders();
		}
	}

	/**
	 * HTTP-specific subclass of UriTemplate, overriding the encode method.
	 */
	private static class HttpUrlTemplate extends UriTemplate {

		public HttpUrlTemplate(String uriTemplate) {
			super(uriTemplate);
		}

		@Override
		protected URI encodeUri(String uri) {
			try {
				String encoded = UriUtils.encodeHttpUrl(uri, "UTF-8");
				return new URI(encoded);
			} catch (UnsupportedEncodingException ex) {
				// should not happen, UTF-8 is always supported
				throw new IllegalStateException(ex);
			} catch (URISyntaxException ex) {
				throw new IllegalArgumentException("Could not create HTTP URL from [" + uri + "]: " + ex, ex);
			}
		}
	}

}
