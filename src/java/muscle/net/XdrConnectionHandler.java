/*
 * 
 */

package muscle.net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.logging.Level;
import org.acplt.oncrpc.OncRpcException;
import org.acplt.oncrpc.XdrTcpDecodingStream;
import org.acplt.oncrpc.XdrTcpEncodingStream;

/**
 *
 * @author Joris Borgdorff
 */
public abstract class XdrConnectionHandler<T> extends AbstractConnectionHandler<T> {
	public XdrConnectionHandler(SocketFactory sf, InetAddress addr, T listener) throws IOException {
		super(sf, addr, listener);
	}
	
	protected void executeProtocol(Socket s) {
		XdrTcpDecodingStream xdrIn = null;
		XdrTcpEncodingStream xdrOut = null;
		try {
			xdrIn =  new XdrTcpDecodingStream(s, 64 * 1024);
			xdrOut = new XdrTcpEncodingStream(s, 64 * 1024);
			
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
		}
	}
	
	protected abstract void executeProtocol(XdrTcpDecodingStream xdrIn, XdrTcpEncodingStream xdrOut) throws OncRpcException, IOException;
}
