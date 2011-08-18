package org.springframework.async;

import java.util.concurrent.CountDownLatch;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jon Brisbin <jon@jbrisbin.com>
 */
@SuppressWarnings({"unchecked"})
public class PromiseTests {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private CompletionHandler<String, CountDownLatch> handler = new CompletionHandler<String, CountDownLatch>() {
		@Override public void completed(String result, CountDownLatch latch) {
			log.info("Got result: " + result + " with attachment: " + latch);
			latch.countDown();
		}

		@Override public void failed(Throwable t, CountDownLatch latch) {
			log.error(t.getMessage(), t);
			latch.countDown();
		}

	};

	@Test
	public void testPromiseCompletionHandler() throws InterruptedException {
		final CountDownLatch latch = new CountDownLatch(1);

		LongRunningAction action = new LongRunningAction();
		action.executeLongRunningAction(latch).complete(handler);

		latch.await();
	}

	@Test
	public void testPromiseFailure() throws InterruptedException {
		final CountDownLatch latch = new CountDownLatch(1);

		LongRunningAction action = new LongRunningAction();
		action.executeFailedAction(latch).complete(handler);

		latch.await();
	}

}
