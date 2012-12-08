/*
 * 
 */
package muscle.util.concurrency;

/**
 * A thread that does safe disposal of its resources.
 * @author Joris Borgdorff
 */
public abstract class SafeThread extends Thread implements Disposable {
	/**
	 * This is set to true as soon as the thread should stop. Methods that do
	 * any long calculation or waiting should check this to see if they should
	 * continue their calculation.
	 */
	private boolean isDone;
	
	public SafeThread(String name) {
		super(name);
		this.isDone = false;
	}
	
	/** The thread calls execute() until it is done. */
	public final void run() {
		try {
			this.setUp();
			while(true) {
				// Call this try/catch inside the loop so that an interruption
				// puts you back to the beginning of the loop. The next evaluation
				// of continueComputation will determine whether the thread should
				// stop.
				try {
					if (!this.continueComputation()) {
						break;
					}
					this.execute();
				} catch (InterruptedException ex) {
					this.handleInterruption(ex);
				}
			}
		} catch (Throwable ex) {
			this.handleException(ex);
		}
		this.dispose();
	}
	
	/** Any initialization before execute is called can be done by overriding this method. */
	protected void setUp() throws InterruptedException {}
	
	protected abstract void handleInterruption(InterruptedException ex);
	protected abstract void handleException(Throwable ex);
	
	/** The thread will keep executing the contents of this method until it isDone. */
	protected abstract void execute() throws Exception;
	
	/**
	 * Signal the thread to stop its calculation. Can be overridden to clean up
	 * local variables. When overriding, always also call the super implementation.
	 */
	public synchronized void dispose() {
		if (this.isDone) {
			return;
		}
		this.isDone = true;
		this.interrupt();
		this.notifyAll();
	}
	
	/**
	 * Whether computation can continue. May wait for some time.
	 */
	protected synchronized boolean continueComputation() throws InterruptedException {
		return !isDone;
	}
	
	public synchronized boolean isDisposed() {
		return this.isDone;
	}
}
