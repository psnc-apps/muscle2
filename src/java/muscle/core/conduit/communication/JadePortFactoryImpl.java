/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package muscle.core.conduit.communication;

import jade.lang.acl.ACLMessage;
import java.io.Serializable;
import muscle.core.PortFactory;
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
public class JadePortFactoryImpl extends PortFactory {
	@Override
	public <T extends Serializable> Receiver<T, ?, ?, ?> getReceiver(InstanceController localInstance, PortalID otherSide) {
		Receiver<T,ACLMessage, JadeIdentifier, JadePortalID> recv = new JadeReceiver();
		recv.setDataConverter(new ACLConverter(new ByteJavaObjectConverter()));
		if (otherSide instanceof JadePortalID) {
			recv.setComplementaryPort((JadePortalID)otherSide);
		} else {
			throw new IllegalArgumentException("Only JADE portals can be created by JadePortFactoryImpl.");
		}
		return recv;
	}

	@Override
	public <T extends Serializable> Transmitter<T, ?, ?, ?> getTransmitter(InstanceController localInstance, PortalID otherSide)  {
		Transmitter<T,byte[],JadeIdentifier, JadePortalID> trans = new JadeTransmitter<T>((JadeInstanceController)localInstance);
		trans.setDataConverter(new ByteJavaObjectConverter<Observation<T>>());
		if (otherSide instanceof JadePortalID) {
			trans.setComplementaryPort((JadePortalID)otherSide);
		} else {
			throw new IllegalArgumentException("Only JADE portals can be created by JadePortFactoryImpl.");
		}
		return trans;
	}
}
