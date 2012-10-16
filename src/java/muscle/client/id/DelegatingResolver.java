package muscle.client.id;

import eu.mapperproject.jmml.util.ArrayMap;
import eu.mapperproject.jmml.util.ArraySet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import muscle.core.kernel.InstanceController;
import muscle.id.*;

/**
 * A simple resolver that delegates the actual creation and searching of Identifiers and Locations.
 * @author Joris Borgdorff
 */
public class DelegatingResolver implements Resolver {
	protected final IDManipulator delegate;
	/** Stores all resolved IDs */
	private final Map<String,Identifier> resolvedIdCache;
	private final Map<String,Identifier> idCache;
	private final Set<String> expecting;
	private final Set<String> registeredHere;
	private Location here;
	private final Set<String> searchingNow;
	private boolean isDone;
	private final static Logger logger = Logger.getLogger(DelegatingResolver.class.getName());
	
	public DelegatingResolver(IDManipulator newDelegate, Set<String> expecting) {
		delegate = newDelegate;
		resolvedIdCache = new ArrayMap<String,Identifier>();
		idCache = new ConcurrentHashMap<String,Identifier>();
		searchingNow = new ArraySet<String>();
		registeredHere = new ArraySet<String>();
		here = delegate.getLocation();
		this.expecting = expecting;
		this.isDone = false;
	}
	
	/**
	 * Gets a current identifier based on the name and type, or creates
	 * a new one if none is available.
	 */
	public Identifier getIdentifier(String name, IDType type) {
		String fullName = name(name, type);
		
		// Try cache first, not synchronized as making a new id is not expensive
		if (resolvedIdCache.containsKey(fullName)) {
			logger.log(Level.FINER, "Returning identifier ''{0}'' of type {1} from cache", new Object[]{name, type});
			return resolvedIdCache.get(fullName);
		} else {
			Identifier id = idCache.get(fullName);
			if (id == null) {
				logger.log(Level.FINE, "Creating identifier ''{0}'' of type {1}", new Object[]{name, type});
				id = delegate.create(name, type);
				idCache.put(fullName, id);
			}
			return id;
		}
	}
	
	/**
	 * Resolves an identifier, waiting until this is finished.
	 * 
	 * @param id will be resolved if there is no error. Always check isResolved() function afterwards.
	 * @throws InterruptedException if the process was interrupted before the id was resolved.
	 */
	public boolean resolveIdentifier(Identifier id) throws InterruptedException  {
		if (id.isResolved()) {
			return true;
		}
		// Only search for instances directly
		if (id.getType() == IDType.port) {
			return resolveIdentifier(((PortalID)id).getOwnerID());
		}
		
		// See if we have a resolved id in cache
		String fullName = name(id.getName(), id.getType());
		
		boolean searching = false;
		synchronized (this) {
			// add a search only if this instance is not already searchd for
			if (!searchingNow.contains(fullName) && !resolvedIdCache.containsKey(fullName)) {
				logger.log(Level.FINE, "Searching to resolve identifier {0}", id);
				searching = true;
				searchingNow.add(fullName);
			}
		}
		
		if (searching) {
			delegate.search(id);
		}

		synchronized (this) {
			if (searching && !isDone && (!id.isResolved() || !resolvedIdCache.containsKey(fullName))) {
				// Make it clear to other threads that this id will not be resolved
				resolvedIdCache.put(fullName, id);
				notifyAll();
				return false;
			}
			// When a search was not conducted, there is a search going
			while (!isDone && !id.isResolved() && !resolvedIdCache.containsKey(fullName)) {
				wait();
			}
			
			if (isDone) {
				return false;
			}
			if (!id.isResolved()) {
				Identifier resolvedId = resolvedIdCache.get(fullName);
				if (resolvedId.isResolved()) {
					id.resolveLike(resolvedIdCache.get(fullName));
				} else {
					logger.log(Level.WARNING, "Identifier {0} could not be resolved", id);
					return false;
				}
			}
			logger.log(Level.FINE, "Identifier {0} resolved", id);
			return true;
		}
	}

	/** Whether the id resides in the current location */
	public boolean isLocal(Identifier id) {
		String name;
		if (id instanceof PortalID) {
			name = ((PortalID)id).getOwnerID().getName();
		} else {
			name = id.getName();
		}
		try {
			synchronized (registeredHere) {
				while (!this.isDisposed() && expecting.contains(name) && !registeredHere.contains(name)) {
					registeredHere.wait();
				}
				return !this.isDisposed() && registeredHere.contains(name);
			}
		} catch (InterruptedException ex) {
			Logger.getLogger(DelegatingResolver.class.getName()).log(Level.SEVERE, "Could not determine whether id " + id + " is local.", ex);
			return false;
		}
	}

	/** Registers a local InstanceController. */
	public boolean register(InstanceController controller) {
		Identifier id = controller.getIdentifier();
		logger.log(Level.FINE, "Registering identifier {0}", id);
		boolean ret = delegate.register(id, here);
		synchronized (registeredHere) {
			if (ret) {
				registeredHere.add(id.getName());
			} else {
				expecting.remove(id.getName());
			}
			registeredHere.notifyAll();
		}
		return ret;
	}
	
	public void makeAvailable(InstanceController controller) {
		Identifier id = controller.getIdentifier();
		logger.log(Level.FINE, "Making identifier {0} available to MUSCLE", id);
		if (id.isResolved()) {
			this.addResolvedIdentifier(id);
		} else {
			throw new IllegalStateException("Controller must be resolved to make it available.");
		}
		delegate.propagate(id);
	}
	
	/** Deregisters a local InstanceController. */
	public boolean deregister(InstanceController controller) {
		Identifier id = controller.getIdentifier();
		logger.log(Level.FINE, "Deregistering identifier {0}", id);
		removeIdentifier(id.getName(), id.getType());
		return delegate.delete(id);
	}
	
	/**
	 * Removes the identifier with given name and type from the resolver.
	 */
	public synchronized void removeIdentifier(String name, IDType type) {
		String fullName = name(name, type);
		// Still know that it was known, but make it inactive.
		Identifier id = this.resolvedIdCache.get(fullName);
		if (id != null) {
			id.unResolve();
		}
	}
	
	/** Add an identifier to the resolver. This also removes it from any 
	 *  search list it might be on.
	 */
	public synchronized void addResolvedIdentifier(Identifier id) {
		if (!id.isResolved()) {
			throw new IllegalArgumentException("ID " + id + " is not resolved, but Resolver only accepts resolved IDs");
		}
		
		String fullName = name(id.getName(), id.getType());
		Identifier resId = resolvedIdCache.get(fullName);
		if (resId == null) {
			resolvedIdCache.put(fullName, id);
		} else if (!resId.isResolved()) {
			// It already registered and deregistered
			id.unResolve();
		}
		searchingNow.remove(fullName);
		
		notifyAll();
	}
	
	public synchronized void canNotResolveIdentifier(Identifier id) {
		if (id.isResolved()) {
			throw new IllegalArgumentException("ID " + id + " is resolved, so it can be resolved.");
		}
		
		String fullName = name(id.getName(), id.getType());
		resolvedIdCache.put(fullName, id);
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
	
	@Override
	public synchronized boolean isDisposed() {
		return this.isDone;
	}

	@Override
	public boolean identifierMayActivate(Identifier id) {
		if (id.getType() == IDType.port) {
			return identifierMayActivate(((PortalID)id).getOwnerID());
		}
		synchronized (registeredHere) {
			if (registeredHere.contains(id.getName())) {
				return true;
			}
		}
		return delegate.willActivate(id);
	}
}
