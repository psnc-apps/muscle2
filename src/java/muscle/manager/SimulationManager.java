/*
 * 
 */

package muscle.manager;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import muscle.core.ident.Identifier;
import muscle.net.AbstractConnectionHandler;
import muscle.net.LocalSocketFactory;
import muscle.net.SocketFactory;
import utilities.data.ArrayMap;
import utilities.data.ArraySet;

/**
 * Keeps a registry of running submodels and stops simulation once all submodels have finished.
 * @author Joris Borgdorff
 */
public class SimulationManager {
	private final Map<String,Identifier> active;
	private final Set<String> stillActive;
	private boolean isDone;
	private AbstractConnectionHandler connections;
	private final static Logger logger = Logger.getLogger(SimulationManager.class.getName());
	
	private SimulationManager(Set<String> stillActive) {
		this.stillActive = stillActive;
		this.active = new ArrayMap<String,Identifier>(stillActive == null ? 10 : stillActive.size());
		this.isDone = false;
	}
	
	private void setConnectionHandler(AbstractConnectionHandler ch) {
		this.connections = ch;
	}
	
	public synchronized boolean register(Identifier id) {
		logger.log(Level.FINE, "Registering ID {0}", id);
		if (this.active.containsKey(id.getName())) {
			logger.log(Level.WARNING, "Registering ID {0} failed: an ID is already registered under the same name", id);
			return false;
		}
		else if (!id.isResolved()) {
			logger.log(Level.WARNING, "Registering ID {0} failed, because it is not resolved; can only register ID's that resolved.", id);
			return false;
		}
		else {
			logger.log(Level.INFO, "Registered ID {0}", id);
			this.active.put(id.getName(),id);
			this.notifyAll();
			return true;
		}
	}
	
	public synchronized boolean deregister(Identifier id) {
		logger.log(Level.FINE, "Deregistering ID {0}", id);
		if (stillActive != null) {
			if (!stillActive.remove(id.getName())) {
				logger.log(Level.WARNING, "Failed to deregister ID {0}, because it was not registered.", id);
			}
			else if (logger.isLoggable(Level.FINE) && !stillActive.isEmpty()) {
				logger.log(Level.INFO, "Deregistered {0}; will quit MUSCLE once IDs {1} have finished computation.", new Object[] {id, stillActive});
			}
			
			if (stillActive.isEmpty()) {
				logger.info("All ID's have finished, quitting MUSCLE now.");
				this.dispose();
			}
		}
		return (this.active.remove(id.getName()) != null);
	}
	
	public synchronized void resolve(Identifier id) throws InterruptedException {
		logger.log(Level.FINE, "Resolving location of ID {0}", id);
		while (!id.isResolved() && !this.active.containsKey(id.getName()) && !this.isDone) {
			logger.log(Level.FINER, "Location of ID {0} not found yet, waiting...", id);
			wait();
		}
		if (!isDone && !id.isResolved()) {
			Identifier resolvedId = this.active.get(id.getName());
			logger.log(Level.FINE, "Location of ID {0} resolved: {1}", new Object[]{id, resolvedId.getLocation()});
			id.resolveLike(resolvedId);
		}
	}
	
	public synchronized void dispose() {
		this.isDone = true;
		if (this.connections != null)
			this.connections.dispose();
		notifyAll();
	}
	
	public static void main(String[] args) {
		Set<String> stillActive = null;
		if (args.length > 0) {
			stillActive = new ArraySet<String>(args.length);
			stillActive.addAll(Arrays.asList(args));
		}
		
		SimulationManager sm = new SimulationManager(stillActive);
		ManagerConnectionHandler mch = null;
		
		try {
			int port = 18310;
			InetAddress addr = InetAddress.getLocalHost();
			SocketFactory sf = new LocalSocketFactory();
			mch = new ManagerConnectionHandler(sm, sf.createServerSocket(port, 10, addr));
			mch.start();
			logger.log(Level.INFO, "Started the connection handler, listening on address {0} on port {1}", new Object[]{addr.getHostAddress(), port});
			sm.setConnectionHandler(mch);
		} catch (Exception ex) {
			logger.log(Level.SEVERE, "Could not start connection manager.", ex);
			if (mch != null) {
				sm.dispose();
				mch.dispose();
			}
		}
	}
}
