/*
 * 
 */
package muscle.core.conduit.communication;

import java.io.IOException;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import muscle.core.LocalManager;
import muscle.core.ident.InstanceID;
import muscle.core.ident.Location;
import muscle.core.ident.PortalID;
import muscle.core.ident.TcpLocation;
import muscle.core.messaging.Observation;
import muscle.core.messaging.signal.DetachConduitSignal;
import muscle.core.messaging.signal.Signal;
import muscle.core.messaging.signal.SignalEnum;
import muscle.net.SocketFactory;
import org.acplt.oncrpc.OncRpcException;
import org.acplt.oncrpc.XdrEncodingStream;
import org.acplt.oncrpc.XdrTcpEncodingStream;
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
			if (obs != null) {
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

				xdrOut = new XdrTcpEncodingStream(socket, 1024);
			
				xdrOut.xdrEncodeInt(XdrDataProtocol.SIGNAL.ordinal());
				xdrOut.xdrEncodeString(portalID.getName());
				xdrOut.xdrEncodeInt(portalID.getType().ordinal());
				xdrOut.xdrEncodeInt(sig.ordinal());
				xdrOut.endEncoding();
			}
			
			logger.finest("Sending response");
		} catch (OncRpcException ex) {
			Logger.getLogger(XdrTcpTransmitter.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IOException ex) {
			Logger.getLogger(XdrTcpTransmitter.class.getName()).log(Level.SEVERE, null, ex);
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
