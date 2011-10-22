/*
 * 
 */
package muscle.core.conduit.communication;

import jade.lang.acl.ACLMessage;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import muscle.core.ident.JadeIdentifier;
import muscle.core.ident.JadePortalID;
import muscle.core.messaging.jade.DataMessage;

/**
 *
 * @author Joris Borgdorff
 */
public class JadeReceiver<T> extends AbstractCommunicatingPoint<DataMessage<T>, ACLMessage,JadeIdentifier,JadePortalID> implements Receiver<DataMessage<T>, ACLMessage,JadeIdentifier,JadePortalID> {
	private BlockingQueue<DataMessage<T>> queue;
	
	public JadeReceiver() {
		this.queue = new LinkedBlockingQueue<DataMessage<T>>();
	}

	public void put(DataMessage<T> msg) {
		queue.add(msg);
	}

	public void dispose() {
		// If the queue was waiting on a take
		this.queue.clear();
		this.queue = null;
		super.dispose();
	}

	@Override
	public DataMessage<T> receive() {
		try {
			return queue.take();
		} catch (InterruptedException ex) {
			Logger.getLogger(JadeReceiver.class.getName()).log(Level.INFO, "Receiver stopped", ex);
			return null;
		}
	}
}
