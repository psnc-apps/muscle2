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
import muscle.client.communication.message.LocalDataHandler;
import muscle.client.instance.ConduitEntranceControllerImpl;
import muscle.client.instance.ConduitExitControllerImpl;
import muscle.client.instance.PassiveConduitExitController;
import muscle.client.instance.ThreadedConduitExitController;
import muscle.core.kernel.InstanceController;
import muscle.core.model.Observation;
import muscle.id.*;
import muscle.net.SocketFactory;
import muscle.util.serialization.BasicMessageConverter;
import muscle.util.serialization.ObservationConverter;
import muscle.util.serialization.PipeConverter;
import muscle.util.serialization.SerializableDataConverter;

/**
 *
 * @author Joris Borgdorff
 */
public class TcpPortFactoryImpl extends PortFactory {
	private final SocketFactory socketFactory;
	private final LocalDataHandler localMsgProcessor;
		
	public TcpPortFactoryImpl(ResolverFactory rf, SocketFactory sf, IncomingMessageProcessor globalMsgProcessor, LocalDataHandler localMsgProcessor) {
		super(rf, globalMsgProcessor);
		this.localMsgProcessor = localMsgProcessor;
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
				
				boolean passive = exit instanceof PassiveConduitExitController;
				
				Receiver recv = passive ?  (PassiveConduitExitController)exit : new TcpReceiver<T>();
				
				recv.setComplementaryPort(instancePort);
				
				Resolver res = getResolver();
				if (res.isLocal(instancePort)) {
					recv.setDataConverter(new PipeConverter());
					localMsgProcessor.addReceiver(exit.getIdentifier(), recv);				
				} else {
					recv.setDataConverter(new BasicMessageConverter(new SerializableDataConverter()));
					messageProcessor.addReceiver(exit.getIdentifier(), recv);				
				}
				
				if (!passive && exit instanceof ThreadedConduitExitController) {
					((ThreadedConduitExitController)exit).setReceiver(recv);
				}
			
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
				
				Resolver res = getResolver();
				Transmitter trans;
				if (res.isLocal(instancePort)) {
					trans = new LocalTransmitter<T>(localMsgProcessor);
					ObservationConverter<T,T> copyPipe = new ObservationConverter<T,T>(new SerializableDataConverter()) {
						public Observation<T> serialize(Observation<T> data) {
							return data.copyWithNewData(this.converter.copy(data.getData()));
						}
					};
					trans.setDataConverter(copyPipe);
				} else {
					trans = new TcpTransmitter<T>(socketFactory);
					trans.setDataConverter(new ObservationConverter(new SerializableDataConverter()));
				}
				trans.setComplementaryPort(instancePort);
				entrance.setTransmitter(trans);
				return trans;
			}
		};
	}
	
	@Override
	public void removeReceiver(Identifier id) {
		this.messageProcessor.removeReceiver(id);
		this.localMsgProcessor.removeReceiver(id);
	}
}
