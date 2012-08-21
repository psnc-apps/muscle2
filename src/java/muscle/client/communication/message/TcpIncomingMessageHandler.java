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
import muscle.core.model.Timestamp;
import muscle.id.IDType;
import muscle.id.Identifier;
import muscle.id.Resolver;
import muscle.id.ResolverFactory;
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
	private final static TcpDataProtocol[] msgType = TcpDataProtocol.values();
	private final DataConnectionHandler connectionHandler;
	private final Resolver resolver;
	
	public TcpIncomingMessageHandler(Socket s, Map<Identifier,Receiver> receivers, ResolverFactory rf, DataConnectionHandler handler) throws InterruptedException {
		super(s, receivers, false, true, 3);
		this.resolver = rf.getResolver();
		this.connectionHandler = handler;
	}
	
	@Override
	protected Boolean executeProtocol(DeserializerWrapper in, SerializerWrapper out) throws IOException {
		boolean success = false;

		in.refresh();
		int magic_number = in.readInt();
		if (magic_number == -1) {
			this.socket.close();
			logger.finer("Closing stale incoming socket.");
			return null;
		}
		if (magic_number != TcpDataProtocol.MAGIC_NUMBER) {
			logger.warning("Unrecognized protocol for data messages: " + magic_number + " != " + TcpDataProtocol.MAGIC_NUMBER);
			return null;
		}
		int protoNum = in.readInt();
		// Protocol not executed.
		if (protoNum < 0 || protoNum >= msgType.length) {
			logger.log(Level.WARNING, "Unrecognized message type number {0} received.", protoNum);
			return false;
		}
		
		TcpDataProtocol proto = msgType[protoNum];
		String owner_name = in.readString();
		IDType idType = IDType.values()[in.readInt()];
		Identifier recipient = resolver.getIdentifier(owner_name, idType);
		boolean shouldDetach = false;

		switch (proto) {
			case OBSERVATION: {
				Timestamp time = new Timestamp(in.readDouble());
				Timestamp nextTime = new Timestamp(in.readDouble());
				SerializableData data = SerializableData.parseData(in);
				BasicMessage<SerializableData> msg = new BasicMessage<SerializableData>(data, time, nextTime, recipient);
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
				shouldDetach = sig instanceof DetachConduitSignal;
				
				if (sig != null) {
					Receiver recv = listener.get(recipient);
					if (recv == null) {
						// Ignore detach conduit if the submodel has already quit
						if (!shouldDetach)
							logger.log(Level.WARNING, "No receiver registered for signal {1} intended for {0}.", new Object[]{recipient, sig});	
					} else {
						recv.put(new BasicMessage<SerializableData>(sig,recipient));
						success = true;					
					}
				}
			} break;
			default:
				break;
		}
		in.cleanUp();
	
		if (!shouldDetach) {
			this.connectionHandler.resubmit(this);
		}
		
		return success;
	}

	private Signal getSignal(int sigNum, Identifier recipient) {
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
}
