package muscle.client.id;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
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
	private final ConcurrentMap<String,Identifier> idCache;
	private final Set<String> expecting;
	private final Set<String> registeredHere;
	private Location here;
	private final Set<String> searchingNow;
	private volatile boolean isDone;
	private final static Logger logger = Logger.getLogger(DelegatingResolver.class.getName());
	private final ConcurrentHashMap<String, Identifier> canonicalIds;
	
	public DelegatingResolver(IDManipulator newDelegate, Set<String> expecting) {
		delegate = newDelegate;
		idCache = new ConcurrentHashMap<String,Identifier>();
		canonicalIds = new ConcurrentHashMap<String,Identifier>();
		searchingNow = Collections.newSetFromMap(new ConcurrentHashMap<String,Boolean>());
		registeredHere = Collections.newSetFromMap(new ConcurrentHashMap<String,Boolean>());
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
		Identifier id = idCache.get(fullName);
		if (id == null) {
			logger.log(Level.FINER, "Creating identifier ''{0}'' of type {1}", new Object[]{name, type});
			id = delegate.create(name, type);
			idCache.put(fullName, id);
		} else {
			logger.log(Level.FINEST, "Returning identifier ''{0}'' of type {1} from cache", new Object[]{name, type});
		}
		return id;
	}
	
	private Identifier canonicalId(Identifier id) {
		if (id.getType() == IDType.port) {
			id = ((PortalID)id).getOwnerID();
		}
		String fullName = name(id.getName(), id.getType());
		Identifier altId = canonicalIds.putIfAbsent(fullName, id);
		if (isDisposed()) {
			if (id.canBeResolved()) {
				id.willNotBeResolved();
				synchronized (id) {
					id.notifyAll();
				}
			}
			if (altId != null && altId.canBeResolved()) {
				altId.willNotBeResolved();
				synchronized (altId) {
					altId.notifyAll();
				}
			}
		}
		if (altId != null) {
			id = altId;
		}
		return id;
	}
	
	/**
	 * Resolves an identifier, waiting until this is finished.
	 * 
	 * @param id will be resolved if there is no error. Always check isResolved() function afterwards.
	 * @throws InterruptedException if the process was interrupted before the id was resolved.
	 */
	public boolean resolveIdentifier(Identifier origId) throws InterruptedException  {
		Identifier id = canonicalId(origId);
		
		if (id.isResolved()) {
			if (!origId.isResolved()) {
				origId.resolveLike(id);
			}
			return true;
		}
		
		// See if we have a resolved id in cache
		String fullName = name(id.getName(), id.getType());
		
		if (!isLocal(id)) {
			boolean searching = false;
			synchronized (id) {
				// add a search only if this instance is not already searchd for
				if (!searchingNow.contains(fullName)) {
					searching = true;
					searchingNow.add(fullName);
				}
			}

			if (searching) {
				logger.log(Level.FINE, "Searching to resolve identifier {0}", id);
				delegate.search(id);
				synchronized (id) {
					searchingNow.remove(fullName);
				}
			}
		}

		synchronized (id) {
			// When a search was not conducted, there is a search going
			while (!id.isResolved() && id.canBeResolved()) {
				id.wait();
			}
		}
		
		if (id.isResolved()) {
			if (!origId.isResolved()) {
				origId.resolveLike(id);
			}
			logger.log(Level.FINE, "Identifier {0} resolved", id);
			return true;
		} else {
			logger.log(Level.WARNING, "Identifier {0} could not be resolved", id);
			return false;
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
	public void removeIdentifier(String name, IDType type) {
		// Still know that it was known, but make it inactive.
		Identifier id = canonicalId(this.getIdentifier(name, type));
		id.willNotBeResolved();
	}
	
	/** Add an identifier to the resolver. This also removes it from any 
	 *  search list it might be on.
	 */
	public void addResolvedIdentifier(Identifier origId) {
		if (!origId.isResolved()) {
			throw new IllegalArgumentException("ID " + origId + " is not resolved, but Resolver only accepts resolved IDs");
		}
		
		Identifier id = canonicalId(origId);
		if (!id.canBeResolved()) {
			origId.willNotBeResolved();
		} else if (!id.isResolved()) {
			id.resolveLike(origId);
		}
		
		synchronized (id) {
			id.notifyAll();
		}
	}
	
	public void canNotResolveIdentifier(Identifier origId) {
		if (origId.isResolved()) {
			throw new IllegalArgumentException("ID " + origId + " is resolved, so it can be resolved.");
		}
		
		Identifier id = canonicalId(origId);
		id.willNotBeResolved();
		
		synchronized (id) {
			id.notifyAll();
		}
	}
	
	private static String name(String name, IDType type) {
		return name + "#" + type.name();
	}
	
	public void dispose() {
		this.isDone = true;
		synchronized (registeredHere) {
			registeredHere.notifyAll();
		}
		for (Identifier id : this.canonicalIds.values()) {
			if (id.canBeResolved()) {
				id.willNotBeResolved();
				synchronized (id) {
					id.notifyAll();
				}
			}
		}
	}
	
	@Override
	public boolean isDisposed() {
		return this.isDone;
	}
	
	@Override
	public boolean identifierMayActivate(Identifier id) {
		if (id.getType() == IDType.port) {
			return identifierMayActivate(((PortalID)id).getOwnerID());
		}
		id = canonicalId(id);
		synchronized (registeredHere) {
			if (registeredHere.contains(id.getName())) {
				return true;
			}
		}
		return delegate.willActivate(id);
	}
}
