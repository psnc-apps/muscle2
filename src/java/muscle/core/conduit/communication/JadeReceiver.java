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
import muscle.core.messaging.signal.Signal;

/**
 *
 * @author Joris Borgdorff
 */
public class JadeReceiver<T extends DataMessage> extends AbstractCommunicatingPoint<T, ACLMessage,JadeIdentifier,JadePortalID> implements Receiver<T, ACLMessage,JadeIdentifier,JadePortalID> {
	private BlockingQueue<T> queue;
	private BlockingQueue<Signal> sigQueue;

	public JadeReceiver() {
		this.queue = new LinkedBlockingQueue<T>();
		this.sigQueue = new LinkedBlockingQueue<Signal>();
	}

	public void put(T msg) {
		queue.add(msg);
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
	public T receive() {
		try {
			return queue.take();
		} catch (InterruptedException ex) {
			Logger.getLogger(JadeReceiver.class.getName()).log(Level.INFO, "Receiver stopped", ex);
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
