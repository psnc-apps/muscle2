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
import org.acplt.oncrpc.OncRpcException;
import org.acplt.oncrpc.XdrTcpDecodingStream;
import org.acplt.oncrpc.XdrTcpEncodingStream;

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
	
	public void run() {
		XdrTcpDecodingStream xdrIn = null;
		XdrTcpEncodingStream xdrOut = null;
		try {
			xdrIn =  new XdrTcpDecodingStream(socket, 64 * 1024);
			xdrOut = new XdrTcpEncodingStream(socket, 64 * 1024);
			
			xdrIn.beginDecoding();
			executeProtocol(xdrIn, xdrOut);

			logger.finest("Flushing response");
			xdrOut.endEncoding();
				
			logger.finest("Operation decoded.");
			xdrIn.endDecoding();
		} catch (OncRpcException ex) {
			logger.log(Level.SEVERE, "Could not encode/decode with XDR", ex);
		} catch (IOException ex) {
			logger.log(Level.SEVERE, "Communication error; could not encode/decode XDR from socket", ex);
		}
		finally {
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
	
	protected abstract void executeProtocol(XdrTcpDecodingStream xdrIn, XdrTcpEncodingStream xdrOut) throws OncRpcException, IOException;
	
	
	protected static void encodeLocation(XdrTcpEncodingStream xdrOut, Location loc) throws OncRpcException, IOException {
		if (!(loc instanceof TcpLocation)) {
			throw new IllegalArgumentException("Location belonging to identity is not a TcpLocation; can only encode TcpLocation");
		}
		TcpLocation tcpLoc = (TcpLocation)loc;

		byte[] addr = tcpLoc.getAddress().getAddress();		
		xdrOut.xdrEncodeByteVector(addr);
		xdrOut.xdrEncodeInt(tcpLoc.getPort());
	}

	protected static Location decodeLocation(XdrTcpDecodingStream xdrIn) throws OncRpcException, IOException {
		byte[] addr = xdrIn.xdrDecodeByteVector();
		InetAddress inetAddr = InetAddress.getByAddress(addr);
		int port = xdrIn.xdrDecodeInt();
		return new TcpLocation(inetAddr, port);
	}
}
