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
import muscle.core.ident.PortalID;
import muscle.core.ident.TcpLocation;
import muscle.core.messaging.Observation;
import muscle.core.messaging.serialization.ConverterWrapperFactory;
import muscle.core.messaging.serialization.SerializerWrapper;
import muscle.core.messaging.signal.DetachConduitSignal;
import muscle.core.messaging.signal.Signal;
import muscle.core.messaging.signal.SignalEnum;
import muscle.net.AliveSocket;
import muscle.net.SocketFactory;
import utilities.data.SerializableData;

/**
 *
 * @author Joris Borgdorff
 */
public class TcpTransmitter<T extends Serializable> extends AbstractCommunicatingPoint<Observation<T>, Observation<SerializableData>,InstanceID,PortalID<InstanceID>> implements Transmitter<T, Observation<SerializableData>,InstanceID,PortalID<InstanceID>> {
	private AliveSocket liveSocket;
	private final static Logger logger = Logger.getLogger(TcpTransmitter.class.getName());
	private final static long socketKeepAlive = 5000*1000;
	private final SocketFactory socketFactory;
	private SerializerWrapper out;
	
	public TcpTransmitter(SocketFactory sf) {
		this.socketFactory = sf;
		this.liveSocket = null;
		this.out = null;
	}

	@Override
	public void setComplementaryPort(PortalID port) {
		super.setComplementaryPort(port);
		this.liveSocket = new AliveSocket(socketFactory, ((TcpLocation)port.getLocation()).getSocketAddress(), socketKeepAlive);
		this.liveSocket.start();
	}
	
	// Synchronized: can only transmit one signal or message at a time.
	public synchronized void transmit(Observation<T> obs) {
		if (converter == null) {
			throw new IllegalStateException("Can not send message without serialization");
		}
		if (this.isDisposed()) {
			logger.log(Level.WARNING, "Transmitter is disposed of; unable to send observation to {0}", portalID);
			return;
		}
		sendMessage(converter.serialize(obs), null);
	}

	// Synchronized: can only transmit one signal or message at a time.
	public synchronized void signal(Signal signal) {
		if (this.isDisposed()) {
			if (!(signal instanceof DetachConduitSignal))
				logger.log(Level.WARNING, "Transmitter is disposed of; unable to send signal {0} to {1}", new Object[] {signal, portalID});

			return;
		}
		sendMessage(null, signal);
	}
	
	private void sendMessage(Observation<SerializableData> obs, Signal signal) {
		try {
			boolean reloadSocket = liveSocket.lockSocket();
			
			if (reloadSocket || out == null) {
				Socket socket = liveSocket.getOrCreateSocket();
				out = ConverterWrapperFactory.getDataSerializer(socket);
			}
			if (obs != null) {
				if (logger.isLoggable(Level.FINEST))
					logger.log(Level.FINEST, "Sending data message of type {0} to {1}", new Object[] {obs.getData().getType(), portalID});
				out.writeInt(TcpDataProtocol.OBSERVATION.ordinal());
				out.writeString(portalID.getName());
				out.writeInt(portalID.getType().ordinal());
				out.writeDouble(obs.getTimestamp().doubleValue());
				out.writeDouble(obs.getNextTimestamp().doubleValue());
				obs.getData().encodeData(out);
				out.flush();
			}
			else if (signal != null) {
				SignalEnum sig;
				if (signal instanceof DetachConduitSignal) {
					sig = SignalEnum.DETACH_CONDUIT;
				} else {
					return;
				}
				logger.log(Level.FINEST, "Sending signal {0} to {1}", new Object[] {sig, portalID});

				out.writeInt(TcpDataProtocol.SIGNAL.ordinal());
				out.writeString(portalID.getName());
				out.writeInt(portalID.getType().ordinal());
				out.writeInt(sig.ordinal());
				out.flush();
			}
		} catch (IOException ex) {
			logger.log(Level.SEVERE, "I/O failure to send message to {0}: {1}", new Object[]{portalID, ex});
		} catch (Exception ex) {
			logger.log(Level.SEVERE, "Unexpected failure to send message to {0}: {1}", new Object[]{portalID, ex});
		} finally {
			liveSocket.unlockSocket();
		}
	}
	
	public synchronized void dispose() {
		super.dispose();
		liveSocket.dispose();
	}
}
