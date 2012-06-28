/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package muscle.util.concurrency;

/**
 * Disposable objects can be disposed in a Thread-safe way.
 * 
 * @author Joris Borgdorff
 */
public interface Disposable {
	/**
	 * Dispose of the current object.
	 * After the first call, any following call is a no-op.
	 */
	public void dispose();
	
	/**
	 * Whether the current object has been disposed of.
	 */
	public boolean isDisposed();
}
