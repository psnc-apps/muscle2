/*
 * 
 */

package muscle.manager;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import muscle.net.AbstractConnectionHandler;
import muscle.net.CrossSocketFactory;

/**
 * Executes the server side of the SimulationManager protocol.
 * 
 * The protocol is executed as follows
 * @author Joris Borgdorff
 */
public class ManagerConnectionHandler extends AbstractConnectionHandler<SimulationManager> {
	public ManagerConnectionHandler(SimulationManager listener) throws UnknownHostException, IOException {
		super(new CrossSocketFactory().createServerSocket(0, 10, InetAddress.getByAddress(new byte[]{ 127, 0, 0, 1})), listener);
	}

	@Override
	protected Runnable createProtocolHandler(Socket s) {
		return new SimulationManagerProtocolHandler(s, this.listener);
	}
}
