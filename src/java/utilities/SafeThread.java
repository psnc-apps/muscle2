/*
 * 
 */
package utilities;

/**
 * A thread that does safe disposal of its resources.
 * @author Joris Borgdorff
 */
public abstract class SafeThread extends Thread {
	protected boolean isDone;
	
	public SafeThread() {
		this.isDone = false;
	}
	
	public void run() {
		while(!isDone) {
			execute();
		}
	}
	
	protected abstract void execute();
	
	public synchronized void dispose() {
		this.isDone = true;
		this.notifyAll();
	}
}
