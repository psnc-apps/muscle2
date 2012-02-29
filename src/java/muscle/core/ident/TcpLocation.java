/*
 * 
 */

package muscle.core.ident;

import java.net.InetAddress;

/**
 *
 * @author Joris Borgdorff
 */
public class TcpLocation implements Location {
	private final InetAddress addr;
	private final int port;
	
	public TcpLocation(InetAddress addr, int port) {
		this.addr = addr;
		this.port = port;
	}
	
	public InetAddress getAddress() {
		return addr;
	}

	public int getPort() {
		return port;
	}
}
