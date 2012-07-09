/*
 * 
 */

package muscle.manager;

import eu.mapperproject.jmml.util.ArrayMap;
import eu.mapperproject.jmml.util.ArraySet;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import muscle.id.Identifier;
import muscle.net.AbstractConnectionHandler;
import muscle.net.LocalSocketFactory;
import muscle.net.SocketFactory;

/**
 * Keeps a registry of running submodels and stops simulation once all submodels have finished.
 * @author Joris Borgdorff
 */
public class SimulationManager {
	private final Map<String,Identifier> registered;
	private final Set<String> available;
	private final Set<String> stillActive;
	private boolean isDone;
	private AbstractConnectionHandler connections;
	private final static Logger logger = Logger.getLogger(SimulationManager.class.getName());
	
	private SimulationManager(Set<String> stillActive) {
		this.stillActive = stillActive;
		this.registered = new ArrayMap<String,Identifier>(stillActive == null ? 10 : stillActive.size());
		this.available = new ArraySet<String>(stillActive == null ? 10 : stillActive.size());
		this.isDone = false;
	}
	
	private void setConnectionHandler(AbstractConnectionHandler ch) {
		this.connections = ch;
	}
	
	public synchronized boolean register(Identifier id) {
		logger.log(Level.FINE, "Registering ID {0}", id);
		if (this.registered.containsKey(id.getName())) {
			logger.log(Level.WARNING, "Registering ID {0} failed: an ID is already registered under the same name.", id);
			return false;
		}
		else if (!id.isResolved()) {
			logger.log(Level.WARNING, "Registering ID {0} failed, because it is not resolved; can only register ID's that resolved.", id);
			return false;
		}
		else {
			logger.log(Level.INFO, "Registered ID {0}", id);
			this.registered.put(id.getName(),id);
			return true;
		}
	}
	
	public synchronized boolean propagate(Identifier id) {
		logger.log(Level.FINE, "Propagating ID {0}", id);
		if (!this.registered.containsKey(id.getName())) {
			logger.log(Level.WARNING, "Propagating ID {0} failed: can only propagate registered ID's.", id);
			return false;
		}
		else {
			this.available.add(id.getName());
			this.notifyAll();
			logger.log(Level.INFO, "Propagated ID {0}", id);
			return true;
		}		
	}
	
	public synchronized boolean deregister(Identifier id) {
		logger.log(Level.FINE, "Deregistering ID {0}", id);
		if (stillActive != null) {
			if (!stillActive.remove(id.getName())) {
				logger.log(Level.WARNING, "Failed to deregister ID {0}, because it was not registered.", id);
			}
			else if (logger.isLoggable(Level.INFO) && !stillActive.isEmpty()) {
				String mult;
				if (stillActive.size() == 1) {
					mult = "'" + stillActive.iterator().next() + "' has";
				} else {
					mult = stillActive + " have";
				}
				logger.log(Level.INFO, "Deregistered {0}; will quit MUSCLE once {1} finished computation.", new Object[] {id, mult});
			}
			
			if (stillActive.isEmpty()) {
				logger.info("All ID's have finished, quitting MUSCLE now.");
				this.dispose();
			}
		}
		return this.available.remove(id.getName());
	}
	
	public synchronized void resolve(Identifier id) throws InterruptedException {
		logger.log(Level.FINE, "Resolving location of ID {0}", id);
		while (!id.isResolved() && !this.available.contains(id.getName()) && !this.isDone) {
			logger.log(Level.FINER, "Location of ID {0} not found yet, waiting...", id);
			wait();
		}
		if (!isDone && !id.isResolved()) {
			Identifier resolvedId = this.registered.get(id.getName());
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
			InetAddress addr = InetAddress.getLocalHost();
			SocketFactory sf = new LocalSocketFactory();
			ServerSocket ss = sf.createServerSocket(0, 10, addr);
			mch = new ManagerConnectionHandler(sm, ss);
			mch.start();
			sm.setConnectionHandler(mch);
		} catch (Exception ex) {
			logger.log(Level.SEVERE, "Could not start connection manager.", ex);
			if (mch != null) {
				sm.dispose();
				mch.dispose();
				mch = null;
			}
		}
		if (mch != null) {
			mch.writeLocation();
		}
	}
}
