package org.springframework.async;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Jon Brisbin <jon@jbrisbin.com>
 */
public abstract class Promise<V> implements Future<V> {

	private final String futureMutex = "future";

	protected AtomicReference<CompletionHandler<V>> completionHandler = new AtomicReference<CompletionHandler<V>>();
	protected AtomicReference<Throwable> error = new AtomicReference<Throwable>();

	public CompletionHandler<V> getCompletionHandler() {
		return completionHandler.get();
	}

	public Promise<V> setCompletionHandler(CompletionHandler<V> completionHandler) throws Exception {
		Future<V> future = getFuture();
		if (future.isDone()) {
			completionHandler.completed(future.get());
		} else if (future.isCancelled()) {
			completionHandler.cancelled(true);
		} else if (null != error.get()) {
			completionHandler.failed(error.get());
		} else {
			this.completionHandler.lazySet(completionHandler);
		}
		return this;
	}

	public void result(V obj) {
		if (null != completionHandler) {
			completionHandler.get().completed(obj);
		} else {
			handleResult(obj);
		}
	}

	protected abstract Future<V> getFuture();

	protected abstract void handleResult(V obj);

	public void failure(Throwable throwable) {
		if (null != completionHandler) {
			completionHandler.get().failed(throwable);
		} else {
			this.error.set(throwable);
			handleFailure(throwable);
		}
	}

	protected abstract void handleFailure(Throwable throwable);

	@Override public boolean cancel(boolean b) {
		return getFuture().cancel(b);
	}

	@Override public boolean isCancelled() {
		return getFuture().isCancelled();
	}

	@Override public boolean isDone() {
		return getFuture().isDone();
	}

	@Override public V get() throws InterruptedException, ExecutionException {
		return getFuture().get();
	}

	@Override public V get(long l, TimeUnit timeUnit) throws InterruptedException, ExecutionException, TimeoutException {
		return getFuture().get(l, timeUnit);
	}

}
