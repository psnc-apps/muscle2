/*
 * 
 */
package muscle.client.communication;

import java.io.Serializable;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import muscle.client.communication.message.IncomingMessageProcessor;
import muscle.client.instance.ConduitEntranceControllerImpl;
import muscle.client.instance.ConduitExitControllerImpl;
import muscle.core.kernel.InstanceController;
import muscle.id.Identifier;
import muscle.id.PortalID;
import muscle.id.Resolver;
import muscle.id.ResolverFactory;
import muscle.util.concurrency.Disposable;
import muscle.util.concurrency.LimitedThreadPool;
import muscle.util.concurrency.NamedCallable;

/**
 * Assigns Receivers and Transmitters to Portals.
 * 
 * @author Joris Borgdorff
 */
public abstract class PortFactory implements Disposable {
	protected final LimitedThreadPool executor;
	protected final ResolverFactory resolverFactory;
	private Resolver resolver;
	protected final IncomingMessageProcessor messageProcessor;
	private final static Logger logger = Logger.getLogger(PortFactory.class.getName());
	
	/**
	 * Assigns a Receiver to a ConduitExitController in a Thread.
	 * 
	 * By evaluating the Future that is returned, it is possible to determine when this has taken place and what the actual assigned receiver was.
	 * The call is non-blocking, however, the returned Future can be evaluated with a blocking call.
	 */
	public <T extends Serializable> Receiver<T,?> getReceiver(InstanceController ic, ConduitExitControllerImpl<T> localInstance, PortalID otherSide) {
		try {
			return this.<T>getReceiverTask(ic, localInstance, otherSide).call();
		} catch (Exception ex) {
			Logger.getLogger(PortFactory.class.getName()).log(Level.SEVERE, "Could not instantiate receiver", ex);
			return null;
		}
	}
	
	/**
	 * Assigns a Transmitter to a ConduitEntranceController in a Thread.
	 * 
	 * By evaluating the Future that is returned, it is possible to determine when this has taken place and what the actual assigned transmitter was.
	 * The call is non-blocking, however, the returned Future can be evaluated with a blocking call.
	 */
	public <T extends Serializable> Future<Transmitter<T,?>> getTransmitter(InstanceController ic, ConduitEntranceControllerImpl<T> localInstance, PortalID otherSide, boolean shared) {
		return executor.submit(this.<T>getTransmitterTask(ic, localInstance, otherSide, shared));
	}

	/**
	 * Creates a task that will assign a receiver to a ConduitExitController.
	 * 
	 * In this task, the receiver must also be added to the messageProcessor, and the otherSide might have to be resolved.
	 */
	protected abstract <T extends Serializable> NamedCallable<Receiver<T,?>> getReceiverTask(InstanceController ic, ConduitExitControllerImpl<T> localInstance, PortalID otherSide);
	
	/**
	 * Creates a task that will assign a transmitter to a ConduitEntranceController.
	 * 
	 * In this task, the otherSide might have to be resolved.
	 */
	protected abstract <T extends Serializable> NamedCallable<Transmitter<T,?>> getTransmitterTask(InstanceController ic, ConduitEntranceControllerImpl<T> localInstance, PortalID otherSide, boolean shared);
	
	protected PortFactory(ResolverFactory rf, IncomingMessageProcessor msgProcessor) {
		this.executor = new LimitedThreadPool(16);
		this.resolverFactory = rf;
		this.messageProcessor = msgProcessor;
		this.resolver = null;
	}
	
	public void start() {
		this.executor.start();
	}
	
	/** Resolves a PortalID, if not already done. */
	protected boolean resolvePort(PortalID port) throws InterruptedException {
		if (!port.isResolved()) {
			Resolver res = getResolver();
			// Could not find resolver
			if (res == null) return false;
			
			res.resolveIdentifier(port);
			if (!port.isResolved()) return false;
		}
		return true;
	}
	
		/** Resolves a PortalID, if not already done. */
	protected boolean portWillActivate(PortalID port) throws InterruptedException {
		Resolver res = getResolver();
		// Could not find resolver
		if (res == null) return false;
			
		return res.identifierMayActivate(port);
	}
	
	protected synchronized Resolver getResolver() {
		if (resolver == null) {
			try {
				resolver = resolverFactory.getResolver();
			} catch (InterruptedException ex) {
				logger.log(Level.SEVERE, "Could not find resolver", ex);
			}
		}
		return resolver;
	}
	
	public void removeReceiver(Identifier id) {
		this.messageProcessor.removeReceiver(id);
	}
	
	/** Frees all resources attached to the PortFactory. After this call, getReceiver() and getTransmitter() can not be called anymore. */
	public void dispose() {
		this.executor.dispose();
	}
	
	public boolean isDisposed() {
		return this.executor.isDisposed();
	}
}
