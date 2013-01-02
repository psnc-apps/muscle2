/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package muscle.exception;

/**
 * Main communication point of a ConnectionHandler
 * @author Joris Borgdorff
 */
public interface ExceptionListener {
	public void fatalException(Throwable ex);
}
