package org.springframework.async;

/**
 * @author Jon Brisbin <jon@jbrisbin.com>
 */
public interface CompletionHandler<T, A> {

	void completed(T result, A attachment);

	void failed(Throwable t, A attachment);

}
