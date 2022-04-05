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

package org.springframework.async.http.converter;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.springframework.async.AbstractCompletionHandler;
import org.springframework.async.Promise;
import org.springframework.async.http.HttpInputMessage;
import org.springframework.async.http.HttpOutputMessage;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

/**
 * @author Jon Brisbin <jon@jbrisbin.com>
 */
public class StringHttpMessageConverter extends AbstractHttpMessageConverter<String> {

	public static final Charset DEFAULT_CHARSET = Charset.forName("ISO-8859-1");

	private final List<Charset> availableCharsets;

	private boolean writeAcceptCharset = true;

	public StringHttpMessageConverter() {
		super(new MediaType("text", "plain", DEFAULT_CHARSET), MediaType.ALL);
		this.availableCharsets = new ArrayList<Charset>(Charset.availableCharsets().values());
	}

	/**
	 * Indicates whether the {@code Accept-Charset} should be written to any outgoing request.
	 * <p>Default is {@code true}.
	 */
	public void setWriteAcceptCharset(boolean writeAcceptCharset) {
		this.writeAcceptCharset = writeAcceptCharset;
	}

	@Override
	public boolean supports(Class<?> clazz) {
		return String.class.equals(clazz);
	}

	@Override
	protected void readInternal(Class<? extends String> clazz, final HttpInputMessage outputMessage, final Promise<String> promise) throws IOException, HttpMessageNotReadableException {
		outputMessage.getHeaders().addCompletionHandler(new AbstractCompletionHandler<HttpHeaders>() {
			@Override public void completed(HttpHeaders headers) {
				final Charset charset = getContentTypeCharset(headers.getContentType());
				outputMessage.setCompletionHandler(new AbstractCompletionHandler<Object>() {
					private StringBuffer sb = new StringBuffer();

					@Override public boolean chunk(ByteBuffer buffer) {
						sb.append(charset.decode(buffer).array());
						return true;
					}

					@Override public void completed(Object obj) {
						promise.result(sb.toString());
					}

					@Override public void failed(Throwable throwable) {
						promise.failure(throwable);
					}
				});
			}
		});
	}

	@Override
	protected void writeInternal(String s, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
		if (writeAcceptCharset) {
			outputMessage.getHeaders().setAcceptCharset(getAcceptedCharsets());
		}
		Charset charset = getContentTypeCharset(outputMessage.getHeaders().getContentType());

	}

	/**
	 * Return the list of supported {@link Charset}.
	 * <p/>
	 * <p>By default, returns {@link Charset#availableCharsets()}. Can be overridden in subclasses.
	 *
	 * @return the list of accepted charsets
	 */
	protected List<Charset> getAcceptedCharsets() {
		return this.availableCharsets;
	}

	private Charset getContentTypeCharset(MediaType contentType) {
		if (contentType != null && contentType.getCharSet() != null) {
			return contentType.getCharSet();
		} else {
			return DEFAULT_CHARSET;
		}

	}

}
