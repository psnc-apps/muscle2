/*
 * 
 */

package muscle.core.ident;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import muscle.core.messaging.serialization.DeserializerWrapper;
import muscle.core.messaging.serialization.SerializerWrapper;
import muscle.manager.SimulationManagerProtocol;
import muscle.net.ProtocolHandler;
import muscle.net.SocketFactory;

/**
 *
 * @author Joris Borgdorff
 */
public class TcpIDManipulator implements IDManipulator {
	private final static Logger logger = Logger.getLogger(TcpIDManipulator.class.getName());
	private final ExecutorService executor;
	private final SocketFactory sockets;
	private Resolver resolver;
	private final InetSocketAddress managerAddr;
	private final Location location;
	
	public TcpIDManipulator(SocketFactory sf, InetSocketAddress managerAddr, Location loc) {
		this.executor = Executors.newCachedThreadPool();
		this.sockets = sf;
		this.managerAddr = managerAddr;
		this.resolver = null;
		this.location = loc;
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

		Future<Boolean> f = runQuery(id, SimulationManagerProtocol.REGISTER);
		try {
			return (f != null && Boolean.TRUE.equals(f.get()));
		} catch (InterruptedException ex) {
			return false;
		} catch (ExecutionException ex) {
			return false;
		}
	}

	@Override
	public boolean propagate(Identifier id) {
		this.checkInstanceID(id);
		
		Future<Boolean> f = runQuery(id, SimulationManagerProtocol.PROPAGATE);
		try {
			return (f != null && Boolean.TRUE.equals(f.get()));
		} catch (InterruptedException ex) {
			return false;
		} catch (ExecutionException ex) {
			return false;
		}
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
	public boolean delete(Identifier id) {
		this.checkInstanceID(id);
		
		Future<Boolean> f = this.runQuery(id, SimulationManagerProtocol.DEREGISTER);
		try {
			return (f != null && Boolean.TRUE.equals(f.get()));
		} catch (InterruptedException ex) {
			return false;
		} catch (ExecutionException ex) {
			return false;
		}
	}

	@Override
	public void deletePlatform() {
		throw new UnsupportedOperationException("Not supported yet.");
	}
		
	private Future<Boolean> runQuery(Identifier id, SimulationManagerProtocol action) {
		try {
			Socket s = this.sockets.createSocket();
			s.connect(managerAddr);
			ManagerProtocolHandler proto = new ManagerProtocolHandler(s, (InstanceID)id, action);
			return this.executor.submit(proto);
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
			super(s, TcpIDManipulator.this, true, true, true);
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
			out.writeInt(this.action.ordinal());
			out.writeString(this.id.getName());
			if (action == SimulationManagerProtocol.REGISTER) {
				encodeLocation(out, this.id.getLocation());
			}
			out.flush();
			logger.log(Level.FINEST, "Initiated protocol to perform action {0} on ID {1}: sent action code", new Object[]{action, id});
			
			// See if the command was understood
			in.refresh();
			int opnum = in.readInt();
			SimulationManagerProtocol[] protoArr = SimulationManagerProtocol.values();
			if (opnum >= protoArr.length || opnum < 0 || protoArr[opnum] == SimulationManagerProtocol.UNSUPPORTED) {
				logger.log(Level.WARNING, "Operation {0} is not understood", this.action);
				return Boolean.FALSE;
			}

			// Wait for the resolver to resolve
			if (action == SimulationManagerProtocol.LOCATE) {
				in.refresh();
			}
			boolean success = in.readBoolean();
			
			if (success) {
				if (action == SimulationManagerProtocol.LOCATE) {
					this.id.resolve(decodeLocation(in));
					resolver.addResolvedIdentifier(id);
				}
				logger.log(Level.FINE, "Successfully finished the {0} protocol for ID {1}", new Object[]{action, id});
			}
			else {
				logger.log(Level.WARNING, "Failed to finish the {0} protocol for ID {1}", new Object[]{action, id});
			}
			return success;
		}
		
		Boolean wasSuccessful() {
			return successful;
		}
	}
}
