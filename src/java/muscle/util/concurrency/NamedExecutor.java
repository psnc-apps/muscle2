/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package muscle.util.concurrency;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 *
 * @author Joris Borgdorff
 */
public class NamedExecutor {
	private final ExecutorService executor;
	private final static Logger logger = Logger.getLogger(NamedExecutor.class.getName());
	
	public NamedExecutor() {
		executor = Executors.newCachedThreadPool();
	}
	
	public <T> Future<T> submit(NamedCallable<T> job) {
		return executor.submit(new NamingCallable<T>(job));
	}
	
	private static class NamingCallable<T> implements Callable<T> {
		private final NamedCallable<T> job;
		NamingCallable(NamedCallable<T> job) {
			this.job = job;
		}
		@Override
		public T call() throws Exception {
			String newName = job.getName();
			
			if (newName == null) {
				return job.call();
			}
			
			Thread currentThread = Thread.currentThread();
			String oldName = currentThread.getName();
			currentThread.setName(newName);
			
			try {
				return job.call();
			} finally {
				currentThread.setName(oldName);
			}
		}
	}
	
	public void shutdown() {
		executor.shutdown();
		try {
			// Wait a while for existing tasks to terminate
			if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
				executor.shutdownNow(); // Cancel currently executing tasks
				// Wait a while for tasks to respond to being cancelled
				if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
					logger.severe("Thread did not terminate");
				}
			}
		} catch (InterruptedException ie) {
			// (Re-)Cancel if current thread also interrupted
			executor.shutdownNow();
			// Preserve interrupt status
			Thread.currentThread().interrupt();
		}
	}
	
	public boolean isShutdown() {
		return executor.isShutdown();
	}
}
