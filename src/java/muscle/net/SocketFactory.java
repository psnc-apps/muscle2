/*
 * 
 */
package muscle.net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public interface SocketFactory {
	public Socket createSocket();

	public ServerSocket createServerSocket(int port, int backlog, InetAddress addr) throws IOException;
}
