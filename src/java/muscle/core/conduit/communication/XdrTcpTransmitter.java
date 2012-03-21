/*
 * 
 */
package muscle.core.conduit.communication;

import java.io.IOException;
import java.io.Serializable;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import muscle.core.ident.InstanceID;
import muscle.core.ident.Location;
import muscle.core.ident.PortalID;
import muscle.core.ident.TcpLocation;
import muscle.core.messaging.Observation;
import muscle.core.messaging.signal.DetachConduitSignal;
import muscle.core.messaging.signal.Signal;
import muscle.core.messaging.signal.SignalEnum;
import muscle.net.SocketFactory;
import org.acplt.oncrpc.*;
import utilities.data.SerializableData;

/**
 *
 * @author Joris Borgdorff
 */
public class XdrTcpTransmitter<T extends Serializable> extends AbstractCommunicatingPoint<Observation<T>, Observation<SerializableData>,InstanceID,PortalID<InstanceID>> implements Transmitter<T, Observation<SerializableData>,InstanceID,PortalID<InstanceID>> {
	private final SocketFactory sockets;
	private Socket socket;
	private final static Logger logger = Logger.getLogger(XdrTcpTransmitter.class.getName());
	
	public XdrTcpTransmitter(SocketFactory sf) {
		this.sockets = sf;
		this.socket = null;
	}

	public void transmit(Observation<T> obs) {
		if (converter == null) {
			throw new IllegalStateException("Can not send message without serialization");
		}
		sendMessage(converter.serialize(obs), null);
	}

	public void signal(Signal signal) {
		sendMessage(null, signal);
	}
	
	private void sendMessage(Observation<SerializableData> obs, Signal signal) {
		if (socket == null) {
			socket = sockets.createSocket();
			Location loc = portalID.getLocation();
			try {
				socket.connect(((TcpLocation)loc).getSocketAddress());
			} catch (IOException ex) {
				Logger.getLogger(XdrTcpTransmitter.class.getName()).log(Level.SEVERE, "Failed to deliver message to " + portalID, ex);
				socket = null;
				return;
			}
		}
		
		try {
			XdrEncodingStream xdrOut;
			XdrDecodingStream xdrIn = new XdrTcpDecodingStream(socket, 1024);
			if (obs != null) {
				if (logger.isLoggable(Level.FINEST))
					logger.log(Level.FINEST, "Sending data message of type {0} to {1}", new Object[] {obs.getData().getType(), portalID});
				xdrOut = new XdrTcpEncodingStream(socket, obs.getData().getSize()+256);
				xdrOut.xdrEncodeInt(XdrDataProtocol.OBSERVATION.ordinal());
				xdrOut.xdrEncodeString(portalID.getName());
				xdrOut.xdrEncodeInt(portalID.getType().ordinal());
				xdrOut.xdrEncodeDouble(obs.getTimestamp().doubleValue());
				xdrOut.xdrEncodeDouble(obs.getNextTimestamp().doubleValue());
				obs.getData().encodeXdrData(xdrOut);
				xdrOut.endEncoding();
			}
			else if (signal != null) {
				SignalEnum sig;
				if (signal instanceof DetachConduitSignal) {
					sig = SignalEnum.DETACH_CONDUIT;
				} else {
					return;
				}
				logger.log(Level.FINEST, "Sending signal {0} to {1}", new Object[] {sig, portalID});

				xdrOut = new XdrTcpEncodingStream(socket, 1024);
			
				xdrOut.xdrEncodeInt(XdrDataProtocol.SIGNAL.ordinal());
				xdrOut.xdrEncodeString(portalID.getName());
				xdrOut.xdrEncodeInt(portalID.getType().ordinal());
				xdrOut.xdrEncodeInt(sig.ordinal());
				xdrOut.endEncoding();
			}
			xdrIn.beginDecoding();
			boolean success = xdrIn.xdrDecodeBoolean();
			if (success) {
				logger.log(Level.FINER, "Message succesfully sent to {0}.", portalID);
			} else {
				logger.log(Level.SEVERE, "Message unsuccesfully sent to {0}.", portalID);
			}
			
		} catch (OncRpcException ex) {
			logger.log(Level.SEVERE, "XDR failure to send message to {0}: {1}", new Object[]{portalID, ex});
		} catch (IOException ex) {
			logger.log(Level.SEVERE, "I/O failure to send message to {0}: {1}", new Object[]{portalID, ex});
		}
	}
	
	public void dispose() {
		if (socket != null) {
			try {
				socket.close();
			} catch (IOException ex) {
				Logger.getLogger(XdrTcpTransmitter.class.getName()).log(Level.SEVERE, null, ex);
			}
			socket = null;
		}
		super.dispose();
	}
}
