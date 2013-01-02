/*
 * 
 */
package muscle.client.communication;

import java.io.Serializable;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import muscle.client.communication.message.BasicMessage;
import muscle.client.communication.message.Message;
import muscle.client.communication.message.ShutdownSignal;
import muscle.id.PortalID;
import muscle.util.serialization.DataConverter;

/**
 *
 * @author Joris Borgdorff
 */
public class TcpReceiver<T extends Serializable> extends AbstractCommunicatingPoint<Message<T>,BasicMessage> implements Receiver<T,BasicMessage> {
	private final BlockingQueue<Message<T>> queue;
	private final static Message SHUTDOWN = new BasicMessage(new ShutdownSignal(), null);
	
	public TcpReceiver(DataConverter<Message<T>,BasicMessage> converter, PortalID portalID) {
		super(converter, portalID);
		this.queue = new LinkedBlockingQueue<Message<T>>();
	}
	
	public void put(BasicMessage msg) {
		queue.add(converter.deserialize(msg));
	}

	@Override
	public void dispose() {
		@SuppressWarnings("unchecked")
		Message<T> shut = (Message<T>)SHUTDOWN;
		this.queue.add(shut);
		super.dispose();
	}

	@Override
	public Message<T> receive() {
		try {
			Message<T> ret = queue.take();
			if (ret == SHUTDOWN) {
				return null;
			} else {
				return ret;
			}
		} catch (InterruptedException ex) {
			Logger.getLogger(TcpReceiver.class.getName()).log(Level.FINE, "Receiver stopped; not passing more messages.");
			return null;
		}
	}
}
