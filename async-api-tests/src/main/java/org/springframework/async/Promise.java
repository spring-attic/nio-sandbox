package org.springframework.async;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Jon Brisbin <jon@jbrisbin.com>
 */
public class Promise<T, A> {

	private Deque<T> resultQueue = new ArrayDeque<>(1);
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

	public Promise<T, A> complete(List<CompletionHandler<T, A>> handlers) {
		for (CompletionHandler<T, A> handler : handlers) {
			if (resultQueue.isEmpty()) {
				synchronized (handlers) {
					this.handlers.add(handler);
				}
			} else {
				handler.completed(resultQueue.peek(), attachment.get());
			}
		}
		return this;
	}

	@SuppressWarnings({"unchecked"})
	public Promise<T, A> complete(CompletionHandler<T, A>... handlers) {
		for (CompletionHandler<T, A> handler : handlers) {
			if (resultQueue.isEmpty()) {
				synchronized (handlers) {
					this.handlers.add(handler);
				}
			} else {
				handler.completed(resultQueue.peek(), attachment.get());
			}
		}
		return this;
	}

	public boolean setResult(T result) {
		if (resultQueue.size() == 0) {
			resultQueue.push(result);
			for (CompletionHandler<T, A> handler : handlers) {
				synchronized (handlers) {
					handler.completed(result, attachment.get());
				}
			}
			return true;
		} else {
			return false;
		}
	}

	public void setFailure(Throwable t) {
		for (CompletionHandler<T, A> handler : handlers) {
			synchronized (handlers) {
				handler.failed(t, attachment.get());
			}
		}
	}

}
