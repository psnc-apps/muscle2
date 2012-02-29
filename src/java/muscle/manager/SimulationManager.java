/*
 * 
 */

package muscle.manager;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import muscle.core.ident.Identifier;
import muscle.net.AbstractConnectionHandler;
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
	
	private SimulationManager(Set<String> stillActive) {
		this.stillActive = stillActive;
		this.active = new ArrayMap<String,Identifier>(stillActive == null ? 10 : stillActive.size());
		this.isDone = false;
	}
	
	private void setConnectionHandler(AbstractConnectionHandler ch) {
		this.connections = ch;
	}
	
	public synchronized boolean register(Identifier id) {
		if (this.active.containsKey(id.getName())) {
			return false;
		}
		else if (!id.isResolved()) {
			throw new IllegalArgumentException("Identifier " +id + " is not resolved; can only register ID's that resolved.");
		}
		else {
			this.active.put(id.getName(),id);
			this.notifyAll();
			return true;
		}
	}
	
	public synchronized boolean deregister(Identifier id) {
		if (stillActive != null) {
			stillActive.remove(id.getName());
			if (stillActive.isEmpty()) {
				this.dispose();
			}
		}
		return (this.active.remove(id.getName()) != null);
	}
	
	public synchronized void resolve(Identifier id) throws InterruptedException {
		while (!id.isResolved() && !this.active.containsKey(id.getName()) && !this.isDone) {
			wait();
		}
		if (!isDone && !id.isResolved()) {
			id.resolveLike(this.active.get(id.getName()));
		}
	}
	
	public synchronized void dispose() {
		this.isDone = true;
		this.connections.dispose();
		notifyAll();
	}
	
	public static void main(String[] args) {
		Set<String> stillActive = new ArraySet<String>(args.length);
		stillActive.addAll(Arrays.asList(args));
		
		SimulationManager sm = new SimulationManager(stillActive);
		ManagerConnectionHandler mch = null;
		
		try {	
			mch = new ManagerConnectionHandler(sm);
			mch.start();
		} catch (Exception ex) {
			Logger.getLogger(SimulationManager.class.getName()).log(Level.SEVERE, "Could not start connection manager.", ex);
			if (mch != null)
				mch.dispose();
		}
	}
}
