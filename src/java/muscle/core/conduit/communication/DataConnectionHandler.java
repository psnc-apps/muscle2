/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package muscle.core.conduit.communication;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import muscle.core.ident.Identifier;
import muscle.core.ident.ResolverFactory;
import muscle.net.AbstractConnectionHandler;

/**
 *
 * @author Joris Borgdorff
 */
public class DataConnectionHandler extends AbstractConnectionHandler<Map<Identifier,TcpReceiver<?>>> {
	private final ResolverFactory resolverFactory;

	public DataConnectionHandler(ServerSocket ss, Map<Identifier,TcpReceiver<?>> listeners, ResolverFactory rf) {
		super(ss, listeners);
		
		this.resolverFactory = rf;
	}
	
	@Override
	protected Callable<?> createProtocolHandler(Socket s) {
		try {
			return new XdrIncomingMessageHandler(s, listener, resolverFactory);
		} catch (InterruptedException ex) {
			Logger.getLogger(DataConnectionHandler.class.getName()).log(Level.SEVERE, "Could not handle incoming data message; no resolver was found.", ex);
			return null;
		}
	}
}