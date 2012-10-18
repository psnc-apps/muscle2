/*
 * 
 */
package muscle.net;

import java.io.IOException;
import java.net.*;
import java.util.Enumeration;
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
				logger.log(Level.FINE, "Trying to bind on port: {0}", i);
				ss = new ServerSocket(i, backlog, addr);
				break;
			} catch (SocketException ex) {
				logger.log(Level.FINE, "Failed to bind to port: " + i, ex);
			}
		}
		
		if (ss == null) {
			throw new BindException("Failed to bind to ports between " + portMin + " and " + portMax + ", inclusive.");
		}
		else {
			return ss;
		}
	}
	
	public static InetAddress getMuscleHost() throws UnknownHostException {
		String addrStr = System.getProperty("muscle.net.bindaddr");
		InetAddress addr = null;
		if (addrStr != null) {
			try {
				addr = InetAddress.getByName(addrStr);
			} catch (UnknownHostException e) {
				logger.log(Level.WARNING, "Hostname {0} is not known. Using default host instead.", addrStr);
			} catch (SecurityException e) {					
				logger.log(Level.WARNING, "Hostname " + addrStr + " is not allowed not be determined. Using default host instead.", e);
			}
		}

		String interfaceStr = System.getProperty("muscle.net.bindinf");
		if (interfaceStr != null) {
			try {
				NetworkInterface inf = NetworkInterface.getByName(interfaceStr);
				if (inf == null) {
					logger.log(Level.WARNING, "Interface {0} could not be used. Using localhost instead.", interfaceStr);
				} else {
					Enumeration<InetAddress> addrs = inf.getInetAddresses();
					if (addrs.hasMoreElements()) {
						if (addr != null) {
							boolean hasAddr = false;
							while (addrs.hasMoreElements()) {
								InetAddress iaddr = addrs.nextElement();
								if (iaddr.equals(addr)) {
									hasAddr = true;
									break;
								}
							}
							if (!hasAddr) {
								logger.log(Level.WARNING, "Given address {0} does not match interface {1}. Using {1}.", new Object[] {addrStr, interfaceStr});
								addr = null;
							}
						}
						if (addr == null) {
							addr = inf.getInetAddresses().nextElement();
						}
					}
					else {
						logger.log(Level.WARNING, "Interface {0} does not have an IP address. Using localhost instead.", interfaceStr);
					}
				}
			} catch (SocketException ex) {
				logger.log(Level.WARNING, "Interface " + addrStr + " could not be used. Using localhost instead.", ex);
			}
		}
		if (addr == null) {
			try {
				addr = InetAddress.getLocalHost();
			} catch (UnknownHostException ex) {
				logger.severe("Could not use local host for networking. Please specify a --bindaddr");
				throw ex;
			}
		}
		
		return addr;
	}
}
