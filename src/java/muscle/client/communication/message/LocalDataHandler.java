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
	private final Map<Identifier,Map<Identifier,Receiver>> listener;
	private final static Logger logger = Logger.getLogger(LocalDataHandler.class.getName());
	private boolean isDone;

	public LocalDataHandler() {
		listener = new ConcurrentHashMap<Identifier,Map<Identifier,Receiver>>();
	}
	
	@Override
	public void addReceiver(Identifier id, Receiver recv) {
		Identifier owner = ((PortalID)id).getOwnerID();
		Map<Identifier,Receiver> recvs = listener.get(owner);
		if (recvs == null) {
			synchronized (listener) {
				if (listener.containsKey(owner)) {
					recvs = listener.get(owner);
				} else {
					recvs = new ConcurrentHashMap<Identifier, Receiver>();
					listener.put(owner, recvs);
				}
			}
		}
		recvs.put(id, recv);
	}
	
	public void put(Message msg) {
		Identifier recipient = msg.getRecipient();
		Map<Identifier,Receiver> recvs = listener.get(((PortalID)recipient).getOwnerID());

		if (recvs == null) {
			if (!msg.isSignal() || !(msg.getSignal() instanceof DetachConduitSignal)) {
				logger.log(Level.WARNING, "No receiver registered for message {0}.", msg);					
			}
		} else {
			Receiver recv = recvs.get(recipient);
			if (recv == null) {
				logger.log(Level.WARNING, "No receiver known for message {0}.", msg);					
			} else {
				recv.put(msg);
			}
		}
	}
	
	@Override
	public void removeReceiver(Identifier id) {
		listener.remove(id);
	}
	
	@Override
	public void dispose() {
		this.isDone = true;
		for (Map<Identifier,Receiver> recvs : listener.values()) {
			for (Receiver recv : recvs.values()) {
				recv.dispose();
			}
		}
	}

	@Override
	public boolean isDisposed() {
		return this.isDone;
	}
}
