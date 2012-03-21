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
public class XdrIncomingMessageHandler extends XdrProtocolHandler<Boolean,Map<Identifier,Receiver>> {
	private final Resolver resolver;
	private final static Logger logger = Logger.getLogger(XdrIncomingMessageHandler.class.getName());
	private final static SignalEnum[] signals = SignalEnum.values();
	private final static XdrDataProtocol[] msgType = XdrDataProtocol.values();
	
	public XdrIncomingMessageHandler(Socket s, Map<Identifier,Receiver> receivers, ResolverFactory rf) throws InterruptedException {
		super(s, receivers);
		this.resolver = rf.getResolver();
	}
	
	@Override
	protected Boolean executeProtocol(XdrDecodingStream xdrIn, XdrEncodingStream xdrOut) throws OncRpcException, IOException {
		boolean success = false;

		xdrIn.beginDecoding();
		int protoNum = xdrIn.xdrDecodeInt();
		if (protoNum < 0 || protoNum >= msgType.length) {
			logger.log(Level.WARNING, "Unrecognized message type number {0} received.", protoNum);
		} else {
			XdrDataProtocol proto = msgType[protoNum];
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
					success = true;
				} break;
				case SIGNAL: {
					int sigNum = xdrIn.xdrDecodeInt();
					Signal sig = null;
					if (sigNum < 0 || sigNum >= signals.length) {
							logger.log(Level.WARNING, "Unrecognized signal number {0} received for {1}.", new Object[]{sigNum, recipient});
					} else {
						SignalEnum sigEnum = signals[sigNum];
						switch (sigEnum) {
							case DETACH_CONDUIT:
								sig = new DetachConduitSignal();
								break;
							default:
								logger.log(Level.WARNING, "Unrecognized signal {0} received for {1}.", new Object[]{sigEnum, recipient});
						}
						listener.get(recipient).putSignal(sig);
					}
					success = (sig != null);
				} break;
				default:
					break;
			}
		}
		xdrOut.xdrEncodeBoolean(success);
		xdrOut.endEncoding();
		
		return success;
	}
}
