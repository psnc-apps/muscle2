/*
 * 
 */

package muscle.manager;

import java.net.ServerSocket;
import java.net.Socket;
import muscle.net.AbstractConnectionHandler;

/**
 * Executes the server side of the SimulationManager protocol.
 * 
 * The protocol is executed as follows
 * @author Joris Borgdorff
 */
public class ManagerConnectionHandler extends AbstractConnectionHandler<SimulationManager> {
	public ManagerConnectionHandler(SimulationManager listener, ServerSocket ss) {
		super(ss, listener);
	}

	@Override
	protected SimulationManagerProtocolHandler createProtocolHandler(Socket s) {
		return new SimulationManagerProtocolHandler(s, this.listener);
	}
}
