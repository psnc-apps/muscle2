/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package muscle.util.concurrency;

/**
 *
 * @author Joris Borgdorff
 */
public interface NamedRunnable extends Runnable, Disposable {
	public String getName();
}
