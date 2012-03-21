/*
 * 
 */
package muscle.core;

import jade.lang.acl.ACLMessage;
import java.io.Serializable;
import muscle.core.conduit.communication.JadeReceiver;
import muscle.core.conduit.communication.JadeTransmitter;
import muscle.core.conduit.communication.Receiver;
import muscle.core.conduit.communication.Transmitter;
import muscle.core.ident.JadeIdentifier;
import muscle.core.ident.JadePortalID;
import muscle.core.ident.PortalID;
import muscle.core.kernel.InstanceController;
import muscle.core.kernel.JadeInstanceController;
import muscle.core.messaging.Observation;
import muscle.core.messaging.serialization.ACLConverter;
import muscle.core.messaging.serialization.ByteJavaObjectConverter;

/**
 *
 * @author Joris Borgdorff
 */
public abstract class PortFactory {	
	
	public abstract <T extends Serializable> Receiver<T,?,?,?> getReceiver(InstanceController localInstance, PortalID otherSide);
	
	public abstract <T extends Serializable> Transmitter<T,?,?,?> getTransmitter(InstanceController localInstance, PortalID otherSide);
	
	// Singleton pattern
	protected PortFactory() {}
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
}
