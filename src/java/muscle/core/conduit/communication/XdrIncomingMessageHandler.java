/*
 * 
 */

package muscle.core.conduit.communication;

import java.io.IOException;
import java.net.Socket;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import muscle.core.ident.IDType;
import muscle.core.ident.Identifier;
import muscle.core.ident.Resolver;
import muscle.core.ident.ResolverFactory;
import muscle.core.messaging.BasicMessage;
import muscle.core.messaging.Timestamp;
import muscle.core.messaging.signal.DetachConduitSignal;
import muscle.core.messaging.signal.Signal;
import muscle.core.messaging.signal.SignalEnum;
import muscle.net.XdrProtocolHandler;
import org.acplt.oncrpc.OncRpcException;
import org.acplt.oncrpc.XdrDecodingStream;
import org.acplt.oncrpc.XdrEncodingStream;
import utilities.data.SerializableData;

/**
 *
 * @author Joris Borgdorff
 */
public class XdrIncomingMessageHandler extends XdrProtocolHandler<Boolean,Map<Identifier,TcpReceiver<?>>> {
	private final Resolver resolver;
	private final static Logger logger = Logger.getLogger(XdrIncomingMessageHandler.class.getName());
	
	public XdrIncomingMessageHandler(Socket s, Map<Identifier,TcpReceiver<?>> receivers, ResolverFactory rf) throws InterruptedException {
		super(s, receivers);
		this.resolver = rf.getResolver();
	}
	
	@Override
	protected Boolean executeProtocol(XdrDecodingStream xdrIn, XdrEncodingStream xdrOut) throws OncRpcException, IOException {
		xdrIn.beginDecoding();
		XdrDataProtocol proto = XdrDataProtocol.values()[xdrIn.xdrDecodeInt()];
		String owner_name = xdrIn.xdrDecodeString();
		IDType idType = IDType.values()[xdrIn.xdrDecodeInt()];
		Identifier recipient = this.resolver.getIdentifier(owner_name, idType);

		switch (proto) {
			case OBSERVATION: {
				Timestamp time = new Timestamp(xdrIn.xdrDecodeDouble());
				Timestamp nextTime = new Timestamp(xdrIn.xdrDecodeDouble());
				SerializableData data = SerializableData.parseXdrData(xdrIn);
				BasicMessage<SerializableData> msg = new BasicMessage<SerializableData>(data, time, nextTime, recipient);
				logger.log(Level.FINEST, "Message for {0} received with type {1}, size {2}, at time {3}.", new Object[]{recipient, data.getType(), data.getSize(), time});
				
				listener.get(recipient).put(msg);
			} break;
			case SIGNAL: {
				SignalEnum sigEnum = SignalEnum.values()[xdrIn.xdrDecodeInt()];
				Signal sig = null;
				switch (sigEnum) {
					case DETACH_CONDUIT:
						sig = new DetachConduitSignal();
						break;
					default:
						logger.log(Level.WARNING, "Unrecognized signal {0} received for {1}.", new Object[]{sigEnum, recipient});
				}
				listener.get(recipient).putSignal(sig);
			} break;
			default:
				return Boolean.FALSE;
		}
		return Boolean.TRUE;
	}
}
