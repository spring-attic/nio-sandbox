package org.springframework.async;

/**
 * @author Jon Brisbin <jon@jbrisbin.com>
 */
public interface CompletionHandler<V> {

	void cancelled(boolean force);

	void completed(V obj);

	void failed(Throwable throwable);

}
