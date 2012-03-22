/*
 * 
 */
package muscle.core.conduit.communication;

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
public class JadeReceiver<T extends DataMessage> extends AbstractCommunicatingPoint<T,T,JadeIdentifier,JadePortalID> implements Receiver<T,T,JadeIdentifier,JadePortalID> {
	private BlockingQueue<T> queue;

	public JadeReceiver() {
		this.queue = new LinkedBlockingQueue<T>();
	}

	public void put(T msg) {
		queue.add(msg);
	}

	@Override
	public void dispose() {
		// If the queue was waiting on a take
		this.queue.clear();
		this.queue = null;
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
}
