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
import muscle.core.messaging.signal.Signal;
import muscle.core.messaging.Observation;
import muscle.core.messaging.signal.DetachConduitSignal;
import muscle.core.messaging.signal.SignalEnum;
import muscle.net.SocketFactory;
import org.acplt.oncrpc.OncRpcException;
import org.acplt.oncrpc.XdrTcpEncodingStream;

/**
 *
 * @author Joris Borgdorff
 */
public class XdrTcpTransmitter<T extends Serializable> extends AbstractCommunicatingPoint<Observation<T>, Observation<byte[]>,InstanceID,PortalID<InstanceID>> implements Transmitter<T, Observation<byte[]>,InstanceID,PortalID<InstanceID>> {
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
		SignalEnum sig = null;
		if (signal instanceof DetachConduitSignal) {
			sig = SignalEnum.DETACH_CONDUIT;
		}
		if (sig != null)
			sendMessage(null, sig);
	}
	
	private void sendMessage(Observation<byte[]> obs, SignalEnum signal) {
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
		
		XdrTcpEncodingStream xdrOut = null;
		try {
			xdrOut = new XdrTcpEncodingStream(socket, 64 * 1024);
			
			if (obs != null) {
				xdrOut.xdrEncodeInt(XdrDataProtocol.OBSERVATION.ordinal());
				xdrOut.xdrEncodeDouble(obs.getTimestamp().doubleValue());
				xdrOut.xdrEncodeDouble(obs.getNextTimestamp().doubleValue());
				xdrOut.xdrEncodeByteVector(obs.getData());
			}
			else if (signal != null) {
				xdrOut.xdrEncodeInt(XdrDataProtocol.SIGNAL.ordinal());
				xdrOut.xdrEncodeInt(signal.ordinal());
			}
			
			logger.finest("Sending response");
			xdrOut.endEncoding();
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
