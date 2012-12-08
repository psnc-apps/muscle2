/*
 * 
 */

package muscle.id;

import java.io.File;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import muscle.id.Location;
import muscle.util.FileTool;
import muscle.util.JVM;

/**
 *
 * @author Joris Borgdorff
 */
public class TcpLocation implements Location {
	private final InetAddress addr;
	private final int port;
	private final String tmpDir;
	
	public TcpLocation(InetAddress addr, int port, String tmpDir) {
		this.addr = addr;
		this.port = port;
		this.tmpDir = tmpDir;
	}

	public TcpLocation(InetSocketAddress localSocketAddress, String tmpDir) {
		this(localSocketAddress.getAddress(), localSocketAddress.getPort(), tmpDir);
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
	
	public String getTmpDir() {
		return this.tmpDir;
	}
	
	public void createSymlink(String name, TcpLocation local) {
		if (!tmpDir.equals(local.tmpDir) && !tmpDir.isEmpty()) {
			FileTool.createSymlink(JVM.ONLY.tmpFile(name), new File("../" + tmpDir));
		}
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !this.getClass().equals(o.getClass())) return false;
		
		return addr.equals(((TcpLocation)o).addr) && port == ((TcpLocation)o).port && tmpDir.equals(((TcpLocation)o).tmpDir);
	}
	
	@Override
	public int hashCode() {
		int hash = 5;
		hash = 59 * hash + this.addr.hashCode();
		hash = 59 * hash + this.port;
		hash = 59 * hash + this.tmpDir.hashCode();
		return hash;
	}
	
	@Override
	public String toString() {
		return "TcpLocation[" + this.addr + ":" + this.port + " <" + this.tmpDir + ">]";
	}
}
