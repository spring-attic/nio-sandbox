package org.springframework.async;

import java.nio.ByteBuffer;

/**
 * @author Jon Brisbin <jon@jbrisbin.com>
 */
public interface CompletionHandler<V> {

	/**
	 * Called when the request has been cancelled.
	 *
	 * @param force
	 */
	void cancelled(boolean force);

	/**
	 * Process a chunk of data.
	 *
	 * @param buffer
	 * @return true to call the completed method when finished, false to simply let this method handle the data
	 */
	boolean chunk(ByteBuffer buffer);

	/**
	 * Called when the process has completed and a fully-constituted object is ready to be used.
	 *
	 * @param obj
	 */
	void completed(V obj);

	/**
	 * Called when an error occurs.
	 *
	 * @param throwable
	 */
	void failed(Throwable throwable);

}
