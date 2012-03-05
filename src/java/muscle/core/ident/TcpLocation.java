/*
 * 
 */

package muscle.core.ident;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

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
	
	public InetSocketAddress getSocketAddress() {
		return InetSocketAddress.createUnresolved(addr.getHostAddress(), port);
	}
	
	public InetAddress getAddress() {
		return addr;
	}

	public int getPort() {
		return port;
	}
}
