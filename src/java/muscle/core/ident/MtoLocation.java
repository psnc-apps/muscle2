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
}
