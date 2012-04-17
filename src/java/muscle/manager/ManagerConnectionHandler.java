/*
 * 
 */

package muscle.manager;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import muscle.net.AbstractConnectionHandler;

/**
 * Executes the server side of the SimulationManager protocol.
 * 
 * The protocol is executed as follows
 * @author Joris Borgdorff
 */
public class ManagerConnectionHandler extends AbstractConnectionHandler<SimulationManager> {
	private final static Logger logger = Logger.getLogger(ManagerConnectionHandler.class.getName());
	private File addressFile;
	
	public ManagerConnectionHandler(SimulationManager listener, ServerSocket ss) {
		super(ss, listener);
		addressFile = null;
	}

	@Override
	protected SimulationManagerProtocolHandler createProtocolHandler(Socket s) {
		return new SimulationManagerProtocolHandler(s, this.listener);
	}
	
	public void dispose() {
		super.dispose();
	}
	
	void writeLocation() {
		String host = ss.getInetAddress().getHostAddress()  +":" + ss.getLocalPort();
		logger.log(Level.INFO, "Started the connection handler, listening on {0}", host);

		// Writing address so that it can automatically be read
		try {
			String pid = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
			FileWriter fw = new FileWriter("simulationmanager." + pid + ".address");
			fw.write(host);
			fw.close();
		} catch (IOException ex) {
			logger.log(Level.SEVERE, null, ex);
		}
	}
}
