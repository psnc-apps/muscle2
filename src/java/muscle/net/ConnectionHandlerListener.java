/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package muscle.net;

/**
 * Main communication point of a ConnectionHandler
 * @author Joris Borgdorff
 */
public interface ConnectionHandlerListener {
	public void fatalException(Throwable ex);
}
