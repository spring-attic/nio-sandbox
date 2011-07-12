package org.springframework.async;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author Jon Brisbin <jon@jbrisbin.com>
 */
public abstract class Promise<V> implements Future<V> {

	private final String futureMutex = "future";

	private Future<V> future;
	private CompletionHandler<V> completionHandler;

	public Promise(Future<V> future) {
		this.future = future;
	}

	public CompletionHandler<V> getCompletionHandler() {
		return completionHandler;
	}

	public Promise<V> setCompletionHandler(CompletionHandler<V> completionHandler) throws Exception {
		synchronized (futureMutex) {
			if (future.isDone()) {
				completionHandler.completed(future.get());
			} else {
				this.completionHandler = completionHandler;
			}
		}
		return this;
	}

	public Future<V> getFuture() {
		return future;
	}

	public void result(V obj) {
		if (null != completionHandler) {
			completionHandler.completed(obj);
		} else {
			handleResult(obj);
		}
	}

	protected abstract void handleResult(V obj);

	public void failure(Throwable throwable) {
		if (null != completionHandler) {
			completionHandler.failed(throwable);
		} else {
			handleFailure(throwable);
		}
	}

	protected abstract void handleFailure(Throwable throwable);

	@Override public boolean cancel(boolean b) {
		return future.cancel(b);
	}

	@Override public boolean isCancelled() {
		return future.isCancelled();
	}

	@Override public boolean isDone() {
		return future.isDone();
	}

	@Override public V get() throws InterruptedException, ExecutionException {
		return future.get();
	}

	@Override public V get(long l, TimeUnit timeUnit) throws InterruptedException, ExecutionException, TimeoutException {
		return future.get(l, timeUnit);
	}

}
