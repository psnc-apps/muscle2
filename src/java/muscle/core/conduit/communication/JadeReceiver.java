/*
 * 
 */
package muscle.core.conduit.communication;

import java.io.Serializable;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import muscle.core.ident.JadeIdentifier;
import muscle.core.ident.JadePortalID;
import muscle.core.messaging.Message;
import muscle.core.messaging.jade.DataMessage;
import utilities.data.SingleProducerConsumerBlockingQueue;

/**
 *
 * @author Joris Borgdorff
 */
public class JadeReceiver<T extends Serializable> extends AbstractCommunicatingPoint<Message<T>,DataMessage<T>,JadeIdentifier,JadePortalID> implements Receiver<T,DataMessage<T>,JadeIdentifier,JadePortalID> {
	private BlockingQueue<Message<T>> queue;

	public JadeReceiver() {
		this.queue = new SingleProducerConsumerBlockingQueue<Message<T>>(10);
	}

	public void put(DataMessage<T> msg) {
		try {
			queue.put(this.converter.deserialize(msg));
		} catch (InterruptedException ex) {
			Logger.getLogger(JadeReceiver.class.getName()).log(Level.WARNING, "Receiver stopped; could not process received message.", ex);
		}
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
			Logger.getLogger(JadeReceiver.class.getName()).log(Level.FINE, "Receiver stopped; not passing more messages.");
			return null;
		}
	}
}
