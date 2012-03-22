/*
 * 
 */

package muscle.core.ident;

import java.net.InetAddress;
import java.net.InetSocketAddress;

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

	public TcpLocation(InetSocketAddress localSocketAddress) {
		this(localSocketAddress.getAddress(), localSocketAddress.getPort());
	}
	
	public InetSocketAddress getSocketAddress() {
		return new InetSocketAddress(addr.getHostAddress(), port);
	}
	
	public InetAddress getAddress() {
		return addr;
	}

	public int getPort() {
		return port;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !this.getClass().equals(o.getClass())) return false;
		
		return addr.equals(((TcpLocation)o).addr) && port == ((TcpLocation)o).port;
	}
	
	@Override
	public int hashCode() {
		int hash = 5;
		hash = 59 * hash + (this.addr != null ? this.addr.hashCode() : 0);
		hash = 59 * hash + this.port;
		return hash;
	}
}
