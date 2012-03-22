/*
 * 
 */
package muscle.core.conduit.communication;

import java.io.Serializable;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import muscle.core.ident.JadeIdentifier;
import muscle.core.ident.JadePortalID;
import muscle.core.messaging.Message;
import muscle.core.messaging.jade.DataMessage;

/**
 *
 * @author Joris Borgdorff
 */
public class JadeReceiver<T extends Serializable> extends AbstractCommunicatingPoint<Message<T>,DataMessage<T>,JadeIdentifier,JadePortalID> implements Receiver<T,DataMessage<T>,JadeIdentifier,JadePortalID> {
	private BlockingQueue<Message<T>> queue;

	public JadeReceiver() {
		this.queue = new LinkedBlockingQueue<Message<T>>();
	}

	public void put(DataMessage<T> msg) {
		queue.add(this.converter.deserialize(msg));
	}

	@Override
	public void dispose() {
		// If the queue was waiting on a take
		this.queue.clear();
		this.queue = null;
		super.dispose();
	}

	@Override
	public Message<T> receive() {
		try {
			return queue.take();
		} catch (InterruptedException ex) {
			Logger.getLogger(JadeReceiver.class.getName()).log(Level.INFO, "Receiver stopped", ex);
			return null;
		}
	}
}
