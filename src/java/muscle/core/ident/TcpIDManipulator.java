/*
 * 
 */

package muscle.core.ident;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import muscle.manager.SimulationManagerProtocol;
import muscle.net.SocketFactory;
import muscle.net.XdrProtocolHandler;
import org.acplt.oncrpc.OncRpcException;
import org.acplt.oncrpc.XdrTcpDecodingStream;
import org.acplt.oncrpc.XdrTcpEncodingStream;

/**
 *
 * @author Joris Borgdorff
 */
public class TcpIDManipulator implements IDManipulator {
	private final static Logger logger = Logger.getLogger(TcpIDManipulator.class.getName());
	private final ExecutorService executor;
	private final SocketFactory sockets;
	private Resolver resolver;
	private final SocketAddress managerAddr;
	
	public TcpIDManipulator(SocketFactory sf, SocketAddress managerAddr) {
		this.executor = Executors.newCachedThreadPool();
		this.sockets = sf;
		this.managerAddr = managerAddr;
		this.resolver = null;
	}
	
	public void setResolver(DelegatingResolver resolver) {
		this.resolver = resolver;
	}

	@Override
	public void propagate(Identifier id, Location loc) {
		this.checkInstanceID(id);
		if (!id.isResolved())
			((InstanceID)id).resolve(loc);

		runQuery(id, SimulationManagerProtocol.REGISTER);
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

	// TODO: implement getLocation
	@Override
	public Location getLocation() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void delete(Identifier id) {
		this.checkInstanceID(id);
		
		this.runQuery(id, SimulationManagerProtocol.DEREGISTER);
	}

	@Override
	public void deletePlatform() {
		throw new UnsupportedOperationException("Not supported yet.");
	}
		
	private void runQuery(Identifier id, SimulationManagerProtocol action) {
		try {
			Socket s = this.sockets.createSocket();
			s.connect(managerAddr);
			ManagerProtocolHandler proto = new ManagerProtocolHandler(s, (InstanceID)id, action);
			this.executor.submit(proto);
		} catch (IOException ex) {
			logger.log(Level.SEVERE, "Could not open socket to initiate the " + action + " protocol with SimulationManager", ex);
		}
	}
	
	private void checkInstanceID(Identifier id) {
		if (!(id instanceof InstanceID))
			throw new IllegalArgumentException("ID " + id + " is not an InstanceID; can only work with InstanceID's.");
	}
	
	private class ManagerProtocolHandler extends XdrProtocolHandler<TcpIDManipulator> {
		private final SimulationManagerProtocol action;
		private final InstanceID id;

		ManagerProtocolHandler(Socket s, InstanceID id, SimulationManagerProtocol action) {
			super(s, TcpIDManipulator.this, true);
			this.action = action;
			this.id = id;
			if (this.action == SimulationManagerProtocol.REGISTER && !this.id.isResolved()) {
				throw new IllegalArgumentException("ID " + id + " is not resolved; can only register resolved ID's.");
			}
			if (this.action == SimulationManagerProtocol.LOCATE && this.id.isResolved()) {
				throw new IllegalArgumentException("ID " + id + " is already resolved, so no need to search it.");
			}
		}
		
		@Override
		protected void executeProtocol(XdrTcpDecodingStream xdrIn, XdrTcpEncodingStream xdrOut) throws OncRpcException, IOException {
			// Send command
			xdrOut.xdrEncodeInt(this.action.ordinal());
			xdrOut.xdrEncodeString(this.id.getName());
			if (action == SimulationManagerProtocol.REGISTER) {
				encodeLocation(xdrOut, this.id.getLocation());
			}
			xdrOut.endEncoding(true);
			
			// See if the command was understood
			int opnum = xdrIn.xdrDecodeInt();
			SimulationManagerProtocol[] protoArr = SimulationManagerProtocol.values();
			if (opnum >= protoArr.length || opnum < 0 || protoArr[opnum] == SimulationManagerProtocol.UNSUPPORTED) {
				logger.log(Level.WARNING, "Operation {0} is not understood", this.action);
				return;
			}

			boolean success = xdrIn.xdrDecodeBoolean();
			
			if (success) {
				if (action == SimulationManagerProtocol.LOCATE) {
					this.id.resolve(decodeLocation(xdrIn));
					resolver.addResolvedIdentifier(id);
				}
				logger.log(Level.FINE, "Successfully finished the {0} protocol for ID {1}", new Object[]{action, id});
			}
			else {
				logger.log(Level.WARNING, "Failed to finish the {0} protocol for ID {1}", new Object[]{action, id});
			}
		}
	}
}
