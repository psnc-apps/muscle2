/*
 * 
 */

package muscle.net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import muscle.core.ident.Location;
import muscle.core.ident.TcpLocation;
import org.acplt.oncrpc.*;

/**
 * Handles a protocol using XDR serialization.
 * 
 * By overriding executeProtocol(XdrTcpDecodingStream xdrIn, XdrTcpEncodingStream xdrOut), you can send
 * and receive messages over a socket using XDR serialization. This class will repeatedly be created for every
 * accepted socket.
 * 
 * @author Joris Borgdorff
 */
public abstract class XdrProtocolHandler<T> implements Runnable {
	protected final T listener;
	private final Socket socket;
	private final static Logger logger = Logger.getLogger(XdrProtocolHandler.class.getName());
	private final boolean closeSocket;
	
	public XdrProtocolHandler(Socket s, T listener, boolean closeSocket) {
		this.listener = listener;
		this.socket = s;
		this.closeSocket = closeSocket;
	}

	public XdrProtocolHandler(Socket s, T listener) {
		this(s, listener, false);
	}
	
	@Override
	public void run() {
		XdrTcpDecodingStream xdrIn = null;
		XdrTcpEncodingStream xdrOut = null;
		try {
			xdrIn =  new XdrTcpDecodingStream(socket, 1024);
			xdrOut = new XdrTcpEncodingStream(socket, 1024);
			
			executeProtocol(xdrIn, xdrOut);
		} catch (OncRpcException ex) {
			logger.log(Level.SEVERE, "Could not encode/decode with XDR.", ex);
		} catch (IOException ex) {
			logger.log(Level.SEVERE, "Communication error; could not encode/decode XDR from socket.", ex);
		} catch (RuntimeException ex) {
			logger.log(Level.SEVERE, "Could not finish protocol due to an error.", ex);
		} finally {
			if (xdrIn != null) {
				try {
					xdrIn.close();
				} catch (OncRpcException ex) {
					logger.log(Level.SEVERE, "Failure to close XDR decoding", ex);
				} catch (IOException ex) {
					logger.log(Level.SEVERE, "Communication error in closing XDR decoding", ex);
				}
			}
			if (xdrOut != null) {
				try {
					xdrOut.close();
				} catch (OncRpcException ex) {
					logger.log(Level.SEVERE, "Failure to close XDR encoding", ex);
				} catch (IOException ex) {
					logger.log(Level.SEVERE, "Communication error in closing XDR encoding", ex);
				}
			}
			if (this.closeSocket) {
				try {
					this.socket.close();
				} catch (IOException ex) {
					logger.log(Level.SEVERE, "Failure to close socket after protocol has finished", ex);
				}
			}
		}
	}
	
	/**
	 * Execute a protocol over an XDR stream.
	 * It is the responsibility of the protocol handler to beginDecoding() and endEncoding(). All exceptions
	 * are handled by XdrProtocolHandler.
	 */
	protected abstract void executeProtocol(XdrDecodingStream xdrIn, XdrEncodingStream xdrOut) throws OncRpcException, IOException;

	/**
	 * Encodes a TcpLocation over an XDR stream.
	 * @param xdrOut a stream to encode over; endEncoding() will not be called over it.
	 * @param loc must be a TcpLocation
	 */
	protected static void encodeLocation(XdrEncodingStream xdrOut, Location loc) throws OncRpcException, IOException {
		if (!(loc instanceof TcpLocation)) {
			throw new IllegalArgumentException("Location belonging to identity is not a TcpLocation; can only encode TcpLocation");
		}
		TcpLocation tcpLoc = (TcpLocation)loc;

		byte[] addr = tcpLoc.getAddress().getAddress();		
		xdrOut.xdrEncodeByteVector(addr);
		xdrOut.xdrEncodeInt(tcpLoc.getPort());
	}

	/**
	 * Decodes a TcpLocation from an XDR stream.
	 * @param xdrIn a stream to decode from; beginEncoding() will not be called over it.
	 * @returns the TcpLocation that was transmitted.
	 */
	protected static TcpLocation decodeLocation(XdrDecodingStream xdrIn) throws OncRpcException, IOException {
		byte[] addr = xdrIn.xdrDecodeByteVector();
		InetAddress inetAddr = InetAddress.getByAddress(addr);
		int port = xdrIn.xdrDecodeInt();
		return new TcpLocation(inetAddr, port);
	}
}
