/*
 * 
 */

package muscle.net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author Joris Borgdorff
 */
public class LocalSocketFactory extends SocketFactory {
	@Override
	public Socket createSocket() {
		return new Socket();
	}

	@Override
	public ServerSocket createServerSocket(int port, int backlog, InetAddress addr) throws IOException {
		if (port == 0) {
			return createServerSocketInRange(backlog, addr);
		} else {
			return new ServerSocket(port, backlog, addr);
		}
	}
}
