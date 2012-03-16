/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package muscle.net;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.Callable;
import muscle.core.conduit.communication.TcpReceiver;
import muscle.core.ident.Identifier;

/**
 *
 * @author Joris Borgdorff
 */
public class DataConnectionHandler extends AbstractConnectionHandler<Map<Identifier,TcpReceiver>> {

	public DataConnectionHandler(ServerSocket ss, Map<Identifier,TcpReceiver> listeners) {
		super(ss, listeners);
	}
	
	@Override
	protected Callable<?> createProtocolHandler(Socket s) {
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
