/*
 * 
 */
package muscle.client.instance;

import java.io.Serializable;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import muscle.client.communication.Receiver;
import muscle.client.communication.message.DetachConduitSignal;
import muscle.client.communication.message.Message;
import muscle.core.ConduitExit;
import muscle.core.ConduitExitController;
import muscle.core.DataTemplate;
import muscle.id.PortalID;
import muscle.core.kernel.InstanceController;
import muscle.core.model.Observation;
import muscle.util.data.SingleProducerConsumerBlockingQueue;

/**
 *
 * @author Joris Borgdorff
 */
public class ConduitExitControllerImpl<T extends Serializable> extends Portal<T> implements ConduitExitController<T> {
	private Receiver<T, ?,?,?> receiver;
	private ConduitExit<T> conduitExit;
	private final BlockingQueue<Observation<T>> queue;
	private static final Logger logger = Logger.getLogger(ConduitExitControllerImpl.class.getName());
	private boolean isDetached;

	public ConduitExitControllerImpl(PortalID newPortalID, InstanceController newOwnerAgent, DataTemplate newDataTemplate) {
		super(newPortalID, newOwnerAgent, newDataTemplate);
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
	
	public String toString() {
		return "in:" + this.getIdentifier();
	}

	@Override
	public BlockingQueue<Observation<T>> getMessageQueue() {
		return this.queue;
	}

	@Override
	public void messageReceived(Observation<T> obs) {
		this.setNextTimestamp(obs.getNextTimestamp());
	}
}
