/*
 * 
 */
package muscle.core;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import muscle.core.conduit.communication.Receiver;
import muscle.core.ident.PortalID;
import muscle.core.kernel.InstanceController;
import muscle.core.messaging.Message;

/**
 *
 * @author Joris Borgdorff
 */
public class ConduitExitController<T> extends Portal<T> {
	private Receiver<Message<T>, ?> receiver;
	private ConduitExit<T> conduitExit;
	private final BlockingQueue<T> queue;
	private static final Logger logger = Logger.getLogger(ConduitExitController.class.getName());

	public ConduitExitController(PortalID newPortalID, InstanceController newOwnerAgent, int newRate, DataTemplate newDataTemplate) {
		super(newPortalID, newOwnerAgent, newRate, newDataTemplate);
		this.queue = new LinkedBlockingQueue<T>();
		this.receiver = null;
		this.conduitExit = null;
	}
	
	public synchronized void setReceiver(Receiver<Message<T>, ?> recv) {
		this.receiver = recv;
		logger.log(Level.FINE, "ConduitExit <{0}> is now attached.", portalID);

		this.notifyAll();
	}
	
	BlockingQueue<T> getQueue() {
		return this.queue;
	}

	public void setExit(ConduitExit<T> exit) {
		this.conduitExit = exit;
	}

	public ConduitExit<T> getExit() {
		return this.conduitExit;
	}

	@Override
	protected void execute() throws InterruptedException {
		Receiver<Message<T>, ?> recv = waitForReceiver();
		if (recv != null) {
			Message<T> msg = this.receiver.receive();
			if (msg != null) {
				this.queue.add(msg.getRawData());
			}
		}
	}
	
	private synchronized Receiver<Message<T>, ?> waitForReceiver() throws InterruptedException {
		while (!isDone && this.receiver == null) {
			logger.log(Level.FINE, "ConduitExit <{0}> is waiting for connection to receive a message over.", portalID);
			wait(WAIT_FOR_ATTACHMENT_MILLIS);
		}
		return this.receiver;
	}
	
	@Override
	public synchronized void dispose() {
		// Empty the message queue and signal a null to the conduitexit
		queue.clear();
		queue.offer(null);
		receiver = null;
		super.dispose();
	}

	@Override
	protected void handleInterruption(InterruptedException ex) {
		logger.log(Level.SEVERE, "ConduitExitController was interrupted", ex);
	}
	
	protected synchronized boolean continueComputation() {
		return !isDone;
	}
}
