/*
 * 
 */
package muscle.client.communication;

import java.io.IOException;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import muscle.client.communication.message.DetachConduitSignal;
import muscle.client.communication.message.Signal;
import muscle.client.communication.message.SignalEnum;
import muscle.client.id.TcpLocation;
import muscle.id.InstanceID;
import muscle.id.PortalID;
import muscle.core.model.Observation;
import muscle.net.AliveSocket;
import muscle.net.SocketFactory;
import muscle.util.data.SerializableData;
import muscle.util.serialization.SerializerWrapper;

/**
 *
 * @author Joris Borgdorff
 */
public class TcpTransmitter<T extends Serializable> extends AbstractCommunicatingPoint<Observation<T>, Observation<SerializableData>,InstanceID,PortalID<InstanceID>> implements Transmitter<T, Observation<SerializableData>,InstanceID,PortalID<InstanceID>> {
	private AliveSocket liveSocket;
	private final static Logger logger = Logger.getLogger(TcpTransmitter.class.getName());
	private final static long socketKeepAlive = 5000*1000;
	private final SocketFactory socketFactory;
	
	public TcpTransmitter(SocketFactory sf) {
		this.socketFactory = sf;
		this.liveSocket = null;
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
		send(converter.serialize(obs), null);
	}

	// Synchronized: can only transmit one signal or message at a time.
	public synchronized void signal(Signal signal) {
		if (this.isDisposed()) {
			if (!(signal instanceof DetachConduitSignal))
				logger.log(Level.WARNING, "Transmitter is disposed of; unable to send signal {0} to {1}", new Object[] {signal, portalID});

			return;
		}
		send(null, signal);
	}
	
	private void send(Observation<SerializableData> obs, Signal signal) {
		boolean sent = false;
		for (int tries = 1; !sent && tries <= 3; tries++) {
			if (liveSocket.lock()) {
				try {
					SerializerWrapper out = liveSocket.getOutput();

					if (obs != null) {
						sendMessage(out, obs);
					}
					else if (signal != null) {
						sendSignal(out, signal);
					}
					sent = true;
				} catch (IOException ex) {
					logger.log(Level.SEVERE, "I/O failure to send message to " + portalID + "; tried " + tries + "/3 times.", ex);
				} catch (Exception ex) {
					logger.log(Level.SEVERE, "Unexpected failure to send message to " + portalID + "; tried " + tries + "/3 times.", ex);
				} finally {
					liveSocket.unlock();
				}
			}
		}
	}
	
	private void sendMessage(SerializerWrapper out, Observation<SerializableData> obs) throws IOException {
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
	
	private void sendSignal(SerializerWrapper out, Signal signal) throws IOException {
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
	
	public synchronized void dispose() {
		super.dispose();
		liveSocket.dispose();
	}
}
