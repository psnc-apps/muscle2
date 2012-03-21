/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package muscle.core.conduit.communication;

import java.io.Serializable;
import muscle.core.PortFactory;
import muscle.core.ident.PortalID;
import muscle.core.kernel.InstanceController;
import muscle.core.messaging.Observation;
import muscle.core.messaging.serialization.DataConverter;
import muscle.core.messaging.serialization.ObservationConverter;
import muscle.core.messaging.serialization.SerializableDataConverter;
import muscle.net.SocketFactory;
import utilities.data.SerializableData;

/**
 *
 * @author Joris Borgdorff
 */
public class TcpPortFactoryImpl extends PortFactory {
	private final DataConverter<Observation<?>,Observation<SerializableData>> converter = new ObservationConverter(new SerializableDataConverter());
	private final SocketFactory socketFactory;
		
	public TcpPortFactoryImpl(SocketFactory sf) {
		this.socketFactory = sf;
	}
	
	@Override
	public <T extends Serializable> Receiver<T, ?, ?, ?> getReceiver(InstanceController localInstance, PortalID otherSide) {
		TcpReceiver recv = new TcpReceiver();
		recv.setDataConverter(converter);
		recv.setComplementaryPort(otherSide);
		return recv;		
	}

	@Override
	public <T extends Serializable> Transmitter<T, ?, ?, ?> getTransmitter(InstanceController localInstance, PortalID otherSide) {
		XdrTcpTransmitter trans = new XdrTcpTransmitter(socketFactory);
		trans.setDataConverter(converter);
		trans.setComplementaryPort(otherSide);
		return trans;
	}
}
