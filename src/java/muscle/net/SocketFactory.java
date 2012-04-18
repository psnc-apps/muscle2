/*
 * 
 */
package muscle.net;

import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class SocketFactory {
	private final static Logger logger = Logger.getLogger(SocketFactory.class.getName());

	protected static final String PROP_PORT_RANGE_MIN = "pl.psnc.mapper.muscle.portrange.min";
	protected static final String PROP_PORT_RANGE_MAX = "pl.psnc.mapper.muscle.portrange.max";

	protected int portMin = 9000;
	protected int portMax = 9500;

	public SocketFactory() {
		if (System.getProperty(PROP_PORT_RANGE_MIN) != null) {
			portMin = Integer.valueOf(System.getProperty(PROP_PORT_RANGE_MIN));
		}

		if (System.getProperty(PROP_PORT_RANGE_MAX) != null) {
			portMax = Integer.valueOf(System.getProperty(PROP_PORT_RANGE_MAX));
		}
	}
	
	public abstract Socket createSocket();

	public abstract ServerSocket createServerSocket(int port, int backlog, InetAddress addr) throws IOException;
	
	protected ServerSocket createServerSocketInRange(int backlog, InetAddress addr) throws IOException {
		ServerSocket ss = null;

		for (int i = portMin; i <= portMax; i++) {
			try {
				logger.log(Level.CONFIG, "Trying to bind on port: {0}", i);
				ss = new ServerSocket(i, backlog, addr);
				break;
			} catch (BindException ex) {
				logger.log(Level.FINE, "Failed to bind to port: {0}", ex);
			}
		}
		
		if (ss == null) {
			throw new BindException("Failed to bind to ports between " + portMin + " and " + portMax + ", inclusive.");
		}
		else {
			return ss;
		}
	}
}
