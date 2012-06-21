/*
 * 
 */
package muscle.core;

import java.io.Serializable;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import muscle.core.conduit.communication.Receiver;
import muscle.core.ident.PortalID;
import muscle.core.kernel.InstanceController;
import muscle.core.messaging.Message;
import muscle.core.messaging.Observation;
import muscle.core.messaging.signal.DetachConduitSignal;
import utilities.data.SingleProducerConsumerBlockingQueue;

/**
 *
 * @author Joris Borgdorff
 */
public class ConduitExitController<T extends Serializable> extends Portal<T> {
	private Receiver<T, ?,?,?> receiver;
	private ConduitExit<T> conduitExit;
	private final BlockingQueue<Observation<T>> queue;
	private static final Logger logger = Logger.getLogger(ConduitExitController.class.getName());
	private boolean isDetached;

	public ConduitExitController(PortalID newPortalID, InstanceController newOwnerAgent, int newRate, DataTemplate newDataTemplate) {
		super(newPortalID, newOwnerAgent, newRate, newDataTemplate);
		this.queue = new SingleProducerConsumerBlockingQueue<Observation<T>>();
		this.receiver = null;
		this.conduitExit = null;
		this.isDetached = false;
	}
	
	public synchronized void setReceiver(Receiver<T, ?,?,?> recv) {
		this.receiver = recv;
		logger.log(Level.FINE, "ConduitExit <{0}> is now attached.", portalID);

		this.notifyAll();
	}
	
	public void setExit(ConduitExit<T> exit) {
		this.conduitExit = exit;
	}

	public ConduitExit<T> getExit() {
		return this.conduitExit;
	}

	@Override
	protected void execute() throws InterruptedException {
		Receiver<T, ?,?,?> recv = waitForReceiver();
		if (recv != null) {
			Message<T> dmsg = this.receiver.receive();
			if (dmsg != null) {
				if (dmsg.isSignal()) {
					if (dmsg.getSignal() instanceof DetachConduitSignal) {
						queue.put(null);
						this.isDetached = true;
					}
				} else {
					this.queue.put(dmsg.getObservation());
					increment();
				}
			}
		}
	}
	
	private synchronized Receiver<T, ?,?,?> waitForReceiver() throws InterruptedException {
		while (!isDisposed() && this.receiver == null) {
			logger.log(Level.FINE, "ConduitExit <{0}> is waiting for connection to receive a message over.", portalID);
			wait(WAIT_FOR_ATTACHMENT_MILLIS);
		}
		return this.receiver;
	}
	
	@Override
	public synchronized void dispose() {
		// dispose of the conduitexit, and send a null in case it was blocked.
		this.conduitExit.dispose();
		receiver = null;
		super.dispose();
	}

	@Override
	protected void handleInterruption(InterruptedException ex) {
		if (continueComputation()) {
			logger.log(Level.SEVERE, "ConduitExitController was interrupted", ex);
		}
	}
	
	protected boolean continueComputation() {
		return !isDetached && !isDisposed();
	}
	
	BlockingQueue<Observation<T>> getQueue() {
		return this.queue;
	}

	public String toString() {
		return "in:" + this.getIdentifier();
	}
}
