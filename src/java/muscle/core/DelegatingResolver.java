package muscle.core;

import muscle.core.ident.IDManipulator;
import java.util.Map;
import java.util.Set;
import muscle.core.ident.IDType;
import muscle.core.ident.Identifier;
import muscle.core.ident.Location;
import muscle.core.kernel.InstanceController;
import utilities.data.ArrayMap;
import utilities.data.ArraySet;

/**
 *
 * @author Joris Borgdorff
 */
public class DelegatingResolver implements Resolver {
	private final IDManipulator delegate;
	private final Map<String,Identifier> idCache;
	private Location here;
	private final Set<String> searchingNow;
	private final Set<String> stillAlive;
	private boolean isDone;
	
	public DelegatingResolver(IDManipulator newDelegate, Set<String> stillAlive) {
		delegate = newDelegate;
		idCache = new ArrayMap<String,Identifier>();
		searchingNow = new ArraySet<String>();
		here = delegate.getLocation();
		this.stillAlive = stillAlive;
		System.out.println("StillAlive: " + this.stillAlive);
		this.isDone = false;
	}
	
	/**
	 * Gets an identifier based on the name and type, or creates a new
	 * one if none exists. If it is not yet resolved, it tries to resolve it and
	 * waits until this is completed.
	 * 
	 * @throws InterruptedException if the process was interrupted before the id was resolved.
	 */
	public synchronized Identifier getResolvedIdentifier(String name, IDType type) throws InterruptedException {
		if (type == IDType.port) {
			Identifier id = getIdentifier(name, type);
			this.resolveIdentifier(id);
			return id;
		}
		String fullName = name(name, type);
		if (!idCache.containsKey(fullName)) {
			this.search(name, type);
			while (!isDone && !idCache.containsKey(fullName)) {
				wait();
			}
			if (isDone) throw new InterruptedException("Resolver interrupted");
		}
		return idCache.get(fullName);
	}
	
	/**
	 * Gets a current identifier based on the name and type, or creates
	 * a new one if none is available.
	 */
	public Identifier getIdentifier(String name, IDType type) {
		String fullName = name(name, type);
		
		// Try cache first, not synchronized as making a new id is not expensive
		if (idCache.containsKey(fullName)) {
			return idCache.get(fullName);
		}
		
		// Make a new ID
		return delegate.create(name, type);
	}
	
	/** Resolves an identifier, waiting until this is finished. */
	public void resolveIdentifier(Identifier id) throws InterruptedException  {
		delegate.resolve(id);
	}
	
	/** Whether the id resides in the current location */
	public boolean isLocal(Identifier id) {
		if (here == null) {
			return false;
		}
		return here.equals(id.getLocation());
	}

	/** Registers a local InstanceController. */
	public void register(InstanceController controller) {
		Identifier id = controller.getIdentifier();
		this.addIdentifier(id);
		
		delegate.propagate(id, here);
	}
	
	/** Deregisters a local InstanceController. */
	public void deregister(InstanceController controller) {
		Identifier id = controller.getIdentifier();
		delegate.delete(id);
		removeIdentifier(id.getName(), id.getType());
	}
	
	/**
	 * Removes the identifier with given name and type from the resolver. If this
	 * is the last id that was alive, and autoquit is active, it kills the platform.
	 */
	public synchronized void removeIdentifier(String name, IDType type) {
		this.idCache.remove(name(name, type));
		if (type == IDType.instance && stillAlive != null && !this.stillAlive.isEmpty()) {
			this.stillAlive.remove(name);
			System.out.println("Waiting on " + stillAlive + " to kill platform.");
		
			if (this.stillAlive.isEmpty()) {
				delegate.deletePlatform();
			}
		}
		
	}

	/**
	 * Searches for an identifier through an IDManipulator.
	 */
	public void search(String name, IDType type) {
		String fullName = name(name, type);
		synchronized(this) {
			if (searchingNow.contains(fullName)) {
				return;
			}
			else {
				searchingNow.add(fullName);
			}
		}
		delegate.search(getIdentifier(name, type));
	}
	
	/** Add an identifier to the resolver. This also removes it from any 
	 *  search list it might be on.
	 */
	public synchronized void addIdentifier(Identifier identifier) {
		String fullName = name(identifier.getName(), identifier.getType());
		idCache.put(fullName, identifier);
		searchingNow.remove(fullName);
		
		notifyAll();
	}
	
	private static String name(String name, IDType type) {
		return name + "#" + type.name();
	}
	
	public synchronized void dispose() {
		this.isDone = true;
		this.notifyAll();
	}
}
