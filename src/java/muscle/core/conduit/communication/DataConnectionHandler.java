/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package muscle.core.conduit.communication;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import muscle.core.ident.Identifier;
import muscle.core.ident.ResolverFactory;
import muscle.net.AbstractConnectionHandler;

/**
 *
 * @author Joris Borgdorff
 */
public class DataConnectionHandler extends AbstractConnectionHandler<Map<Identifier,Receiver>> implements IncomingMessageProcessor {
	private final ResolverFactory resolverFactory;
	private final static Logger logger = Logger.getLogger(DataConnectionHandler.class.getName());

	public DataConnectionHandler(ServerSocket ss, ResolverFactory rf) {
		super(ss, new ConcurrentHashMap<Identifier,Receiver>());
		logger.log(Level.INFO, "Listening for data connections on {0}.", ss);
		this.resolverFactory = rf;
	}
	
	@Override
	protected Callable<?> createProtocolHandler(Socket s) {
		try {
			return new TcpIncomingMessageHandler(s, listener, resolverFactory, this);
		} catch (InterruptedException ex) {
			Logger.getLogger(DataConnectionHandler.class.getName()).log(Level.SEVERE, null, ex);
			return null;
		}
	}
	
	void resubmit(TcpIncomingMessageHandler c) {
		executor.submit(c);
	} 

	@Override
	public void addReceiver(Identifier id, Receiver recv) {
		listener.put(id, recv);
		logger.log(Level.FINER, "Added receiver {0} to receivers", id);
	}
	
	@Override
	public void dispose() {
		super.dispose();
		for (Receiver recv : listener.values()) {
			recv.dispose();
		}
	}

	@Override
	public void removeReceiver(Identifier id) {
		this.listener.remove(id);
	}
}
