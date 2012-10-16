/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package muscle.client.communication.message;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import muscle.client.LocalManager;
import muscle.client.communication.Receiver;
import muscle.id.Identifier;
import muscle.id.ResolverFactory;
import muscle.net.AbstractConnectionHandler;
import muscle.util.concurrency.NamedCallable;

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
	protected NamedCallable<?> createProtocolHandler(Socket s) {
		try {
			return new TcpIncomingMessageHandler(s, listener, resolverFactory, this);
		} catch (InterruptedException ex) {
			Logger.getLogger(DataConnectionHandler.class.getName()).log(Level.SEVERE, null, ex);
			return null;
		}
	}
	
	void result(Boolean bool) {
		if (bool != null && !bool.booleanValue() && !isDisposed()) {
			logger.log(Level.SEVERE, "Could not receive message, message lost. Aborting.");
			LocalManager.getInstance().shutdown(6);
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
