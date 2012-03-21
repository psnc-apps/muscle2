/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package muscle.core.conduit.communication;

import muscle.core.ident.Identifier;

/**
 *
 * @author Joris Borgdorff
 */
public interface IncomingMessageProcessor {
	public void addReceiver(Identifier id, Receiver recv);
}
