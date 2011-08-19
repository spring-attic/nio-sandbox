package org.springframework.async;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Jon Brisbin <jon@jbrisbin.com>
 */
public class Promise<T, A> {

	private final String handlersMutex = "handlers";

	private T result = null;
	private AtomicReference<A> attachment = new AtomicReference<>();
	private List<CompletionHandler<T, A>> handlers = new ArrayList<>();

	public Promise() {
	}

	public Promise(A attachment) {
		this.attachment.set(attachment);
	}

	public A getAttachment() {
		return attachment.get();
	}

	public Promise<T, A> setAttachment(A attachment) {
		this.attachment.set(attachment);
		return this;
	}

	public Promise<T, A> setCompletionHandlers(List<CompletionHandler<T, A>> handlers) {
		synchronized (handlersMutex) {
			handlers.clear();
			for (CompletionHandler<T, A> handler : handlers) {
				if (null == result) {
					this.handlers.add(handler);
				} else {
					handler.completed(result, attachment.get());
				}
			}
		}
		return this;
	}

	@SuppressWarnings({"unchecked"})
	public Promise<T, A> setCompletionHandler(CompletionHandler<T, A>... handlers) {
		for (CompletionHandler<T, A> handler : handlers) {
			if (null == result) {
				synchronized (handlersMutex) {
					this.handlers.add(handler);
				}
			} else {
				handler.completed(result, attachment.get());
			}
		}
		return this;
	}

	public boolean setResult(T result) {
		if (null == this.result) {
			this.result = result;
			synchronized (handlersMutex) {
				for (CompletionHandler<T, A> handler : handlers) {
					handler.completed(result, attachment.get());
				}
			}
			return true;
		} else {
			return false;
		}
	}

	public Promise<T, A> setFailure(Throwable t) {
		for (CompletionHandler<T, A> handler : handlers) {
			synchronized (handlersMutex) {
				handler.failed(t, attachment.get());
			}
		}
		return this;
	}

}
