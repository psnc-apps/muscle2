/*
 * 
 */

package muscle.client.id;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import muscle.exception.MUSCLERuntimeException;
import muscle.id.*;
import muscle.manager.SimulationManagerProtocol;
import muscle.net.ProtocolHandler;
import muscle.net.SocketFactory;
import muscle.util.concurrency.NamedExecutor;
import muscle.util.serialization.DeserializerWrapper;
import muscle.util.serialization.SerializerWrapper;

/**
 * @author Joris Borgdorff
 */
public class TcpIDManipulator implements IDManipulator {
	private final static Logger logger = Logger.getLogger(TcpIDManipulator.class.getName());
	private final NamedExecutor executor;
	private final SocketFactory sockets;
	private Resolver resolver;
	private final InetSocketAddress managerAddr;
	private final Location location;
	private Location managerLocation;
	private boolean searchingForManager;
	
	public TcpIDManipulator(SocketFactory sf, InetSocketAddress managerAddr, Location loc) {
		this.executor = new NamedExecutor();
		this.sockets = sf;
		this.managerAddr = managerAddr;
		this.resolver = null;
		this.location = loc;
		this.managerLocation = null;
		this.searchingForManager = false;
	}
	
	public void setResolver(Resolver resolver) {
		this.resolver = resolver;
	}
	
	public void dispose() {
		executor.shutdown();
	}

	@Override
	public boolean register(Identifier id, Location loc) {
		this.checkInstanceID(id);
		if (!id.isResolved())
			((InstanceID)id).resolve(loc);

		return runQuery(id, SimulationManagerProtocol.REGISTER);
	}

	@Override
	public boolean propagate(Identifier id) {
		this.checkInstanceID(id);
		
		return runQuery(id, SimulationManagerProtocol.PROPAGATE);
	}
	
	@Override
	public void search(Identifier id) {
		if (id.isResolved()) return;
		this.checkInstanceID(id);
		if (this.resolver == null) {
			throw new IllegalStateException("Can not search for an ID while no Resolver is known.");
		}
		
		runQuery(id, SimulationManagerProtocol.LOCATE);
	}
	
	public boolean willActivate(Identifier id) {
		this.checkInstanceID(id);
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
			String[] portId = name.split("@");
			if (portId.length != 2) {
				throw new IllegalArgumentException("A port identifier <" + portId + "> must feature a port and owner name, separated by an '@' symbol.");
			}
			return new PortalID<InstanceID>(portId[0], new InstanceID(portId[1]));
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
		this.runQuery(null, SimulationManagerProtocol.MANAGER_LOCATION);
		// Manager is set in the runQuery
		synchronized (this) {
			return this.managerLocation;
		}
	}

	@Override
	public boolean delete(Identifier id) {
		this.checkInstanceID(id);
		
		return this.runQuery(id, SimulationManagerProtocol.DEREGISTER);
	}

	@Override
	public void deletePlatform() {
		throw new UnsupportedOperationException("Not supported yet.");
	}
		
	private Future<Boolean> submitQuery(Identifier id, SimulationManagerProtocol action) {
		ManagerProtocolHandler proto = createProtocolHandler(id, action);
		if (proto == null) {
			return null;
		}
		return this.executor.submit(proto);
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
			Socket s = this.sockets.createSocket();
			s.connect(managerAddr);
			return new ManagerProtocolHandler(s, (InstanceID)id, action);
		} catch (IOException ex) {
			logger.log(Level.SEVERE, "Could not open socket to initiate the " + action + " protocol with SimulationManager", ex);
			return null;
		}
	}
	
	private void checkInstanceID(Identifier id) {
		if (!(id instanceof InstanceID))
			throw new IllegalArgumentException("ID " + id + " is not an InstanceID; can only work with InstanceID's.");
	}
	
	private class ManagerProtocolHandler extends ProtocolHandler<Boolean,TcpIDManipulator> {
		private final SimulationManagerProtocol action;
		private final InstanceID id;
		private Boolean successful;

		ManagerProtocolHandler(Socket s, InstanceID id, SimulationManagerProtocol action) {
			super(s, TcpIDManipulator.this, true, true, 3, true);
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
			out.writeInt(SimulationManagerProtocol.MAGIC_NUMBER.intValue());
			out.writeInt(this.action.intValue());
			if (id != null) {
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
				return Boolean.FALSE;
			} else if (op == SimulationManagerProtocol.ERROR) {
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
						resolver.addResolvedIdentifier(id);
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
