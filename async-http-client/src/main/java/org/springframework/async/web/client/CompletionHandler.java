package org.springframework.async.web.client;

/**
 * @author Jon Brisbin <jon@jbrisbin.com>
 */
public interface CompletionHandler<V> {

	void cancelled(boolean force);

	void complete(V obj);

	void failure(Throwable throwable);

}
