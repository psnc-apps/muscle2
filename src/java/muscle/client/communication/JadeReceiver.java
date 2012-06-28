/*
 * 
 */
package muscle.client.communication;

import muscle.client.communication.message.Message;
import muscle.client.communication.message.JadeMessage;
import java.io.Serializable;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import muscle.client.ident.JadeIdentifier;
import muscle.client.ident.JadePortalID;
import muscle.util.data.SingleProducerConsumerBlockingQueue;

/**
 *
 * @author Joris Borgdorff
 */
public class JadeReceiver<T extends Serializable> extends AbstractCommunicatingPoint<Message<T>,JadeMessage<T>,JadeIdentifier,JadePortalID> implements Receiver<T,JadeMessage<T>,JadeIdentifier,JadePortalID> {
	private volatile BlockingQueue<Message<T>> queue;

	public JadeReceiver() {
		this.queue = new SingleProducerConsumerBlockingQueue<Message<T>>();
	}

	public void put(JadeMessage<T> msg) {
		try {
			queue.put(this.converter.deserialize(msg));
		} catch (InterruptedException ex) {
			Logger.getLogger(JadeReceiver.class.getName()).log(Level.WARNING, "Receiver stopped; could not process received message.", ex);
		} catch (NullPointerException ex) {
			Logger.getLogger(TcpReceiver.class.getName()).log(Level.WARNING, "Receiver stopped; could not process received message", ex);
		}
	}

	@Override
	public void dispose() {
		// If the queue was waiting on a take
		this.queue = null;
		super.dispose();
	}

	@Override
	public Message<T> receive() {
		try {
			BlockingQueue<Message<T>> recvQueue = queue;
			if (recvQueue == null) return null;
			return recvQueue.take();
		} catch (InterruptedException ex) {
			Logger.getLogger(JadeReceiver.class.getName()).log(Level.FINE, "Receiver stopped; not passing more messages.");
			return null;
		}
	}
}
