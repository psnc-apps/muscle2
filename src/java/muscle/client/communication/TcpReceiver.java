/*
 * 
 */
package muscle.client.communication;

import java.io.Serializable;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import muscle.client.communication.message.BasicMessage;
import muscle.client.communication.message.Message;
import muscle.id.PortalID;
import muscle.util.data.SingleProducerConsumerBlockingQueue;
import muscle.util.serialization.DataConverter;

/**
 *
 * @author Joris Borgdorff
 */
public class TcpReceiver<T extends Serializable> extends AbstractCommunicatingPoint<Message<T>,BasicMessage> implements Receiver<T,BasicMessage> {
	private volatile BlockingQueue<Message<T>> queue;
	
	public TcpReceiver(DataConverter<Message<T>,BasicMessage> converter, PortalID portalID) {
		super(converter, portalID);
		this.queue = new SingleProducerConsumerBlockingQueue<Message<T>>();
	}
	
	public void put(BasicMessage msg) {
		try {
			queue.put(converter.deserialize(msg));
		} catch (InterruptedException ex) {
			Logger.getLogger(TcpReceiver.class.getName()).log(Level.WARNING, "Receiver stopped; could not process received message", ex);
		} catch (NullPointerException ex) {
			Logger.getLogger(TcpReceiver.class.getName()).log(Level.WARNING, "Receiver stopped; could not process received message", ex);
		}
	}

	@Override
	public void dispose() {
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
			Logger.getLogger(TcpReceiver.class.getName()).log(Level.FINE, "Receiver stopped; not passing more messages.");
			return null;
		}
	}
}
