package org.springframework.async;

import java.util.Timer;
import java.util.TimerTask;

/**
 * @author Jon Brisbin <jon@jbrisbin.com>
 */
public class LongRunningAction {

	private Timer timer = new Timer();

	public <A> Promise<String, A> executeLongRunningAction(A attachment) {
		final Promise<String, A> promise = new Promise<>(attachment);
		timer.schedule(new TimerTask() {
										 @Override public void run() {
											 promise.setResult("Hello World!");
										 }
									 }, 1000);
		return promise;
	}

	public <A> Promise<String, A> executeFailedAction(A attachment) {
		final Promise<String, A> promise = new Promise<>(attachment);
		timer.schedule(new TimerTask() {
										 @Override public void run() {
											 promise.setFailure(new IllegalStateException("Bad doggie!"));
										 }
									 }, 1000);
		return promise;
	}

}
