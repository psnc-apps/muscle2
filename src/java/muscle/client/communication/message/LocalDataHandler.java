/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package muscle.client.communication.message;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import muscle.client.LocalManager;
import muscle.client.communication.Receiver;
import muscle.id.Identifier;
import muscle.id.PortalID;
import muscle.util.concurrency.Disposable;
import muscle.util.concurrency.SafeTriggeredThread;

/**
 *
 * @author Joris Borgdorff
 */
public class LocalDataHandler implements Disposable, IncomingMessageProcessor {
	private final Map<Identifier,Receiver> listener;
	private final static Logger logger = Logger.getLogger(LocalDataHandler.class.getName());
	private boolean isDone;

	public LocalDataHandler() {
		listener = new ConcurrentHashMap<Identifier,Receiver>();
	}
	
	@Override
	public void addReceiver(Identifier id, Receiver recv) {
		listener.put(id, recv);
	}
	
	public void put(Message msg) {
		Identifier recipient = msg.getRecipient();
		Receiver recv = listener.get(recipient);

		if (recv == null) {
			if (!msg.isSignal() || !(msg.getSignal() instanceof DetachConduitSignal)) {
				logger.log(Level.WARNING, "No receiver registered for message {0}.", msg);					
			}
		} else {
			recv.put(msg);
		}
	}
	
	@Override
	public void removeReceiver(Identifier id) {
		listener.remove(id);
	}
	
	@Override
	public void dispose() {
		this.isDone = true;
		for (Receiver recv : listener.values()) {
			recv.dispose();
		}
	}

	@Override
	public boolean isDisposed() {
		return this.isDone;
	}
}
