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
import muscle.core.messaging.signal.Signal;
import utilities.data.SerializableData;

/**
 *
 * @author Joris Borgdorff
 */
public class TcpReceiver<T extends Serializable> extends AbstractCommunicatingPoint<BasicMessage<T>,BasicMessage<SerializableData>,InstanceID,PortalID<InstanceID>> implements Receiver<BasicMessage<T>,BasicMessage<SerializableData>,InstanceID,PortalID<InstanceID>> {
	private BlockingQueue<BasicMessage<T>> queue;
	private BlockingQueue<Signal> sigQueue;
	
	public TcpReceiver() {
		this.queue = new LinkedBlockingQueue<BasicMessage<T>>();
		this.sigQueue = new LinkedBlockingQueue<Signal>();
	}

	
	public void put(BasicMessage<SerializableData> msg) {
		queue.add(converter.deserialize(msg));
	}
	
	public void putSignal(Signal s) {
		sigQueue.add(s);
	}

	@Override
	public void dispose() {
		// If the queue was waiting on a take
		this.queue.clear();
		this.queue = null;
		this.sigQueue.clear();
		this.sigQueue = null;
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

	@Override
	public boolean hasSignal() {
		return !this.sigQueue.isEmpty();
	}

	@Override
	public Signal getSignal() {
		return sigQueue.poll();
	}
}
