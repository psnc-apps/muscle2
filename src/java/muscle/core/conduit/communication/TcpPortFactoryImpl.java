/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package muscle.core.conduit.communication;

import java.io.Serializable;
import java.util.concurrent.Callable;
import muscle.core.ConduitEntranceController;
import muscle.core.ConduitExitController;
import muscle.core.PortFactory;
import muscle.core.ident.PortalID;
import muscle.core.ident.ResolverFactory;
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
		
	public TcpPortFactoryImpl(ResolverFactory rf, SocketFactory sf, IncomingMessageProcessor msgProcessor) {
		super(rf, msgProcessor);
		this.socketFactory = sf;
	}

	@Override
	protected <T extends Serializable> Callable<Receiver<T, ?, ?, ?>> getReceiverTask(final ConduitExitController exit, final PortalID port) {
		return new Callable<Receiver<T,?,?,?>>() {
			public Receiver<T, ?, ?, ?> call() throws Exception {
				exit.start();
				resolvePort(port);
			
				TcpReceiver recv = new TcpReceiver();
				recv.setDataConverter(converter);
				recv.setComplementaryPort(port);
				exit.setReceiver(recv);
			
				messageProcessor.addReceiver(exit.getIdentifier(), recv);
				
				return recv;
			}
		};
	}

	@Override
	protected <T extends Serializable> Callable<Transmitter<T, ?, ?, ?>> getTransmitterTask(InstanceController ic, final ConduitEntranceController entrance, final PortalID port) {
		return new Callable<Transmitter<T,?,?,?>>() {
			public Transmitter<T, ?, ?, ?> call() throws Exception {
				entrance.start();
				resolvePort(port);
			
				XdrTcpTransmitter trans = new XdrTcpTransmitter(socketFactory);
				trans.setDataConverter(converter);
				trans.setComplementaryPort(port);
				entrance.setTransmitter(trans);
				return trans;
			}
		};
	}
}
