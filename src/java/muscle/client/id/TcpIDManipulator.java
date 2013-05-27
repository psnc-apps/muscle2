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

package muscle.client.id;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import muscle.exception.MUSCLERuntimeException;
import muscle.id.*;
import muscle.manager.SimulationManagerProtocol;
import muscle.net.ProtocolHandler;
import muscle.net.SocketFactory;
import muscle.net.SocketPool;
import muscle.util.serialization.DeserializerWrapper;
import muscle.util.serialization.SerializerWrapper;

/**
 * @author Joris Borgdorff
 */
public class TcpIDManipulator implements IDManipulator {
	private final static Logger logger = Logger.getLogger(TcpIDManipulator.class.getName());
	private Resolver resolver;
	private final InetSocketAddress managerAddr;
	private final Location location;
	private Location managerLocation;
	private boolean searchingForManager;
	private final SocketPool socketPool;
	
	public TcpIDManipulator(SocketFactory sf, InetSocketAddress managerAddr, Location loc) {
		this.managerAddr = managerAddr;
		this.resolver = null;
		this.location = loc;
		this.managerLocation = null;
		this.searchingForManager = false;
		this.socketPool = new SocketPool(15, sf, managerAddr, SimulationManagerProtocol.CLOSE.intValue());
	}
	
	public synchronized void setResolver(Resolver resolver) {
		this.resolver = resolver;
	}

	public void dispose() {
		this.socketPool.dispose();
	}
	
	@Override
	public boolean register(Identifier id, Location loc) {
		if (!id.isResolved()) {
			((InstanceID)id).resolve(loc);
		}

		return runQuery(id, SimulationManagerProtocol.REGISTER);
	}

	@Override
	public void propagate(Identifier id) {
		this.runQuery(id, SimulationManagerProtocol.PROPAGATE);
	}
	
	@Override
	public void search(Identifier id) {
		if (id.isResolved()) {
			return;
		}
		if (this.resolver == null) {
			throw new IllegalStateException("Can not search for an ID while no Resolver is known.");
		}
		
		runQuery(id, SimulationManagerProtocol.LOCATE);
	}
	
	public boolean willActivate(Identifier id) {
		if (this.resolver == null) {
			throw new IllegalStateException("Can not search for an ID while no Resolver is known.");
		}
		return runQuery(id, SimulationManagerProtocol.WILL_ACTIVATE);
	}

	@Override
	public Identifier create(String name, IDType type) {
		if (type == IDType.instance) {
			return new InstanceID(name);
		}
		else if (type == IDType.port) {
			int portId = name.indexOf('@');
			if (portId == -1) {
				throw new IllegalArgumentException("A port identifier <" + name + "> must feature a port and owner name, separated by an '@' symbol.");
			}
			Identifier owner = resolver.getIdentifier(name.substring(portId+1), IDType.instance);
			return new PortalID(name, name.substring(0, portId), owner);
		}
		else {
			throw new IllegalArgumentException("TcpIDManipulator can only resolve ports and instances, not '" + type + "'.");
		}
	}

	@Override
	public Location getLocation() {
		return this.location;
	}

	@Override
	public  Location getManagerLocation() {
		synchronized (this) {
			// See if manager is found
			if (this.managerLocation != null) {
				return this.managerLocation;
			} // or already searched for
			else if (this.searchingForManager) {
				try {
					while (this.searchingForManager) {
						wait();
					}
					return this.managerLocation;
				} catch (InterruptedException ex) {
					logger.log(Level.WARNING, "Search for manager interrupted.", ex);
					return null;
				}
			}
			// Otherwise, find the manager yourself
			this.searchingForManager = true;
		}
		if (!this.runQuery(null, SimulationManagerProtocol.MANAGER_LOCATION)) {
			synchronized (this) {
				this.searchingForManager = false;
				this.managerLocation = new TcpLocation(this.managerAddr, "");
				this.notifyAll();
			}
		}
		// Manager is set in the runQuery
		synchronized (this) {
			return this.managerLocation;
		}
	}

	@Override
	public void delete(Identifier id) {
		this.runQuery(id, SimulationManagerProtocol.DEREGISTER);
	}

