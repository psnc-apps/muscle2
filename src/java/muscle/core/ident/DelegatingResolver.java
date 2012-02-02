package muscle.core.ident;

import java.util.Map;
import java.util.Set;
import muscle.core.kernel.InstanceController;
import utilities.data.ArrayMap;
import utilities.data.ArraySet;

/**
 *
 * @author Joris Borgdorff
 */
public class DelegatingResolver implements Resolver {
	private final IDManipulator delegate;
	/** Stores all resolved IDs */
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
	 * Gets a current identifier based on the name and type, or creates
	 * a new one if none is available.
	 */
	public Identifier getIdentifier(String name, IDType type) {
		String fullName = name(name, type);
		
		// Try cache first, not synchronized as making a new id is not expensive
		if (idCache.containsKey(fullName)) {
			return idCache.get(fullName);
		}

		return delegate.create(name, type);
	}
	
	/**
	 * Resolves an identifier, waiting until this is finished.
	 * 
	 * @throws InterruptedException if the process was interrupted before the id was resolved.
	 */
	public void resolveIdentifier(Identifier id) throws InterruptedException  {
		if (id.isResolved()) return;
		// Only search for instances directly
		if (id.getType() == IDType.port) {
			resolveIdentifier(((PortalID)id).getOwnerID());
			return;
		}
		
		// See if we have a resolved id in cache
		String fullName = name(id.getName(), id.getType());
				
		synchronized (this) {
			// add a search only if this instance is not already searchd for
			if (!searchingNow.contains(fullName) && !idCache.containsKey(fullName)) {
				searchingNow.add(fullName);
				delegate.search(id);
			}
			// Whether a new search was conducted or not, there is a search going
			while (!isDone && !id.isResolved() && !idCache.containsKey(fullName)) {
				wait();
			}
			
			if (isDone) {
				throw new InterruptedException("Resolver interrupted");
			}
			if (!id.isResolved()) {
				id.resolveLike(idCache.get(fullName));
			}
		}
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
		this.addResolvedIdentifier(id);
		
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
	
	/** Add an identifier to the resolver. This also removes it from any 
	 *  search list it might be on.
	 */
	public synchronized void addResolvedIdentifier(Identifier id) {
		if (!id.isResolved()) throw new IllegalArgumentException("ID " + id + " is not resolved, but Resolver only accepts resolved IDs");
		
		String fullName = name(id.getName(), id.getType());
		idCache.put(fullName, id);
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
