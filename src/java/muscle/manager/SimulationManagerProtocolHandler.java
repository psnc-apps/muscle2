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
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package muscle.manager;

import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import muscle.id.InstanceID;
import muscle.id.Location;
import muscle.net.ProtocolHandler;
import muscle.util.serialization.DeserializerWrapper;
import muscle.util.serialization.ProtocolSerializer;
import muscle.util.serialization.SerializerWrapper;

/**
 *
 * @author Joris Borgdorff
 */
public class SimulationManagerProtocolHandler extends ProtocolHandler<Boolean,SimulationManager> {
	private final static Logger logger = Logger.getLogger(SimulationManagerProtocolHandler.class.getName());
	private final ManagerConnectionHandler handler;

	public SimulationManagerProtocolHandler(Socket s, SimulationManager listener, ManagerConnectionHandler handler) {
		// Use control for in and out
		super(s, listener, true, true, 3);
		this.handler = handler;
	}

	@Override
	protected Boolean executeProtocol(DeserializerWrapper in, SerializerWrapper out) throws IOException {
		final ProtocolSerializer<SimulationManagerProtocol> protocol = SimulationManagerProtocol.handler;
		boolean success = false;
		boolean resubmit;
		in.refresh();
		SimulationManagerProtocol magic_number = protocol.read(in);
		switch (magic_number) {
			case CLOSE:
				socket.close();
				return true;
			case MAGIC_NUMBER_KEEP_ALIVE:
				resubmit = true;
				break;
			case MAGIC_NUMBER:
				protocol.write(out, SimulationManagerProtocol.ERROR);
				out.flush();
				socket.close();
				logger.warning("Protocol for communicating MUSCLE information is not recognized.");
				return null;
			default:
				resubmit = false;
				break;
		}
		
		SimulationManagerProtocol proto = protocol.read(in);
		InstanceID id = new InstanceID(in.readString());
		
		switch (proto) {
			case LOCATE:
				protocol.write(out, proto);
				// Flush, to indicate that we are waiting to resolve the location
				out.flush();
				try {
					success = listener.resolve(id);
					out.writeBoolean(success);
					if (success) {
						encodeLocation(out, id.getLocation());
					}
				} catch (InterruptedException ex) {
					out.writeBoolean(false);
					logger.log(Level.SEVERE, "Could not resolve identifier", ex);
				}
				break;
			case REGISTER:
				protocol.write(out, proto);
				Location loc = decodeLocation(in);
				id.resolve(loc);
				success = listener.register(id);
				out.writeBoolean(success);
				break;
			case PROPAGATE:
				protocol.write(out, proto);
				success = listener.propagate(id);
				out.writeBoolean(success);
				break;
			case DEREGISTER:
				protocol.write(out, proto);
				success = listener.deregister(id);
				out.writeBoolean(success);
				break;
			case WILL_ACTIVATE:
				protocol.write(out, proto);
				success = listener.willActivate(id);
				out.writeBoolean(success);
				break;
			case MANAGER_LOCATION:
				protocol.write(out, proto);
				Location mgrloc = listener.getLocation();
				encodeLocation(out, mgrloc);
				success = true;
				break;
			default:
				logger.log(Level.WARNING, "Unsupported operation {0} requested", proto);
				out.writeInt(SimulationManagerProtocol.UNSUPPORTED.num);
				break;
		}
		out.flush();
		in.cleanUp();
		
		if (resubmit) {
			handler.resubmit(this);
		}
		
		return success;
	}

	@Override
	public String getName() {
		return "ManagerProtocolHandler";
	}

	@Override
	protected void handleThrowable(Throwable ex) {
		listener.fatalException(ex);
	}	
}
