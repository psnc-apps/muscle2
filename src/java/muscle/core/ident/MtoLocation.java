/*
 * 
 */

package muscle.core.ident;

import java.net.InetAddress;

/**
 *
 * @author Joris Borgdorff
 */
public class MtoLocation extends TcpLocation {
	private final TcpLocation mtoLoc;

	public MtoLocation(InetAddress mtoAddr, int mtoPort, InetAddress localAddr, int localPort) {
		super(localAddr, localPort);
		mtoLoc = new TcpLocation(mtoAddr, mtoPort);
	}

	public TcpLocation getMtoTcpLocation() {
		return mtoLoc;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == null || !this.getClass().equals(o.getClass())) return false;
		
		return super.equals(o) && mtoLoc.equals(((MtoLocation)o).mtoLoc);
	}
	
	@Override
	public int hashCode() {
		int hash = 5;
		hash = 59 * hash + super.hashCode();
		hash = 59 * hash + (mtoLoc == null ? 0 : mtoLoc.hashCode());
		return hash;
	}
}