	@Override
	public void deletePlatform() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	private boolean runQuery(Identifier id, SimulationManagerProtocol action) {
		ManagerProtocolHandler proto = createProtocolHandler(id, action);
		if (proto == null) {
			return false;
		}
		Boolean bool = proto.call();
		return (bool != null && bool);
	}
	
	private ManagerProtocolHandler createProtocolHandler(Identifier id, SimulationManagerProtocol action) {
		try {
			Socket s = socketPool.createSocket(action == SimulationManagerProtocol.LOCATE);
			return new ManagerProtocolHandler(s, (InstanceID)id, action);
		} catch (Exception ex) {
			logger.log(Level.SEVERE, "Could not open socket to initiate the " + action + " protocol with SimulationManager", ex);
			return null;
		}
	}
	
	private class ManagerProtocolHandler extends ProtocolHandler<Boolean,TcpIDManipulator> {
		private final SimulationManagerProtocol action;
		private final InstanceID id;
		private Boolean successful;

		ManagerProtocolHandler(Socket s, InstanceID id, SimulationManagerProtocol action) {
			super(s, TcpIDManipulator.this, true, true, 3, false);
			this.action = action;
			this.id = id;
			if (this.action == SimulationManagerProtocol.REGISTER && !this.id.isResolved()) {
				throw new IllegalArgumentException("ID " + id + " is not resolved; can only register resolved ID's.");
			}
			if (this.action == SimulationManagerProtocol.LOCATE && this.id.isResolved()) {
				throw new IllegalArgumentException("ID " + id + " is already resolved, so no need to search it.");
			}
			successful = null;
		}
		
		@Override
		protected Boolean executeProtocol(DeserializerWrapper in, SerializerWrapper out) throws IOException {
			// Send command
			logger.log(Level.FINER, "Initiating protocol to perform action {0} on ID {1}", new Object[]{action, id});
			out.writeInt(SimulationManagerProtocol.MAGIC_NUMBER_KEEP_ALIVE.intValue());
			out.writeInt(this.action.intValue());
			if (id == null) {
				out.writeString("");
			} else {
				out.writeString(this.id.getName());
			}
			if (action == SimulationManagerProtocol.REGISTER) {
				encodeLocation(out, this.id.getLocation());
			}
			out.flush();
			logger.log(Level.FINEST, "Initiated protocol to perform action {0} on ID {1}: sent action code", new Object[]{action, id});
			
			// See if the command was understood
			in.refresh();
			SimulationManagerProtocol op = SimulationManagerProtocol.valueOf(in.readInt());
			if (op == SimulationManagerProtocol.UNSUPPORTED) {
				logger.log(Level.WARNING, "Operation {0} is not understood", this.action);
				in.cleanUp();
				socketPool.discard(this.socket);
				return Boolean.FALSE;
			} else if (op == SimulationManagerProtocol.ERROR) {
				in.cleanUp();
				throw new MUSCLERuntimeException("Connection manager refused communication.");
			}

			// Wait for the resolver to resolve
			if (action == SimulationManagerProtocol.LOCATE) {
				in.refresh();
			}
			boolean success;
			if (action == SimulationManagerProtocol.MANAGER_LOCATION) {
				synchronized (TcpIDManipulator.this) {
					TcpIDManipulator.this.managerLocation = decodeLocation(in);
					TcpIDManipulator.this.searchingForManager = false;
					TcpIDManipulator.this.notifyAll();
				}
				success = true;
			} else {
				success = in.readBoolean();

				if (success || action == SimulationManagerProtocol.WILL_ACTIVATE) {
					if (action == SimulationManagerProtocol.LOCATE) {
						this.id.resolve(decodeLocation(in));
						this.id.setAvailable(true);
						resolver.addAvailableIdentifier(id);
					}
					logger.log(Level.FINE, "Successfully finished the {0} protocol for ID {1}", new Object[]{action, id});
				}
				else {
					if (action == SimulationManagerProtocol.LOCATE) {
						resolver.canNotResolveIdentifier(this.id);
					}
					logger.log(Level.WARNING, "Failed to finish the {0} protocol for ID {1}", new Object[]{action, id});
				}
			}
			in.cleanUp();
			
			socketPool.socketFinished(this.socket, out);
			
			return success;
		}
		
		Boolean wasSuccessful() {
			return successful;
		}

		@Override
		public String getName() {
			return "ManagerProtocol-" + action + "(" + id + ")";
		}
	}
}
