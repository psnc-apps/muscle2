/*
 * 
 */

package muscle.net;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import muscle.utilities.parallelism.SafeThread;

/**
 * Handles connections over a socket.
 * 
 * By overriding executeProtocol(Socket), you can send
 * and receive messages over a socket. This function will repeatedly be called for every
 * accepted socket, until the dispose() function is called.
 * 
 * @author Joris Borgdorff
 */
public abstract class AbstractConnectionHandler<T> extends SafeThread {
	public final static String PROP_NUM_CONNECTIONS = "muscle.net.num_connections";
	protected final ServerSocket ss;
	protected final T listener;
	protected final ExecutorService executor;
	private final static Logger logger = Logger.getLogger(AbstractConnectionHandler.class .getName());

	public AbstractConnectionHandler(ServerSocket ss, T listener) {
		super("ConnectionHandler-" + ss);
		this.ss = ss;
		this.listener = listener;
		String prop_num_connections = System.getProperty(PROP_NUM_CONNECTIONS);
		int numConnections = prop_num_connections == null ? 10 : Integer.valueOf(prop_num_connections);
		executor = Executors.newFixedThreadPool(numConnections);
	}

	@Override
	protected final synchronized void handleInterruption(InterruptedException ex) {
		if (isDisposed()) {
			logger.log(Level.FINE, "ConnectionHandler {0} finished.", this.getClass());		 
		} else {
			logger.log(Level.WARNING, "ConnectionHandler interrupted", ex);
		}
	}
	
	@Override
	protected final void execute() throws InterruptedException {
		try {
			Socket s = this.ss.accept();
			logger.log(Level.FINE, "Accepted connection from: {0}", s.getRemoteSocketAddress());
			executor.submit(this.createProtocolHandler(s));
		} catch (IOException iox) {
			if (!isDisposed())
				logger.log(Level.SEVERE, "ConnectionHandler could not accept connection.", iox);
			// Else, we're closing the serversocket ourselves.
		}
	}
	
	protected abstract Callable<?> createProtocolHandler(Socket s);
	
	public synchronized void dispose() {
		super.dispose();
		try {
			this.ss.close();
		} catch (IOException ex) {
			logger.log(Level.SEVERE, "ServerSocket could not be closed.", ex);
		}
		executor.shutdown();
	}
}
