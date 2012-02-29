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
public class LocalSocketFactory implements SocketFactory {
	@Override
	public Socket createSocket() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public ServerSocket createServerSocket(int port, int backlog, InetAddress addr) throws IOException {
		throw new UnsupportedOperationException("Not supported yet.");
	}
}
