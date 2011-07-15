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

package org.springframework.async.web.client.test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Before;
import org.junit.Test;
import org.springframework.async.Promise;
import org.springframework.async.web.client.AsyncRestTemplate;

/**
 * @author Jon Brisbin <jon@jbrisbin.com>
 */
public class AsyncRestTemplateTests {

	static final int TIMEOUT = 30;

	AsyncRestTemplate restTemplate;

	@Before
	public void setup() {
		restTemplate = new AsyncRestTemplate();
	}

	@Test
	public void testGetForObject() throws ExecutionException, TimeoutException, InterruptedException {
		Promise<String> text = restTemplate.getForObject("http://localhost:8098/riak/status", String.class);
		String s = text.get(TIMEOUT, TimeUnit.SECONDS);

		assert null != s;
	}

}
