/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package muscle.client.communication;

import java.io.Serializable;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import muscle.client.communication.message.IncomingMessageProcessor;
import muscle.client.instance.ConduitEntranceControllerImpl;
import muscle.client.instance.ConduitExitControllerImpl;
import muscle.core.ident.InstanceID;
import muscle.core.ident.PortalID;
import muscle.core.ident.ResolverFactory;
import muscle.core.kernel.InstanceController;
import muscle.net.SocketFactory;
import muscle.util.serialization.BasicMessageConverter;
import muscle.util.serialization.ObservationConverter;
import muscle.util.serialization.SerializableDataConverter;

/**
 *
 * @author Joris Borgdorff
 */
public class TcpPortFactoryImpl extends PortFactory {
	private final SocketFactory socketFactory;
		
	public TcpPortFactoryImpl(ResolverFactory rf, SocketFactory sf, IncomingMessageProcessor msgProcessor) {
		super(rf, msgProcessor);
		this.socketFactory = sf;
	}

	@Override
	protected <T extends Serializable> Callable<Receiver<T, ?, ?, ?>> getReceiverTask(final ConduitExitControllerImpl<T> exit, final PortalID port) {
		return new Callable<Receiver<T,?,?,?>>() {
			@SuppressWarnings("unchecked")
			public Receiver<T, ?, ?, ?> call() throws Exception {
				exit.start();
				
				@SuppressWarnings("unchecked")
				PortalID<InstanceID> instancePort = (PortalID<InstanceID>)port;
				
				TcpReceiver<T> recv = new TcpReceiver<T>();
				recv.setDataConverter(new BasicMessageConverter(new SerializableDataConverter()));
				recv.setComplementaryPort(instancePort);
				exit.setReceiver(recv);
			
				messageProcessor.addReceiver(exit.getIdentifier(), recv);
				
				return recv;
			}
		};
	}

	@Override
	protected <T extends Serializable> Callable<Transmitter<T, ?, ?, ?>> getTransmitterTask(InstanceController ic, final ConduitEntranceControllerImpl<T> entrance, final PortalID port) {
		return new Callable<Transmitter<T,?,?,?>>() {
			@SuppressWarnings("unchecked")
			public Transmitter<T, ?, ?, ?> call() throws Exception {
				entrance.start();
				if (!resolvePort(port)) {
					Logger.getLogger(TcpPortFactoryImpl.class.getName()).log(Level.SEVERE, "Could not resolve port {0} for {1}.", new Object[]{port, entrance});
				}
				@SuppressWarnings("unchecked")
				PortalID<InstanceID> instancePort = (PortalID<InstanceID>)port;
				
				TcpTransmitter<T> trans = new TcpTransmitter<T>(socketFactory);
				trans.setDataConverter(new ObservationConverter(new SerializableDataConverter()));
				trans.setComplementaryPort(instancePort);
				entrance.setTransmitter(trans);
				return trans;
			}
		};
	}
}
