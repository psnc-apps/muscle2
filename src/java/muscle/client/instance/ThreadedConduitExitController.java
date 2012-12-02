/*
 * 
 */
package muscle.client.instance;

import eu.mapperproject.jmml.util.FastArrayList;
import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import muscle.client.LocalManager;
import muscle.client.communication.Receiver;
import muscle.client.communication.message.DetachConduitSignal;
import muscle.client.communication.message.Message;
import muscle.core.ConduitDescription;
import muscle.core.ConduitExit;
import muscle.core.ConnectionScheme;
import muscle.core.DataTemplate;
import muscle.core.conduit.filter.FilterChain;
import muscle.core.kernel.InstanceController;
import muscle.core.model.Observation;
import muscle.exception.MUSCLEDatatypeException;
import muscle.id.PortalID;
import muscle.util.data.SingleProducerConsumerBlockingQueue;
import muscle.util.data.TakeableQueue;

/**
 *
 * @author Joris Borgdorff
 */
public class ThreadedConduitExitController<T extends Serializable> extends ThreadedPortal<T> implements ConduitExitControllerImpl<T> {
	private Receiver<T, ?> receiver;
	private ConduitExit<T> conduitExit;
	private final TakeableQueue<Observation<T>> queue;
	private static final Logger logger = Logger.getLogger(ThreadedConduitExitController.class.getName());
	private boolean isDetached;
	private final FilterChain filters;

	public ThreadedConduitExitController(PortalID newPortalID, InstanceController newOwnerAgent, DataTemplate newDataTemplate) {
		super(newPortalID, newOwnerAgent, newDataTemplate);
		this.queue = new SingleProducerConsumerBlockingQueue<Observation<T>>();
		this.receiver = null;
		this.conduitExit = null;
		this.isDetached = false;
		this.filters = createFilterChain();
	}
	
	/** Create a filter chain from the given arguments */
	private FilterChain createFilterChain() {
		ConnectionScheme cs = ConnectionScheme.getInstance();
		ConduitDescription cd = cs.exitDescriptionForPortal(portalID).getConduitDescription();
		List<String> args = cd.getArgs();
		if (args.isEmpty()) return null;
		int exitArgDiv = args.indexOf("") + 1;
		if (exitArgDiv == args.size()) return null;
		List<String> exitArgs = new FastArrayList<String>(args.size() - exitArgDiv);
		for (int i = exitArgDiv; i < args.size(); i++) {
			exitArgs.add(args.get(i));
		}
		
		FilterChain fc = new FilterChain() {
			protected void apply(Observation subject) {
				queue.add(subject);
			}
		};
		
		fc.init(exitArgs);
		logger.log(Level.INFO, "The conduit exit ''{0}'' will use filter(s) {1}.", new Object[] {cd, exitArgs});
		return fc;
	}

	public synchronized void setReceiver(Receiver<T, ?> recv) {
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

	protected synchronized void setUp() throws InterruptedException {
		// Try to get the receiver. If the loop exits because there was
		// a dispose, then execute will not be called.
		while (this.receiver == null && !isDisposed()) {
			logger.log(Level.FINE, "ConduitExit <{0}> is waiting for connection to receive a message over.", portalID);
			wait(WAIT_FOR_ATTACHMENT_MILLIS);
		}
	}
	
	@Override
	protected void execute() throws InterruptedException {
		Message<T> dmsg = this.receiver.receive();
		if (dmsg != null) {
			if (dmsg.isSignal()) {
				if (dmsg.getSignal() instanceof DetachConduitSignal) {
					queue.put(null);
					this.isDetached = true;
				}
			} else {
				if (this.filters == null) {
					this.queue.put(dmsg.getObservation());
				} else {
					this.filters.process(dmsg.getObservation());
				}
				increment();
			}
		}
	}
	
	@Override
	public synchronized void dispose() {
		receiver = null;
		if (this.filters != null && !isDisposed()) {
			this.filters.dispose();
		} else {
			super.dispose();
		}
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
		return "threaded-in:" + this.getIdentifier();
	}

	@Override
	public TakeableQueue<Observation<T>> getMessageQueue() {
		return this.queue;
	}

	@Override
	public void messageReceived(Observation<T> obs) {
		this.resetTime(obs.getNextTimestamp());
	}
}
