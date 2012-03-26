/*
 * 
 */
package muscle.core.conduit.communication;

import java.io.Serializable;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import muscle.core.ident.InstanceID;
import muscle.core.ident.PortalID;
import muscle.core.messaging.BasicMessage;
import muscle.core.messaging.Message;
import utilities.data.SerializableData;
import utilities.data.SingleProducerConsumerBlockingQueue;

/**
 *
 * @author Joris Borgdorff
 */
public class TcpReceiver<T extends Serializable> extends AbstractCommunicatingPoint<Message<T>,BasicMessage<SerializableData>,InstanceID,PortalID<InstanceID>> implements Receiver<T,BasicMessage<SerializableData>,InstanceID,PortalID<InstanceID>> {
	private BlockingQueue<Message<T>> queue;
	
	public TcpReceiver() {
		this.queue = new SingleProducerConsumerBlockingQueue<Message<T>>(1024);
	}

	
	public void put(BasicMessage<SerializableData> msg) {
		try {
			queue.put(converter.deserialize(msg));
		} catch (InterruptedException ex) {
			Logger.getLogger(TcpReceiver.class.getName()).log(Level.SEVERE, null, ex);
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
			Logger.getLogger(TcpReceiver.class.getName()).log(Level.FINE, "Receiver stopped.");
			return null;
		}
	}
}
