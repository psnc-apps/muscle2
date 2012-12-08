/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package muscle.util.concurrency;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import muscle.client.LocalManager;

/**
 * A thread pool that is principle bounded to a fixed number of threads, but will
 * create more threads if it detects there might be a deadlock.
 * The threshold for a deadlock is set to 50 ms of inactivity with a non-empty queue.
 * Threads are destroyed if there is no work for them for over 2 seconds.
 * @author Joris Borgdorff
 */
public class LimitedThreadPool extends SafeThread {
	private final int limit;
	private final static long TIMEOUT_THREAD = 2;
	private final static long TIMEOUT_NEXTGET = 100;
	private int numberOfRunners;
	private int numberOfWaiting;
	private LinkedBlockingQueue<TaskFuture> queue;
	private final TaskFuture EMPTY = new TaskFuture(null);
	private long lastGet;
	private final Object counterLock = new Object();

	public LimitedThreadPool(int limit) {
		super("LimitedThreadPool");
		this.limit = limit;
		this.numberOfRunners = 0;
		this.numberOfWaiting = 0;
		this.lastGet = Long.MAX_VALUE;
		queue = new LinkedBlockingQueue<TaskFuture>();
	}
	
	public <T> Future<T> submit(NamedCallable<T> task) {
		this.tryInactiveRunner();
		TaskFuture<T> fut = new TaskFuture<T>(task);
		queue.add(fut);
		return fut;
	}

	private TaskFuture getNextTask() throws InterruptedException {
		synchronized (counterLock) {
			this.numberOfWaiting++;
		}
		try {
			return queue.poll(TIMEOUT_THREAD, TimeUnit.SECONDS);
		} finally {
			synchronized (counterLock) {
				this.lastGet = System.currentTimeMillis();
				this.numberOfWaiting--;
			}
		}
	}
	
	private void tryInactiveRunner() {
		if (this.isDisposed()) {
			throw new RejectedExecutionException("Executor was halted");
		}
		synchronized (counterLock) {
			if (this.numberOfWaiting > 0 || this.numberOfRunners >= this.limit) {
				return;
			}
		}
		this.createRunner();
		System.out.print("#");
	}
	
	private void createRunner() {
		new TaskRunner().start();
		
		boolean offeredEmpty = false;
		synchronized (counterLock) {
			this.numberOfRunners++;
			if (this.numberOfRunners > this.limit) {
				this.queue.add(EMPTY);
				offeredEmpty = true;
			}
		}

		if (isDisposed() && !offeredEmpty) {
			this.queue.add(EMPTY);
		}
		
	}
		
	private void runnerIsDisposed() {
		synchronized (counterLock) {
			this.numberOfRunners--;
		}
		System.out.print("-");
	}

	@Override
	public void dispose() {
		synchronized (this) {
			if (isDisposed()) {
				return;
			}
			super.dispose();
		}
		this.queue.clear();
		
		synchronized (counterLock) {
			for (int i = 0; i < this.numberOfRunners; i++) {
				this.queue.add(EMPTY);
			}
		}
	}

	@Override
	protected void handleInterruption(InterruptedException ex) {
		// Do nothing
	}

	@Override
	protected void handleException(Throwable ex) {
		LocalManager.getInstance().fatalException(ex);
	}

	@Override
	protected synchronized boolean continueComputation() throws InterruptedException {
		if (!isDisposed()) {
			wait(TIMEOUT_NEXTGET+1);
		}
		return !isDisposed();
	}
	
	@Override
	protected void execute() {
		long diffTime = System.currentTimeMillis();
		synchronized (counterLock) {		
			diffTime -= lastGet;
		}
		if (!this.queue.isEmpty() && diffTime > TIMEOUT_NEXTGET) {
			this.createRunner();
			System.out.print("+");
		}
	}
	
	private class TaskRunner extends SafeThread {
		private TaskFuture taskFuture;
		TaskRunner() {
			super("threadpool-taskrunner");
			this.taskFuture = null;
		}
		
		@Override
		protected void handleInterruption(InterruptedException ex) {
			this.handleException(ex);
		}

		@Override
		protected synchronized void handleException(Throwable ex) {
			if (taskFuture != null) {
				taskFuture.setException(ex);
			}
		}

		@Override
		protected boolean continueComputation() throws InterruptedException {
			taskFuture = getNextTask();
			return taskFuture != null && taskFuture.task != null;
		}
		
		@Override
		public synchronized void dispose() {
			runnerIsDisposed();
			super.dispose();
		}
		
		@Override
		protected void execute() throws Exception {
			this.setName("threadpool-taskrunner-" + taskFuture.task.getName());
			taskFuture.calculateResult();
			taskFuture = null;
		}
	}
	
	private class TaskFuture<T> implements Future<T> {
		private final BlockingQueue<T> resultQueue;
		private T result;
		private boolean resultIsSet;
		private ExecutionException except;
		private Thread runningThread;
		private Thread waitingThread;
		private boolean cancelled;
		private final NamedCallable<T> task;
		
		TaskFuture(NamedCallable<T> task) {
			this.resultQueue = new ArrayBlockingQueue<T>(1);
			this.result = null;
			this.resultIsSet = false;
			this.except = null;
			this.runningThread = null;
			this.waitingThread = null;
			this.cancelled = false;
			this.task = task;
		}
		
		void calculateResult() throws Exception {
			synchronized (this) {
				this.runningThread = Thread.currentThread();
			}
			T res = this.task.call();
				
			synchronized (this) {
				this.runningThread = null;
				this.result = res;
				this.resultIsSet = true;
				resultQueue.offer(res);
			}
		}
		
		synchronized void setException(Throwable ex) {
			this.runningThread = null;
			if (this.except == null) {
				this.except = new ExecutionException(ex);
			}
			if (waitingThread != null) {
				waitingThread.interrupt();
			}
		}
		
		@Override
		public synchronized boolean cancel(boolean mayInterruptIfRunning) {
			this.cancelled = true;
			if(this.resultIsSet) {
				return false;
			}
			setException(new CancellationException("Task was cancelled"));
			if (mayInterruptIfRunning) {
				runningThread.interrupt();
			}
			return true;
		}

		@Override
		public synchronized boolean isCancelled() {
			return cancelled;
		}

		@Override
		public synchronized boolean isDone() {
			return this.resultIsSet;
		}

		private T getAll(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
			synchronized (this) {
				if (this.except != null) {
					ExecutionException thrExcept = this.except;
					this.except = null;
					throw thrExcept;
				} else if (this.resultIsSet) {
					return result;
				}
				this.waitingThread = Thread.currentThread();
			}
			try {
				T res;
				if (unit == null) {
					res = resultQueue.take();
				} else {
					res = resultQueue.poll(timeout, unit);
				}
				synchronized (this) {
					waitingThread = null;
				}
				if (res == null) {
					throw new TimeoutException("Getting value timed out");
				}
			} catch (InterruptedException ex) {
				synchronized (this) {
					this.waitingThread = null;
					if (this.except != null) {
						ExecutionException thrExcept = this.except;
						this.except = null;
						throw thrExcept;
					} else {
						throw ex;
					}
				}
			}
			
			synchronized (this) {
				return result;
			}				
		}
		
		@Override
		public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
			return this.getAll(timeout, unit);
		}
		public T get() throws InterruptedException, ExecutionException {
			try {
				return this.getAll(0L, null);
			} catch (TimeoutException ex) {
				return null; // Should not happen!
			}
		}
	}
}
