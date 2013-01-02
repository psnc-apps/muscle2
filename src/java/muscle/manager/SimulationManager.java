/*
 * 
 */

package muscle.manager;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import muscle.id.Identifier;
import muscle.id.Location;
import muscle.id.TcpLocation;
import muscle.net.AbstractConnectionHandler;
import muscle.exception.ExceptionListener;
import muscle.net.CrossSocketFactory;
import muscle.net.SocketFactory;
import muscle.util.JVM;
import muscle.util.concurrency.Disposable;

/**
 * Keeps a registry of running submodels and stops simulation once all submodels have finished.
 * 
 *
 * Constraints that must be satisfied:
 * - every ID will register exactly once (synchronized registration)
 * - an ID will become available exactly once, during propagation
 * - an ID will become inactive exactly once, during deregistration
 * - willActivate will return true if an ID did not deregister (is in stillActive)
 * - resolve(ID) will return an ID if willActive is true and once the ID is available (in stillActive and in available, waiting for propagation, deregistration and dispose)
 * - all methods register, resolve, willActivate will return false if the manager is disposed
 *
 * @author Joris Borgdorff
 */
public class SimulationManager implements ExceptionListener, Disposable {
	private final static Logger logger = Logger.getLogger(SimulationManager.class.getName());

	public static void main(String[] args) {
		Set<String> stillActive;
		if (args.length == 0) {
			logger.severe("No instances given for manager to manage. Aborting.");
			System.err.println("Usage: SimulationManager (-f INSTANCE_FILE|INSTANCE...)");
			System.exit(21);
		}
		if (args[0].equals("-f")) {
			if (args.length != 2) {
				System.err.println("Usage: SimulationManager (-f INSTANCE_FILE|INSTANCE...)");
				System.exit(21);
			}
			
			stillActive = new HashSet<String>(100);
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new FileReader(args[1]));
				String line;
				while ((line = reader.readLine()) != null) {
					stillActive.add(line);
				}
			} catch (IOException ex) {
				logger.log(Level.SEVERE, "Could not load instances file", ex);
				System.err.println("Usage: SimulationManager (-f INSTANCE_FILE|INSTANCE...)");
				System.exit(21);
			} finally {
				if (reader != null) {
					try {
						reader.close();
					} catch (IOException ex) {
					}
				}
			}
		} else {
			stillActive = new HashSet<String>(args.length*4/3);
			stillActive.addAll(Arrays.asList(args));
		}

		SimulationManager sm = new SimulationManager(stillActive);
		ManagerConnectionHandler mch;
		
		try {
			SocketFactory sf = new CrossSocketFactory();
			InetAddress addr = SocketFactory.getMuscleHost();
			int port = Integer.parseInt(System.getProperty("muscle.manager.bindport","0"));
			ServerSocket ss = sf.createServerSocket(port, 1000, addr);
			mch = new ManagerConnectionHandler(sm, ss);
			sm.setConnectionHandler(mch);
			mch.start();
		} catch (Throwable ex) {
			logger.log(Level.SEVERE, "Could not start connection manager.", ex);
			sm.dispose();
			mch = null;
			System.exit(1);
		}
		if (mch != null) {
			mch.writeLocation();
		}
	}
	
	/**
	 * Contains one identifier per Instance that has correctly registered.
	 * Only operations performed: put (register), containsKey (register), and get (resolve)
	 */
	private final Map<String,Identifier> registered;
	
	/**
	 * Once an identifier has propagated it is available until it deregisters.
	 * Operations performed: put (propagate), contains (resolve), and remove (deregister)
	 */
	private final Set<String> available;
	
	/**
	 * Simulation manager will only wait for instances that are in the stillActive list.
	 * Operations performed: contains (resolve, willActivate), remove (deregister), isEmpty (deregister)
	 */
	private final Set<String> stillActive;
	/**
	 * Contains already used locations, for creating correct symlinks.
	 * Operations performed: put (register), contains (register)
	 */
	private final Set<Location> locs;
	
	private boolean isDone;
	private AbstractConnectionHandler connections;
	private Location location;
	
	private SimulationManager(Set<String> stillActive) {
		this.stillActive = Collections.synchronizedSet(stillActive);
		int size = stillActive.size();
		this.registered = new HashMap<String,Identifier>(size*4/3);
		this.available = new HashSet<String>(size*4/3);
		this.locs = new HashSet<Location>(size*4/3);
		this.connections = null;
		this.isDone = false;
		this.location = null;
	}
	
	private synchronized void setConnectionHandler(AbstractConnectionHandler ch) {
		this.connections = ch;
	}
	
	public boolean register(Identifier id) {
		logger.log(Level.FINE, "Registering ID {0}", id);
		String name = id.getName();
		synchronized (registered) {
			if (this.registered.containsKey(name)) {
				logger.log(Level.WARNING, "Registering ID {0} failed: an ID is already registered under the same name.", id);
				return false;
			}

			this.registered.put(name,id);
		}
		logger.log(Level.INFO, "Registered ID {0}", id);
		
		addSimulationLocation(id);
		return true;
	}
	
	public boolean propagate(Identifier id) {
		logger.log(Level.FINER, "Propagating ID {0}", id);
		String name = id.getName();
		synchronized (this.registered) {
			if (!this.registered.containsKey(name)) {
				logger.log(Level.WARNING, "Propagating ID {0} failed: can only propagate registered ID's.", id);
				return false;
			}
		}
		synchronized (this) {
			this.available.add(name);
			this.notifyAll();
		}
		logger.log(Level.FINE, "Propagated ID {0}", id);
		return true;
	}
	
	public boolean resolve(Identifier id) throws InterruptedException {
		logger.log(Level.FINE, "Resolving location of ID {0}", id);
		String idName = id.getName();
		// Exit this loop if 1) the ID became available; 2) the ID in question quit; 3) the simulationManager quit
		synchronized (this) {
			while (!this.available.contains(idName) && stillActive.contains(idName) && !this.isDisposed()) {
				logger.log(Level.FINER, "Location of ID {0} not found yet, waiting...", idName);
				wait();
			}
		}
		if (stillActive.contains(idName)) {
			// We exited the loop, so either manager is disposed (in return statement) or it is available
			Identifier resolvedId = this.registered.get(idName);
			logger.log(Level.FINE, "Location of ID {0} resolved: {1}", new Object[]{id, resolvedId.getLocation()});
			id.resolveLike(resolvedId);
			return !isDisposed();
		} else {
			// The ID already quit
			logger.log(Level.FINE, "Location of ID {0} not available: it already quit.", id);		
			return false;
		}
	}
	
	public boolean deregister(Identifier id) {
		logger.log(Level.FINE, "Deregistering ID {0}", id);
		int size;
		String name = id.getName();
		// Must be able to remove identifier, otherwise it was not registered
		if (!stillActive.remove(name)) {
			logger.log(Level.WARNING, "Failed to deregister ID {0}, because it was not registered.", id);
		} else if (logger.isLoggable(Level.INFO) && (size = stillActive.size()) > 0) {
			// Print registration information; not synchronized, no synchronization error (mult = null), print nothing
			String mult;
			if (size == 1) {
				try {
					mult = "'" + stillActive.iterator().next() + "' has";
				} catch (Exception ex) {
					mult = null;
				}
			} else if (size < 10) {
				try {
					mult = stillActive + " have";
				} catch (Exception ex) {
					mult = size + " more instances have";
				}
			} else {
				mult = size + " more instances have";
			}
			if (mult != null) {
				logger.log(Level.INFO, "Deregistered {0}; will quit MUSCLE once {1} finished computation.", new Object[] {id, mult});
			}
		}

		// Only occurs if the current remove finished. May be called by multiple threads at once but that's no problem
		if (stillActive.isEmpty()) {
			logger.info("All ID's have finished, quitting MUSCLE now.");
			this.dispose();
		}
		
		synchronized (this) {
			this.notifyAll();
			return this.available.remove(name);
		}
	}
	
	public boolean willActivate(Identifier id) {
		logger.log(Level.FINE, "Checking whether ID {0} has not deregistered", id);
		boolean ret = stillActive.contains(id.getName());
		logger.log(Level.FINE, "ID {0} {1} deregistered", new Object[] {id, ret ? "has not" : "has"});
		return ret;
	}
	
	@Override
	public void fatalException(Throwable ex) {
		logger.log(Level.SEVERE, "Fatal exception occurred. Aborting.", ex);
		System.exit(19);
	}

	public Location getLocation() {
		if (location == null) {
			String dir = JVM.ONLY.getTmpDirName();
			location = new TcpLocation(connections.getSocketAddress(), dir);
		}
		return location;
	}
	
	
	// Creates a symlink to the MUSCLE dir in question
	private void addSimulationLocation(Identifier id) {
		Location loc = id.getLocation();
		synchronized (this.locs) {
			if (this.locs.contains(loc)) {
				return;
			}
			this.locs.add(loc);
		}

		((TcpLocation)loc).createSymlink("simulation-" + id.getName() + "-et-al", (TcpLocation)getLocation());
	}
	
	@Override
	public void dispose() {
		synchronized (this) {
			if (isDisposed()) {
				return;
			}
			this.isDone = true;
		}
		logger.finer("Stopping Simulation Manager");
		if (this.connections != null) {
			this.connections.dispose();
		}
		synchronized (this) {
			notifyAll();
		}
	}

	@Override
	public synchronized boolean isDisposed() {
		return this.isDone;
	}
}
