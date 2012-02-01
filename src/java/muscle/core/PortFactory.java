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
import muscle.core.messaging.jade.DataMessage;
import muscle.core.messaging.serialization.ACLConverter;
import muscle.core.messaging.serialization.ByteJavaObjectConverter;
import muscle.core.messaging.Observation;

/**
 *
 * @author Joris Borgdorff
 */
public class PortFactory {
	public <T> Receiver<DataMessage<T>,?,?,?> getReceiver(InstanceController localInstance, PortalID otherSide) {
		Receiver<DataMessage<T>,ACLMessage, JadeIdentifier, JadePortalID> recv = new JadeReceiver<T>();
		recv.setDataConverter(new ACLConverter<T>(new ByteJavaObjectConverter<T>()));
		if (otherSide instanceof JadePortalID) {
			recv.setComplementaryPort((JadePortalID)otherSide);
		}
		else {
			throw new IllegalArgumentException("Only JADE portals are currently created.");
		}
		return recv;
	}
	
	public <T extends Serializable> Transmitter<T,?,?,?> getTransmitter(InstanceController localInstance, PortalID otherSide) {
		Transmitter<T,byte[],JadeIdentifier, JadePortalID> trans = new JadeTransmitter<T>((JadeInstanceController)localInstance);
		trans.setDataConverter(new ByteJavaObjectConverter<Observation<T>>());
		if (otherSide instanceof JadePortalID) {
			trans.setComplementaryPort((JadePortalID)otherSide);
		}
		else {
			throw new IllegalArgumentException("Only JADE portals are currently created.");
		}
		return trans;
	}
	
	// Singleton pattern
	protected PortFactory() {}
	private static PortFactory instance = new PortFactory();
	public static PortFactory getInstance() {
		return instance;
	}
}
