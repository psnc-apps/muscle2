/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package muscle.util.concurrency;

/**
 * @author Joris Borgdorff
 */
public abstract class SafeTriggeredThread extends SafeThread {
	protected boolean triggered;
	
	public SafeTriggeredThread(String name) {
		super(name);
		this.triggered = false;
	}
	
	public synchronized void trigger() {
		this.triggered = true;
		this.notifyAll();
	}
	
	/**
	 * Wait until apply notify is called. Returns false if the thread should halt.
	 */
	protected synchronized boolean continueComputation() throws InterruptedException {
		while (!isDisposed() && !triggered) {
			wait();
		}
		triggered = false;
		return !isDisposed();
	}
}
