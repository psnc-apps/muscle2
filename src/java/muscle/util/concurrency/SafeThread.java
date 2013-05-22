/*
* Copyright 2008, 2009 Complex Automata Simulation Technique (COAST) consortium
* Copyright 2010-2013 Multiscale Applications on European e-Infrastructures (MAPPER) project
*
* GNU Lesser General Public License
* 
* This file is part of MUSCLE (Multiscale Coupling Library and Environment).
* 
* MUSCLE is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
* 
* MUSCLE is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Lesser General Public License for more details.
* 
* You should have received a copy of the GNU Lesser General Public License
* along with MUSCLE.  If not, see <http://www.gnu.org/licenses/>.
*/
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
	public void dispose() {
		synchronized (this) {
			if (this.isDone) {
				return;
			}
			this.isDone = true;
			this.notifyAll();
		}
		this.interrupt();
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
