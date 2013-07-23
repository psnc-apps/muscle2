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
 * 
 */

package muscle.client.communication.message;

import java.io.IOException;
import java.net.Socket;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import muscle.client.communication.Receiver;
import muscle.client.communication.TcpDataProtocol;
import muscle.core.model.Observation;
import muscle.core.model.Timestamp;
import muscle.id.IDType;
import muscle.id.Identifier;
import muscle.id.Resolver;
import muscle.net.ProtocolHandler;
import muscle.util.data.SerializableData;
import muscle.util.serialization.DeserializerWrapper;
import muscle.util.serialization.SerializerWrapper;

/**
 *
 * @author Joris Borgdorff
 */
public class TcpIncomingMessageHandler extends ProtocolHandler<Boolean,Map<Identifier,Receiver>> {
	private final static Logger logger = Logger.getLogger(TcpIncomingMessageHandler.class.getName());
	private final static SignalEnum[] signals = SignalEnum.values();
	private final DataConnectionHandler connectionHandler;
	private final Resolver resolver;
	
	public TcpIncomingMessageHandler(Socket s, Map<Identifier,Receiver> receivers, Resolver res, DataConnectionHandler handler) {
		super(s, receivers, false, true, 3);
		this.resolver = res;
		this.connectionHandler = handler;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	protected Boolean executeProtocol(DeserializerWrapper in, SerializerWrapper out) throws IOException {
		boolean success = false;

		in.refresh();
		TcpDataProtocol magic = TcpDataProtocol.valueOf(in.readInt());
		if (magic == TcpDataProtocol.CLOSE) {
			this.socket.close();
			logger.finer("Closing stale incoming socket.");
			return true;
		}
		if (magic != TcpDataProtocol.MAGIC_NUMBER) {
			logger.log(Level.WARNING, "Unrecognized protocol for data messages;\n[\t\t] the wrong manager address may have been specified");
			out.writeInt(TcpDataProtocol.ERROR.intValue());
			out.flush();
			this.socket.close();
			return null;
		}
		// Protocol not executed.
		TcpDataProtocol proto = TcpDataProtocol.valueOf(in.readInt());
		boolean shouldResubmit = true;
		
		if (proto != TcpDataProtocol.ERROR) {
			String owner_name = in.readString();
			IDType idType = IDType.values()[in.readInt()];
			Identifier recipient = resolver.getIdentifier(owner_name, idType);

			switch (proto) {
				case OBSERVATION: {
					Timestamp time = new Timestamp(in.readDouble());
					Timestamp nextTime = new Timestamp(in.readDouble());
					SerializableData data = SerializableData.parseData(in);
					Observation<SerializableData> obs = new Observation<SerializableData>(data, time, nextTime, true);
					BasicMessage<SerializableData> msg = new BasicMessage<SerializableData>(obs, recipient);
					logger.log(Level.FINEST, "Message for {0} received with type {1}, size {2}, at time {3}.", new Object[]{recipient, data.getType(), data.getSize(), time});
					Receiver recv = listener.get(recipient);
					if (recv == null) {
						logger.log(Level.SEVERE, "No receiver registered for message for {0} received with type {1}, size {2}, at time {3}.", new Object[]{recipient, data.getType(), data.getSize(), time});	
					} else {
						recv.put(msg);
						success = true;					
					}
				} break;
				case SIGNAL: {
					Signal sig = getSignal(in.readInt(), recipient);

					if (sig != null) {
						if (sig instanceof DetachConduitSignal) {
							shouldResubmit = false;
						}
						Receiver recv = listener.get(recipient);
						if (recv == null) {
							// Ignore detach conduit if the submodel has already quit
							if (shouldResubmit) {
								logger.log(Level.WARNING, "No receiver registered for signal {1} intended for {0}.", new Object[]{recipient, sig});
							} else {
								success = true;
							}
						} else {
							recv.put(new BasicMessage<SerializableData>(sig,recipient));
							success = true;					
						}
					}
				} break;
				default:
					proto = TcpDataProtocol.ERROR;
					break;
			}
			in.cleanUp();
		}
	
		out.writeInt(proto.intValue());
		out.flush();
		
		if (proto == TcpDataProtocol.ERROR) {
			logger.log(Level.WARNING, "Unrecognized protocol for data messages;\n[\t\t] the wrong address may have been specified");
			this.socket.close();
		}
		else if (shouldResubmit) {
			this.connectionHandler.resubmit(this);
		} else {
			this.socket.close();
		}
		
		return success;
	}

	private static Signal getSignal(int sigNum, Identifier recipient) {
		if (sigNum < 0 || sigNum >= signals.length) {
			logger.log(Level.WARNING, "Unrecognized signal number {0} received for {1}.", new Object[]{sigNum, recipient});
		} else {
			switch (signals[sigNum]) {
				case DETACH_CONDUIT:
					return new DetachConduitSignal();
				default:
					logger.log(Level.WARNING, "Unrecognized signal {0} received for {1}.", new Object[]{signals[sigNum], recipient});
			}
		}
		return null;
	}

	@Override
	public String getName() {
		return "TcpIncomingMessageHandler";
	}
	
	@Override
	protected void handleThrowable(Throwable ex) {
		this.connectionHandler.result(false);
	}
	
	@Override
	protected void handleResult(Boolean bool) {
		this.connectionHandler.result(bool);
	}
}
