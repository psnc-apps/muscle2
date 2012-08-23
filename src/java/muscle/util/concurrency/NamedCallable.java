/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package muscle.util.concurrency;

import java.util.concurrent.Callable;

/**
 *
 * @author Joris Borgdorff
 */
public interface NamedCallable<T> extends Callable<T> {
	public String getName();
}
