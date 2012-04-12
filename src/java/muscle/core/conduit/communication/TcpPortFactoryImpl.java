/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package muscle.core.conduit.communication;

import java.io.Serializable;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import muscle.core.ConduitEntranceController;
import muscle.core.ConduitExitController;
import muscle.core.PortFactory;
import muscle.core.ident.InstanceID;
import muscle.core.ident.PortalID;
import muscle.core.ident.ResolverFactory;
import muscle.core.kernel.InstanceController;
import muscle.core.messaging.serialization.BasicMessageConverter;
import muscle.core.messaging.serialization.ObservationConverter;
import muscle.core.messaging.serialization.SerializableDataConverter;
import muscle.net.SocketFactory;

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
	protected <T extends Serializable> Callable<Receiver<T, ?, ?, ?>> getReceiverTask(final ConduitExitController<T> exit, final PortalID port) {
		return new Callable<Receiver<T,?,?,?>>() {
			@SuppressWarnings("unchecked")
			public Receiver<T, ?, ?, ?> call() throws Exception {
				exit.start();
				if (!resolvePort(port)) {
					Logger.getLogger(TcpPortFactoryImpl.class.getName()).log(Level.SEVERE, "Could not resolve port {0} for {1}.", new Object[]{port, exit});
				}
			
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
	protected <T extends Serializable> Callable<Transmitter<T, ?, ?, ?>> getTransmitterTask(InstanceController ic, final ConduitEntranceController<T> entrance, final PortalID port) {
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
