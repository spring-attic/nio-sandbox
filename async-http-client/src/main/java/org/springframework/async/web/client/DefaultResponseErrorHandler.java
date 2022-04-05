/*
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.async.web.client;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.async.AbstractCompletionHandler;
import org.springframework.async.http.client.ClientHttpResponse;
import org.springframework.http.HttpHeaders;

/**
 * @author Jon Brisbin <jon@jbrisbin.com>
 */
public class DefaultResponseErrorHandler implements ResponseErrorHandler {

	private final Logger log = LoggerFactory.getLogger(getClass());

	@Override public boolean hasError(ClientHttpResponse response) throws IOException {
//		response.getHeaders().addCompletionHandler(new AbstractCompletionHandler<HttpHeaders>(){
//			@Override public void completed(HttpHeaders headers) {
//
//			}
//		});
		return false;
	}

	@Override public void handleError(ClientHttpResponse response) throws IOException {
		try {
			log.error(response.getStatusText().get());
		} catch (InterruptedException e) {
			e.printStackTrace();	//To change body of catch statement use File | Settings | File Templates.
		} catch (ExecutionException e) {
			e.printStackTrace();	//To change body of catch statement use File | Settings | File Templates.
		}
	}

}
