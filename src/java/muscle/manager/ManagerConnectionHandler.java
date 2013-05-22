/*
* Copyright 2008, 2009 Complex Automata Simulation Technique (COAST) consortium
* Copyright 2010-2013 Multiscale Applications on European e-Infrastructures (MAPPER) project
*
* GNU Lesser General Public License
* 
* This file is part of MUSCLE (Multiscale Coupling Library and Environment).
* 
* MUSCLE is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
* 
* MUSCLE is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Lesser General Public License for more details.
* 
* You should have received a copy of the GNU Lesser General Public License
* along with MUSCLE.  If not, see <http://www.gnu.org/licenses/>.
*/
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
public class ManagerConnectionHandler extends AbstractConnectionHandler<SimulationManager,Boolean> {
	private final static Logger logger = Logger.getLogger(ManagerConnectionHandler.class.getName());
	
	public ManagerConnectionHandler(SimulationManager listener, ServerSocket ss) {
		super(ss, listener);
	}

	@Override
	protected SimulationManagerProtocolHandler createProtocolHandler(Socket s) {
		return new SimulationManagerProtocolHandler(s, this.listener, this);
	}
	
	void writeLocation() {
		String hostport = ss.getInetAddress().getHostAddress()  +":" + ss.getLocalPort();
		logger.log(Level.INFO, "Started the connection handler, listening on {0}", hostport);

		// Writing address so that it can automatically be read
		try {
			String addrFileName = System.getProperty("java.io.tmpdir") + "/.muscle/simulationmanager.address";
			File flock = new File(addrFileName + ".lock");
			
			flock.createNewFile(); /* lock */
			
			FileWriter fw = new FileWriter(addrFileName);
			fw.write(hostport);
			fw.close();
			
			flock.delete(); /* unlock */
		} catch (IOException ex) {
			logger.log(Level.SEVERE, null, ex);
		}
	}
}
