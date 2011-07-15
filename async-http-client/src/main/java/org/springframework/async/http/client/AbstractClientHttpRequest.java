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

package org.springframework.async.http.client;

import java.io.IOException;

import org.springframework.async.CompletionHandler;
import org.springframework.http.HttpHeaders;
import org.springframework.util.Assert;

/**
 * @author Jon Brisbin <jon@jbrisbin.com>
 */
public abstract class AbstractClientHttpRequest implements ClientHttpRequest {

	protected boolean executed = false;

	protected final HttpHeaders headers = new HttpHeaders();
	private CompletionHandler<?> completionHandler;

	public final HttpHeaders getHeaders() {
		return headers;
	}

	@Override public <V> void setCompletionHandler(CompletionHandler<V> completionHandler) {
		this.completionHandler = completionHandler; 
	}

	public final ClientHttpResponse execute() throws IOException {
		checkExecuted();
		ClientHttpResponse result = executeInternal(this.headers);
		this.executed = true;
		return result;
	}

	private void checkExecuted() {
		Assert.state(!this.executed, "ClientHttpRequest already executed");
	}

	/**
	 * Abstract template method that writes the given headers and content to the HTTP request.
	 *
	 * @param headers the HTTP headers
	 * @return the response object for the executed request
	 */
	protected abstract ClientHttpResponse executeInternal(HttpHeaders headers) throws IOException;

}
