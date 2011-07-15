package org.springframework.async;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Jon Brisbin <jon@jbrisbin.com>
 */
public abstract class Promise<V> implements Future<V> {

	private final String handlerMutex = "handlers";

	protected LinkedBlockingDeque<CompletionHandler<V>> completionHandlers = new LinkedBlockingDeque<CompletionHandler<V>>();
	protected AtomicReference<Throwable> error = new AtomicReference<Throwable>();
	protected AtomicLong timeout = new AtomicLong(30000L);
	protected V obj;

	public LinkedBlockingDeque<CompletionHandler<V>> getCompletionHandlers() {
		return completionHandlers;
	}

	public void setTimeout(long timeout) {
		this.timeout.set(timeout);
	}

	public Promise<V> addCompletionHandler(CompletionHandler<V> completionHandler) {
		Future<V> future = getFuture();
		if (future.isDone()) {
			try {
				completionHandler.completed(null != obj ? obj : future.get(timeout.get(), TimeUnit.MILLISECONDS));
			} catch (InterruptedException e) {
				completionHandler.failed(e);
			} catch (ExecutionException e) {
				completionHandler.failed(e);
			} catch (TimeoutException e) {
				completionHandler.failed(e);
			}
		} else if (future.isCancelled()) {
			completionHandler.cancelled(true);
		} else if (null != error.get()) {
			completionHandler.failed(error.get());
		} else {
			this.completionHandlers.add(completionHandler);
		}
		return this;
	}

	public void result(V obj) {
		if (!completionHandlers.isEmpty()) {
			List<CompletionHandler<V>> handlers = new ArrayList<CompletionHandler<V>>(completionHandlers.size());
			completionHandlers.drainTo(handlers);
			for (CompletionHandler<V> handler : handlers) {
				handler.completed(obj);
			}
		} else {
			this.obj = obj;
			handleResult(obj);
		}
	}

	protected abstract Future<V> getFuture();

	protected abstract void handleResult(V obj);

	public void failure(Throwable throwable) {
		if (!completionHandlers.isEmpty()) {
			List<CompletionHandler<V>> handlers = new ArrayList<CompletionHandler<V>>(completionHandlers.size());
			completionHandlers.drainTo(handlers);
			for (CompletionHandler<V> handler : handlers) {
				handler.failed(throwable);
			}
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
