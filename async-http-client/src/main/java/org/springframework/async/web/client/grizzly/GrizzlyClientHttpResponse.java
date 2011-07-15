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
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;

import org.glassfish.grizzly.Buffer;
import org.glassfish.grizzly.http.HttpContent;
import org.glassfish.grizzly.http.HttpHeader;
import org.glassfish.grizzly.http.HttpResponsePacket;
import org.glassfish.grizzly.http.util.DataChunk;
import org.glassfish.grizzly.http.util.MimeHeaders;
import org.glassfish.grizzly.memory.HeapMemoryManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.async.AbstractCompletionHandler;
import org.springframework.async.CompletionHandler;
import org.springframework.async.Promise;
import org.springframework.async.http.client.ClientHttpResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

/**
 * @author Jon Brisbin <jon@jbrisbin.com>
 */
@SuppressWarnings({"unchecked"})
public class GrizzlyClientHttpResponse implements ClientHttpResponse {

	private final Logger log = LoggerFactory.getLogger(getClass());
	private final HeapMemoryManager heap = new HeapMemoryManager();
	private final String readMutex = "read";

	private Promise<HttpHeaders> headers = new GrizzlyFuturePromise<HttpHeaders>();
	private HttpHeader responseHeader;
	private Promise<HttpStatus> status = new GrizzlyFuturePromise<HttpStatus>();
	private Promise<String> statusText = new GrizzlyFuturePromise<String>();
	private LinkedBlockingDeque<Buffer> buffers = new LinkedBlockingDeque<Buffer>();
	private CompletionHandler<?> completionHandler;
	private ReadableByteChannel readChannel = new ReadableByteChannel() {
		@Override public int read(ByteBuffer buffer) throws IOException {
			Buffer b = buffers.peek();
			if (null == b) {
				return 0;
			} else {
				synchronized (readMutex) {
					int start = buffer.position();
					b = buffers.pop();
					b.get(buffer);
					return buffer.position() - start;
				}
			}
		}

		@Override public boolean isOpen() {
			return true;
		}

		@Override public void close() throws IOException {

		}
	};

	public void addContent(HttpContent httpContent) {
		if (!headers.isDone()) {
			responseHeader = httpContent.getHttpHeader();
			org.glassfish.grizzly.http.util.HttpStatus responseStatus = ((HttpResponsePacket) responseHeader).getHttpStatus();
			status.result(HttpStatus.valueOf(responseStatus.getStatusCode()));
			statusText.result(new String(responseStatus.getReasonPhraseBytes()));

			HttpHeaders httpHeaders = new HttpHeaders();
			MimeHeaders hdrs = httpContent.getHttpHeader().getHeaders();
			for (String name : hdrs.names()) {
				List<String> values = new ArrayList<String>();
				DataChunk chunk = hdrs.getValue(name);
				String val = null;
				switch (chunk.getType()) {
					case String:
						val = chunk.toString(Charset.defaultCharset());
						break;
					case Buffer:
						val = chunk.getBufferChunk().toString(Charset.defaultCharset());
						break;
					default:
						log.info("type=" + chunk.getType());
						log.info("chunk=" + chunk);
				}
				if (null != val) {
					values.add(val);
				}

				if ("Content-Type".equalsIgnoreCase(name)) {
					httpHeaders.setContentType(MediaType.parseMediaType(val));
				} else {
					httpHeaders.put(name, values);
				}
			}
			headers.result(httpHeaders);
		}

		Buffer contentBuffer = httpContent.getContent();
		if (null != contentBuffer && contentBuffer.remaining() > 0) {
			synchronized (readMutex) {
				if (null != completionHandler) {
					completionHandler.chunk(contentBuffer.toByteBuffer());
				} else {
					buffers.push(contentBuffer);
				}
			}
		}
	}

	@Override public <V> void setCompletionHandler(CompletionHandler<V> completionHandler) {
		this.completionHandler = completionHandler;
		if (!buffers.isEmpty()) {
			List<Buffer> bufferList = new ArrayList<Buffer>();
			buffers.drainTo(bufferList);
			for (Buffer b : bufferList) {
				completionHandler.chunk(b.toByteBuffer());
			}
		}
	}

	@Override public Promise<HttpStatus> getStatusCode() throws IOException {
		return status;
	}

	@Override public Promise<String> getStatusText() throws IOException {
		return statusText;
	}

	@Override public void close() {
	}

	@Override public Promise<HttpHeaders> getHeaders() {
		return headers;
	}

	private class WriteToChannelCompletionHandler extends AbstractCompletionHandler {
		@Override public boolean chunk(ByteBuffer buffer) {
			return false;
		}
	}

}
