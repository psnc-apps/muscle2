/*
 * 
 */

package muscle.net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import muscle.core.ident.Location;
import muscle.core.ident.TcpLocation;
import muscle.core.messaging.serialization.ConverterWrapperFactory;
import muscle.core.messaging.serialization.DeserializerWrapper;
import muscle.core.messaging.serialization.SerializerWrapper;

/**
 * Handles a protocol using serialization.
 * 
 * By overriding executeProtocol(XdrTcpDecodingStream xdrIn, XdrTcpEncodingStream xdrOut), you can send
 * and receive messages over a socket using XDR serialization. This class will repeatedly be created for every
 * accepted socket.
 * 
 * @author Joris Borgdorff
 */
public abstract class ProtocolHandler<S,T> implements Callable<S> {
	protected final T listener;
	protected final Socket socket;
	private final static Logger logger = Logger.getLogger(ProtocolHandler.class.getName());
	private final boolean closeSocket;
	private DeserializerWrapper in;
	private SerializerWrapper out;
	public final static int SIZE_BUFFER_IN = 1024*1024*10;
	public final static int SIZE_BUFFER_OUT = 1024;
	
	
	public ProtocolHandler(Socket s, T listener, boolean closeSocket) {
		this.listener = listener;
		this.socket = s;
		this.closeSocket = closeSocket;
		this.in = null;
		this.out = null;
	}

	public ProtocolHandler(Socket s, T listener) {
		this(s, listener, false);
	}
	
	@Override
	public S call() {
		S ret = null;
		try {
			if (in == null) {
				in = ConverterWrapperFactory.getControlDeserializer(socket);
			}
			if (out == null) {
				out = ConverterWrapperFactory.getDataSerializer(socket);
			}
			
			ret = executeProtocol(in, out);
		} catch (IOException ex) {
			logger.log(Level.SEVERE, "Communication error; could not encode/decode from socket.", ex);
		} catch (RuntimeException ex) {
			logger.log(Level.SEVERE, "Could not finish protocol due to an error.", ex);
		} finally {
			if (this.closeSocket) {
				try {
					this.socket.close();
				} catch (IOException ex) {
					logger.log(Level.SEVERE, "Failure to close socket after protocol has finished", ex);
				}
			}
		}
		return ret;
	}
	
	/**
	 * Execute a protocol over a serialized stream.
	 * It is the responsibility of the protocol handler to perform in.refresh() and out.flush(). All exceptions
	 * are handled by ProtocolHandler.
	 */
	protected abstract S executeProtocol(DeserializerWrapper in, SerializerWrapper out) throws IOException;

	/**
	 * Encodes a TcpLocation over a serialized stream.
	 * @param out a stream to encode over; flush() will not be called over it.
	 * @param loc must be a TcpLocation
	 */
	protected static void encodeLocation(SerializerWrapper out, Location loc) throws IOException {
		if (!(loc instanceof TcpLocation)) {
			throw new IllegalArgumentException("Location belonging to identity is not a TcpLocation; can only encode TcpLocation");
		}
		TcpLocation tcpLoc = (TcpLocation)loc;

		byte[] addr = tcpLoc.getAddress().getAddress();		
		out.writeByteArray(addr);
		out.writeInt(tcpLoc.getPort());
	}

	/**
	 * Decodes a TcpLocation from a serialized stream.
	 * @param in a stream to decode from; refresh() will not be called over it.
	 * @returns the TcpLocation that was transmitted.
	 */
	protected static TcpLocation decodeLocation(DeserializerWrapper in) throws IOException {
		byte[] addr = in.readByteArray();
		InetAddress inetAddr = InetAddress.getByAddress(addr);
		int port = in.readInt();
		return new TcpLocation(inetAddr, port);
	}
}
