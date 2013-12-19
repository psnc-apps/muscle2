/*
* Copyright 2008, 2009 Complex Automata Simulation Technique (COAST) consortium
* Copyright 2010-2013 Multiscale Applications on European e-Infrastructures (MAPPER) project
*
* GNU Lesser General Public License
* 
* This file is part of MUSCLE (Multiscale Coupling Library and Environment).
* 
* MUSCLE is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
* 
* MUSCLE is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Lesser General Public License for more details.
* 
* You should have received a copy of the GNU Lesser General Public License
* along with MUSCLE.  If not, see <http://www.gnu.org/licenses/>.
*/
/*
 * 
 */
package muscle.net;

import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class SocketFactory {
	private final static Logger logger = Logger.getLogger(SocketFactory.class.getName());

	private final static String PROP_PORT_RANGE_MIN = System.getProperty("pl.psnc.mapper.muscle.portrange.min");
	private final static String PROP_PORT_RANGE_MAX = System.getProperty("pl.psnc.mapper.muscle.portrange.max");
	protected final static String PROP_BIND_INTERFACE = System.getProperty("muscle.net.bindinf");
	protected final static String PROP_BIND_HOST = System.getProperty("muscle.net.bindaddr");

	protected final static int portMin = PROP_PORT_RANGE_MIN == null ? 9000 : Integer.valueOf(PROP_PORT_RANGE_MIN);
	protected final static int portMax = PROP_PORT_RANGE_MAX == null ? 9500 : Integer.valueOf(PROP_PORT_RANGE_MAX);

	public abstract Socket createSocket();

	public abstract ServerSocket createServerSocket(int port, int backlog, InetAddress addr) throws IOException;
	
	protected static ServerSocket createServerSocketInRange(int backlog, InetAddress addr) throws IOException {
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
		} else {
			return ss;
		}
	}
	
	public static InetAddress getMuscleHost() throws UnknownHostException {
		InetAddress addr = null;
		if (PROP_BIND_HOST != null) {
			try {
				addr = InetAddress.getByName(PROP_BIND_HOST);
			} catch (UnknownHostException e) {
				logger.log(Level.WARNING, "Hostname <{0}> is unknown. Using default host instead.", PROP_BIND_HOST);
			} catch (SecurityException e) {					
				logger.log(Level.WARNING, "Hostname <" + PROP_BIND_HOST + "> is not allowed not be determined. Using default host instead.", e);
			}
		}

		if (PROP_BIND_INTERFACE != null) {
			try {
				NetworkInterface inf = NetworkInterface.getByName(PROP_BIND_INTERFACE);
				if (inf == null) {
					logger.log(Level.WARNING, "Interface {0} could not be used. Using localhost instead.", PROP_BIND_INTERFACE);
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
								logger.log(Level.WARNING, "Given address {0} does not match interface {1}. Using {1}.", new Object[] {PROP_BIND_HOST, PROP_BIND_INTERFACE});
								addr = null;
							}
						}
						if (addr == null) {
							addr = inf.getInetAddresses().nextElement();
						}
					} else {
						logger.log(Level.WARNING, "Interface {0} does not have an IP address. Using localhost instead.", PROP_BIND_INTERFACE);
					}
				}
			} catch (SocketException ex) {
				logger.log(Level.WARNING, "Interface " + PROP_BIND_HOST + " could not be used. Using localhost instead.", ex);
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
