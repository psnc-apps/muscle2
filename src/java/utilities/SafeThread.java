/*
 * 
 */
package utilities;

/**
 * A thread that does safe disposal of its resources.
 * @author Joris Borgdorff
 */
public abstract class SafeThread extends Thread {
	/**
	 * This is set to true as soon as the thread should stop. Methods that do
	 * any long calculation or waiting should check this to see if they should
	 * continue their calculation.
	 */
	protected boolean isDone;
	
	public SafeThread() {
		this.isDone = false;
	}
	
	/** The thread calls execute() until it is done. */
	public void run() {
		try {
			while(continueComputation()) {
				execute();
			}
		} catch (InterruptedException ex) {
			this.handleInterruption(ex);
		}
	}
	
	
	protected abstract void handleInterruption(InterruptedException ex);
	
	/** The thread will keep executing the contents of this method until it isDone. */
	protected abstract void execute() throws InterruptedException;
	
	/**
	 * Signal the thread to stop its calculation. Can be overridden to clean up
	 * local variables. When overriding, always also call the super implementation.
	 */
	public synchronized void dispose() {
		this.isDone = true;
		this.notifyAll();
	}
	
	/**
	 * Whether computation can continue. May wait for some time.
	 */
	protected synchronized boolean continueComputation() throws InterruptedException {
		return !isDone;
	}
}
