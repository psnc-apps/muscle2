/*
 * 
 */
package muscle.core;

import java.io.Serializable;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import muscle.core.conduit.communication.IncomingMessageProcessor;
import muscle.core.conduit.communication.Receiver;
import muscle.core.conduit.communication.Transmitter;
import muscle.core.ident.PortalID;
import muscle.core.ident.ResolverFactory;
import muscle.core.kernel.InstanceController;

/**
 *
 * @author Joris Borgdorff
 */
public abstract class PortFactory {
	protected final ExecutorService executor;
	protected final ResolverFactory resolverFactory;
	protected final IncomingMessageProcessor messageProcessor;
	private final static Logger logger = Logger.getLogger(PortFactory.class.getName());
	
	public <T extends Serializable> Future<Receiver<T,?,?,?>> getReceiver(ConduitExitController localInstance, PortalID otherSide) {
		return executor.submit(this.<T>getReceiverTask(localInstance, otherSide));
	}
	
	protected abstract <T extends Serializable> Callable<Receiver<T,?,?,?>> getReceiverTask(ConduitExitController localInstance, PortalID otherSide);
	
	public <T extends Serializable> Future<Transmitter<T,?,?,?>> getTransmitter(InstanceController ic, ConduitEntranceController localInstance, PortalID otherSide) {
		return executor.submit(this.<T>getTransmitterTask(ic, localInstance, otherSide));
	}

	protected abstract <T extends Serializable> Callable<Transmitter<T,?,?,?>> getTransmitterTask(InstanceController ic, ConduitEntranceController localInstance, PortalID otherSide);
	
	// Singleton pattern
	protected PortFactory(ResolverFactory rf, IncomingMessageProcessor msgProcessor) {
		this.executor = Executors.newCachedThreadPool();
		this.resolverFactory = rf;
		this.messageProcessor = msgProcessor;
	}
	
	private static PortFactory instance = null;
	public static PortFactory getInstance() {
		if (instance == null) {
			throw new IllegalStateException("PortFactory implementation could not be determined.");
		}
		return instance;
	}
	
	static void setImpl(PortFactory factory) {
		instance = factory;
	}
	
	protected boolean resolvePort(PortalID port) {
		if (!port.isResolved()) {
			try {
				resolverFactory.getResolver().resolveIdentifier(port);
				if (!port.isResolved()) return false;
			} catch (InterruptedException ex) {
				logger.log(Level.SEVERE, "Resolving port interrupted", ex);
				return false;
			}
		}
		return true;
	}
	
	public IncomingMessageProcessor getMessageProcessor() {
		return this.messageProcessor;
	}
	
	public void dispose() {
		executor.shutdown();
	}
}
