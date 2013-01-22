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
import muscle.id.Resolver;
import muscle.net.AbstractConnectionHandler;
import muscle.util.concurrency.NamedCallable;

/**
 *
 * @author Joris Borgdorff
 */
public class DataConnectionHandler extends AbstractConnectionHandler<LocalManager,Boolean> implements IncomingMessageProcessor {
	private final Resolver resolver;
	private final Map<Identifier,Receiver> receivers;
	private final static Logger logger = Logger.getLogger(DataConnectionHandler.class.getName());

	public DataConnectionHandler(ServerSocket ss, Resolver res) {
		super(ss, LocalManager.getInstance());
		logger.log(Level.CONFIG, "Listening for data connections on {0}:{1}", new Object[]{ss.getInetAddress().getHostAddress(), ss.getLocalPort()});
		this.resolver = res;
		this.receivers = new ConcurrentHashMap<Identifier,Receiver>();
	}
	
	@Override
	protected NamedCallable<Boolean> createProtocolHandler(Socket s) {
		return new TcpIncomingMessageHandler(s, receivers, resolver, this);
	}
	
	void result(Boolean bool) {
		if (bool != null && !bool.booleanValue() && !isDisposed()) {
			logger.log(Level.SEVERE, "Could not receive message, message lost. Aborting.");
			LocalManager.getInstance().shutdown(6);
		}
	}

	@Override
	public void addReceiver(Identifier id, Receiver recv) {
		receivers.put(id, recv);
		logger.log(Level.FINER, "Added receiver {0} to receivers", id);
	}
	
	@Override
	public void dispose() {
		super.dispose();
		for (Receiver recv : receivers.values()) {
			recv.dispose();
		}
	}

	@Override
	public void removeReceiver(Identifier id) {
		this.receivers.remove(id);
	}
}
