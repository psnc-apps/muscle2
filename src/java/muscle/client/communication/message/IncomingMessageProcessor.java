/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package muscle.client.communication.message;

import muscle.client.communication.Receiver;
import muscle.id.Identifier;

/**
 *
 * @author Joris Borgdorff
 */
public interface IncomingMessageProcessor {
	public void addReceiver(Identifier id, Receiver recv);
	public void removeReceiver(Identifier id);
}
