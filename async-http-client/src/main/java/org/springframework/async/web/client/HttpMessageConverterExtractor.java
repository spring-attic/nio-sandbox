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
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.async.AbstractCompletionHandler;
import org.springframework.async.Promise;
import org.springframework.async.PromiseFactory;
import org.springframework.async.http.client.ClientHttpResponse;
import org.springframework.async.http.converter.HttpMessageConverter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClientException;

/**
 * @author Jon Brisbin <jon@jbrisbin.com>
 */
@SuppressWarnings({"unchecked"})
public class HttpMessageConverterExtractor<T> implements ResponseExtractor<T> {

	private final Logger log = LoggerFactory.getLogger(getClass());
	private final Class<T> responseType;
	private final List<HttpMessageConverter<?>> messageConverters;

	public HttpMessageConverterExtractor(Class<T> responseType, List<HttpMessageConverter<?>> messageConverters) {
		this.responseType = responseType;
		this.messageConverters = messageConverters;
	}

	@Override public Promise<T> extractData(final ClientHttpResponse response) throws IOException {
		final Promise<T> promise = PromiseFactory.createPromise();
		response.getHeaders().addCompletionHandler(new AbstractCompletionHandler<HttpHeaders>() {
			@Override public void completed(HttpHeaders headers) {
				MediaType contentType = headers.getContentType();
				if (contentType == null) {
					throw new RestClientException("Cannot extract response: no Content-Type found");
				}
				for (HttpMessageConverter messageConverter : messageConverters) {
					if (messageConverter.canRead(responseType, contentType)) {
						if (log.isDebugEnabled()) {
							log.debug("Reading [" + responseType.getName() + "] as \"" + contentType
									+ "\" using [" + messageConverter + "]");
						}
						try {
							messageConverter.read(responseType, response, promise);
						} catch (IOException e) {
							promise.failure(e);
						}
					}
				}
			}
		});

		return promise;
	}

}
