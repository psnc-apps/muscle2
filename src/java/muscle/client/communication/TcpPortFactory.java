/*
* Copyright 2008, 2009 Complex Automata Simulation Technique (COAST) consortium
* Copyright 2010-2013 Multiscale Applications on European e-Infrastructures (MAPPER) project
*
* GNU Lesser General Public License
* 
* This file is part of MUSCLE (Multiscale Coupling Library and Environment).
* 
* MUSCLE is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
* 
* MUSCLE is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Lesser General Public License for more details.
* 
* You should have received a copy of the GNU Lesser General Public License
* along with MUSCLE.  If not, see <http://www.gnu.org/licenses/>.
*/
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package muscle.client.communication;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import muscle.client.instance.ConduitEntranceControllerImpl;
import muscle.client.instance.ConduitExitControllerImpl;
import muscle.core.kernel.InstanceController;
import muscle.core.model.Observation;
import muscle.exception.ExceptionListener;
import muscle.id.Identifier;
import muscle.id.PortalID;
import muscle.id.Resolver;
import muscle.net.SocketFactory;
import muscle.util.concurrency.NamedCallable;
import muscle.util.serialization.BasicMessageConverter;
import muscle.util.serialization.DataConverter;
import muscle.util.serialization.ObservationConverter;
import muscle.util.serialization.PipeConverter;
import muscle.util.serialization.PipeObservationConverter;
import muscle.util.serialization.SerializableDataConverter;

/**
 *
 * @author Joris Borgdorff
 */
public class TcpPortFactory extends PortFactory {
	private final SocketFactory socketFactory;
	private final LocalDataHandler localMsgProcessor;
	private final static Logger logger = Logger.getLogger(TcpPortFactory.class.getName());
		
	public TcpPortFactory(Resolver res, ExceptionListener listener, SocketFactory sf, IncomingMessageProcessor globalMsgProcessor, LocalDataHandler localMsgProcessor) {
		super(res, listener, globalMsgProcessor);
		this.localMsgProcessor = localMsgProcessor;
		this.socketFactory = sf;
	}

	@Override
	protected <T extends Serializable> NamedCallable<Receiver<T, ?>> getReceiverTask(final InstanceController ic, final ConduitExitControllerImpl<T> exit, final PortalID port) {
		return new NamedCallable<Receiver<T,?>>() {
			// Datatypes are not filled in Java Generics, because we don't know
			// them yet: they depend on the messages sent.
			@Override @SuppressWarnings({"unchecked", "rawtypes"})
			public Receiver<T, ?> call() throws Exception {
				exit.start();
				try {
					if (!portWillActivate(port)) {
						throw new IllegalStateException("Port already shut down");
					}
					if (!(exit instanceof Receiver)) {
						throw new IllegalArgumentException("ConduitExitController must be a receiver");
					}
				// We need to generate an exception, to make sure that the
				// exception stack gets activated. It doesn't matter which
				} catch (Exception ex) {
					logger.log(Level.SEVERE, "Port {0} for {1} will not activate. Aborting.", new Object[]{port, exit});
					ic.fatalException(ex);
					throw ex;
				}
				PortalID instancePort = port;
				
				Receiver recv = (Receiver)exit;
				DataConverter converter;
				
				if (resolver.isLocal(instancePort)) {
					// Local data is simply read without modification
					converter = new PipeConverter();
					localMsgProcessor.addReceiver(exit.getIdentifier(), recv);				
				} else {
					// Remote data is converted frpm serialized format and wrapped in a message
					converter = new BasicMessageConverter(new SerializableDataConverter());
					messageProcessor.addReceiver(exit.getIdentifier(), recv);				
				}
				recv.setDataConverter(converter);
				
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
			// Datatypes are not filled in Java Generics, because we don't know
			// them yet: they depend on the messages sent.
			@Override @SuppressWarnings({"unchecked", "rawtypes"})
			public Transmitter<T, ?> call() throws Exception {
				entrance.start();
				try {
					if (!resolvePort(port)) {
						throw new IllegalStateException("Port was not resolved");
					}
				// We need to generate an exception, to make sure that the
				// exception stack gets activated. It doesn't matter which
				} catch (Exception ex) {
					logger.log(Level.SEVERE, "Could not resolve port {0} for {1}. Aborting.", new Object[]{port, entrance});
					ic.fatalException(ex);
					throw ex;
				}
				PortalID instancePort = port;

				Transmitter trans;
				DataConverter converter;
				if (resolver.isLocal(instancePort)) {
					if (shared) {
						converter = new PipeConverter<Observation<T>>();
						entrance.setSharedData();
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
				logger.log(Level.FINE, "<{0}> is now attached.", entrance);
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
