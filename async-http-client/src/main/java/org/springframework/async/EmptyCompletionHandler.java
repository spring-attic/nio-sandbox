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

package org.springframework.async;

import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jon Brisbin <jon@jbrisbin.com>
 */
public class EmptyCompletionHandler<V> implements CompletionHandler<V> {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	@Override public void cancelled(boolean force) {
		if (log.isWarnEnabled()) {
			log.warn("cancelled(): " + force);
		}
	}

	@Override public boolean chunk(ByteBuffer buffer) {
		if (log.isDebugEnabled()) {
			log.debug("chunk(): " + buffer);
		}
		return true;
	}

	@Override public void completed(V obj) {
		if (log.isDebugEnabled()) {
			log.debug("completed(): " + obj);
		}
	}

	@Override public void failed(Throwable throwable) {
		log.error("failed(): " + throwable);
	}

}
