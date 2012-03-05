/*
 * 
 */
package muscle.core.conduit.communication;

import java.io.Serializable;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import muscle.core.ident.InstanceID;
import muscle.core.ident.PortalID;
import muscle.core.messaging.BasicMessage;

/**
 *
 * @author Joris Borgdorff
 */
public class TcpReceiver<T extends Serializable> extends AbstractCommunicatingPoint<BasicMessage<T>,BasicMessage<byte[]>,InstanceID,PortalID<InstanceID>> implements Receiver<BasicMessage<T>,BasicMessage<byte[]>,InstanceID,PortalID<InstanceID>> {
	private BlockingQueue<BasicMessage<T>> queue;
	
	public TcpReceiver() {
		this.queue = new LinkedBlockingQueue<BasicMessage<T>>();
	}

	public void put(BasicMessage<byte[]> msg) {
		queue.add(converter.deserialize(msg));
	}

	public void dispose() {
		// If the queue was waiting on a take
		this.queue.clear();
		this.queue = null;
		super.dispose();
	}

	@Override
	public BasicMessage<T> receive() {
		try {
			return queue.take();
		} catch (InterruptedException ex) {
			Logger.getLogger(TcpReceiver.class.getName()).log(Level.INFO, "Receiver stopped", ex);
			return null;
		}
	}
}
