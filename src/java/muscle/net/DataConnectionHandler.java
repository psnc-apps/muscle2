/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package muscle.net;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import muscle.core.conduit.communication.Receiver;
import muscle.core.conduit.communication.TcpReceiver;
import muscle.core.ident.Identifier;
import muscle.core.ident.InstanceID;
import muscle.core.ident.PortalID;
import muscle.core.messaging.Message;
import org.acplt.oncrpc.OncRpcException;
import org.acplt.oncrpc.XdrTcpDecodingStream;
import org.acplt.oncrpc.XdrTcpEncodingStream;

/**
 *
 * @author Joris Borgdorff
 */
public class DataConnectionHandler extends AbstractConnectionHandler<Map<Identifier,TcpReceiver>> {

	public DataConnectionHandler(ServerSocket ss, Map<Identifier,TcpReceiver> listeners) {
		super(ss, listeners);
	}
	
	@Override
	protected Runnable createProtocolHandler(Socket s) {
		throw new UnsupportedOperationException("Not supported yet.");
	}
	
//	private class XdrDataConnectionHandler extends XdrProtocolHandler {
//		XdrDataConnectionHandler() {
//			super();
//		} 
//		@Override
//		protected void executeProtocol(XdrTcpDecodingStream xdrIn, XdrTcpEncodingStream xdrOut) throws OncRpcException, IOException {
//			xdrIn.xd
//			throw new UnsupportedOperationException("Not supported yet.");
//		}
//	}
}
