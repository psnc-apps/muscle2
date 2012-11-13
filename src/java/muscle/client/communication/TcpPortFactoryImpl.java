/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package muscle.client.communication;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import muscle.client.communication.message.IncomingMessageProcessor;
import muscle.client.communication.message.LocalDataHandler;
import muscle.client.instance.ConduitEntranceControllerImpl;
import muscle.client.instance.ConduitExitControllerImpl;
import muscle.client.instance.PassiveConduitExitController;
import muscle.client.instance.ThreadedConduitEntranceController;
import muscle.client.instance.ThreadedConduitExitController;
import muscle.core.kernel.InstanceController;
import muscle.core.model.Observation;
import muscle.id.*;
import muscle.net.SocketFactory;
import muscle.util.concurrency.NamedCallable;
import muscle.util.serialization.*;

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
	protected <T extends Serializable> NamedCallable<Receiver<T, ?>> getReceiverTask(final InstanceController ic, final ConduitExitControllerImpl<T> exit, final PortalID port) {
		return new NamedCallable<Receiver<T,?>>() {
			@SuppressWarnings("unchecked")
			public Receiver<T, ?> call() throws Exception {
				exit.start();
				try {
					if (!portWillActivate(port)) {
						throw new IllegalStateException("Port already shut down");
					}
				} catch (Exception ex) {
					Logger.getLogger(TcpPortFactoryImpl.class.getName()).log(Level.SEVERE, "Port {0} for {1} will not activate. Aborting.", new Object[]{port, exit});
					ic.fatalException(ex);
					throw ex;
				}
				@SuppressWarnings("unchecked")
				PortalID<InstanceID> instancePort = (PortalID<InstanceID>)port;
				
				boolean passive = exit instanceof PassiveConduitExitController;
				
				Receiver recv;
				DataConverter converter;
				
				Resolver res = getResolver();
				if (res.isLocal(instancePort)) {
					converter = new PipeConverter();
					if (passive) {
						recv = (PassiveConduitExitController)exit;
						((PassiveConduitExitController)recv).setDataConverter(converter);
					} else {
						recv = new TcpReceiver<T>(converter, instancePort);					
					}
					localMsgProcessor.addReceiver(exit.getIdentifier(), recv);				
				} else {
					converter = new BasicMessageConverter(new SerializableDataConverter());
					if (passive) {
						recv = (PassiveConduitExitController)exit;
						((PassiveConduitExitController)recv).setDataConverter(converter);
					} else {
						recv = new TcpReceiver<T>(converter, instancePort);
					}
					messageProcessor.addReceiver(exit.getIdentifier(), recv);				
				}
				
				if (!passive && exit instanceof ThreadedConduitExitController) {
					((ThreadedConduitExitController)exit).setReceiver(recv);
				}
			
				return recv;
			}

			@Override
			public String getName() {
				return "TCPReceiverLocator-" + port;
			}
		};
	}

	@Override
	protected <T extends Serializable> NamedCallable<Transmitter<T, ?>> getTransmitterTask(final InstanceController ic, final ConduitEntranceControllerImpl<T> entrance, final PortalID port, final boolean shared) {
		return new NamedCallable<Transmitter<T,?>>() {
			@SuppressWarnings("unchecked")
			public Transmitter<T, ?> call() throws Exception {
				entrance.start();
				try {
					if (!resolvePort(port)) {
						throw new IllegalStateException("Port was not resolved");
					}
				} catch (Exception ex) {
					Logger.getLogger(TcpPortFactoryImpl.class.getName()).log(Level.SEVERE, "Could not resolve port {0} for {1}. Aborting.", new Object[]{port, entrance});
					ic.fatalException(ex);
					throw ex;
				}
				@SuppressWarnings("unchecked")
				PortalID<InstanceID> instancePort = (PortalID<InstanceID>)port;
				
				Resolver res = getResolver();
				Transmitter trans;
				DataConverter converter;
				if (res.isLocal(instancePort)) {
					if (shared || entrance instanceof ThreadedConduitEntranceController) {
						converter = new PipeConverter();
					} else {
						 converter = new PipeObservationConverter<T>(new SerializableDataConverter());
					}
					trans = new LocalTransmitter<T>(localMsgProcessor, converter, instancePort);
				} else {
					converter = new ObservationConverter(new SerializableDataConverter());
					trans = new TcpTransmitter<T>(socketFactory, converter, instancePort);
					((TcpTransmitter)trans).start();
				}
				entrance.setTransmitter(trans);
				return trans;
			}
			
			@Override
			public String getName() {
				return "TCPTransmitterLocator-" + port;
			}
		};
	}
	
	@Override
	public void removeReceiver(Identifier id) {
		this.messageProcessor.removeReceiver(id);
		this.localMsgProcessor.removeReceiver(id);
	}
}
