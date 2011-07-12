package org.springframework.async.web.client;

import java.util.concurrent.Future;

/**
 * @author Jon Brisbin <jon@jbrisbin.com>
 */
public interface Promise<V> extends Future<V> {

	void setCompletionHandler(V obj) throws Exception;

}
