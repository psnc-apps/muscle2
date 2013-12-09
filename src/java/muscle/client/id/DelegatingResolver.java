/*
* Copyright 2008, 2009 Complex Automata Simulation Technique (COAST) consortium
* Copyright 2010-2013 Multiscale Applications on European e-Infrastructures (MAPPER) project
*
* GNU Lesser General Public License
* 
* This file is part of MUSCLE (Multiscale Coupling Library and Environment).
* 
* MUSCLE is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
* 
* MUSCLE is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Lesser General Public License for more details.
* 
* You should have received a copy of the GNU Lesser General Public License
* along with MUSCLE.  If not, see <http://www.gnu.org/licenses/>.
*/
package muscle.client.id;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import muscle.core.kernel.InstanceController;
import muscle.id.IDType;
import muscle.id.Identifier;
import muscle.id.Location;
import muscle.id.PortalID;
import muscle.id.Resolver;

/**
 * A simple resolver that delegates the actual creation and searching of Identifiers and Locations.
 * @author Joris Borgdorff
 */
public class DelegatingResolver implements Resolver {
	protected final IDManipulator delegate;
	/** Stores all resolved IDs */
	private final Set<String> expecting;
	private final Set<Identifier> registeredHere;
	private final Location here;
	private final Set<Identifier> searchingNow;
	private volatile boolean isDone;
	private final static Logger logger = Logger.getLogger(DelegatingResolver.class.getName());
	private final ConcurrentHashMap<String, Identifier> canonicalIds;
	
	public DelegatingResolver(IDManipulator newDelegate, Set<String> expecting) {
		delegate = newDelegate;
		canonicalIds = new ConcurrentHashMap<String,Identifier>();
		searchingNow = Collections.newSetFromMap(new ConcurrentHashMap<Identifier,Boolean>());
		registeredHere = Collections.newSetFromMap(new ConcurrentHashMap<Identifier,Boolean>());
		here = delegate.getLocation();
		this.expecting = expecting;
		this.isDone = false;
	}
	
	/**
	 * Gets a current identifier based on the name and type, or creates
	 * a new one if none is available.
	 * @param name Name of the new identifier
	 * @param type Type of identifier, currently port or instance
	 * @return an identifier
	 */
	@Override
	public Identifier getIdentifier(String name, IDType type) {
		if (type == IDType.instance) {
			// Try cache first, not synchronized as making a new id is not expensive
			Identifier id = canonicalIds.get(name);
			if (id == null) {
				return canonicalId(delegate.create(name, IDType.instance));
			} else {
				return id;
			}
		} else {
			return delegate.create(name, type);
		}
	}
	
	private Identifier canonicalId(final Identifier origId) {
		final Identifier id = getProperId(origId);

		Identifier altId = canonicalIds.putIfAbsent(id.getName(), id);
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
		return altId == null ? id : altId;
	}
	
	/**
	 * Resolves an identifier, waiting until this is finished.
	 * Always check the return value or Identifier.isResolved() function afterwards.
	 * @param origId will be resolved if there is no error. 
	 * @return whether the ID is available (has not yet stopped)
	 * @throws InterruptedException if the process was interrupted before the id was resolved.
	 */
	@Override
	public boolean resolveIdentifier(Identifier origId) throws InterruptedException  {
		Identifier id = canonicalId(origId);
		
		if (id.isAvailable()) {
			if (!origId.isAvailable()) {
				origId.resolveLike(id);
			}
			return true;
		}
		
		// See if we have a resolved id in cache
		if (!isLocal(id)) {
			boolean searching = false;
			synchronized (id) {
				// add a search only if this instance is not already searchd for
				if (!searchingNow.contains(id)) {
					searching = true;
					searchingNow.add(id);
				}
			}

			if (searching) {
				logger.log(Level.FINE, "Searching to resolve identifier {0}", id);
				delegate.search(id);
				if (id.isResolved()) {
					logger.log(Level.FINE, "Identifier {0} resolved", id);
				} else {
					logger.log(Level.WARNING, "Identifier {0} could not be resolved", id);
				}
				synchronized (id) {
					searchingNow.remove(id);
				}
			}
		}

		synchronized (id) {
			// When a search was not conducted, there is a search going
			while (!id.isAvailable() && id.canBeResolved()) {
				id.wait();
			}
		}
		
		if (id.isAvailable()) {
			if (!origId.isAvailable()) {
				origId.resolveLike(id);
			}
			return true;
		} else {
			return false;
		}
	}

	/**
	 * whether the id resides in the current location
	 * @param origId Identifier to be checked
	 * @return whether the id resides in the current location */
	@Override
	public boolean isLocal(Identifier origId) {
		Identifier id = getProperId(origId);
		String name = id.getName();

		try {
			synchronized (registeredHere) {
				while (!this.isDisposed() && expecting.contains(name) && !registeredHere.contains(id)) {
					registeredHere.wait();
				}
				return !this.isDisposed() && registeredHere.contains(id);
			}
		} catch (InterruptedException ex) {
			Logger.getLogger(DelegatingResolver.class.getName()).log(Level.SEVERE, "Could not determine whether id " + id + " is local.", ex);
			return false;
		}
	}

	/** Registers a local InstanceController.
	 * @param controller controller of instance to be registered
	 * @return whether the instance was the first of its name to be registered
	 */
	@Override
	public boolean register(InstanceController controller) {
		Identifier id = controller.getIdentifier();
		logger.log(Level.FINE, "Registering identifier {0}", id);
		boolean ret = delegate.register(id, here);
		synchronized (registeredHere) {
			if (ret) {
				registeredHere.add(id);
			} else {
				expecting.remove(id.getName());
			}
			registeredHere.notifyAll();
		}
		return ret;
	}
	
	/**
	 * Makes the instance available for communication with other instances
	 * @param controller controller of the available instance
	 */
	@Override
	public void makeAvailable(InstanceController controller) {
		Identifier id = controller.getIdentifier();
		logger.log(Level.FINER, "Making identifier {0} available to MUSCLE", id);
		if (id.isResolved()) {
			id.setAvailable(true);
			this.addAvailableIdentifier(id);
		} else {
			throw new IllegalStateException("Controller must be resolved to make it available.");
		}
		delegate.propagate(id);
	}
	
	/**
	 * Deregisters a local Instance.
	 * @param controller controller of the instance to be deregistered
	 */
	@Override
	public void deregister(InstanceController controller) {
		Identifier id = controller.getIdentifier();
		logger.log(Level.FINE, "Deregistering identifier {0}", id);
		removeIdentifier(id.getName(), id.getType());
		delegate.delete(id);
	}
	
	/**
	 * Removes the identifier with from the resolver, setting that it will not resolve.
	 * @param name name of the identifier to be removed
	 * @param type type of identifier to be removed, resolving to the instance identifier
	 */
	@Override
	public void removeIdentifier(String name, IDType type) {
		// Still know that it was known, but make it inactive.
		canonicalId(this.getIdentifier(name, type)).willNotBeResolved();
	}
	
	/**
	 * Add an identifier to the resolver. This removes it from any 
	 *  search list it might be on.
	 * @param origId identifier to be made available
	 * @throws IllegalArgumentException if the provided origId is not resolved (or isAvailable() is false)
	 */
	@Override
	public void addAvailableIdentifier(Identifier origId) {
		if (!origId.isAvailable()) {
			throw new IllegalArgumentException("ID " + origId + " is not resolved, but Resolver only accepts resolved IDs");
		}
		
		Identifier id = canonicalId(origId);
		if (!id.isAvailable()) {
			if (!id.canBeResolved()) {
				origId.willNotBeResolved();
			} else if (!id.isResolved()) {
				id.resolveLike(origId);
			} else {
				id.setAvailable(true);
			}
		}
		
		synchronized (id) {
			id.notifyAll();
		}
	}
	
	/**
	 * Set that an identifier will not be able to be resolved because it stopped.
	 * The identifier will be made unavailable.
	 * @param origId identifier to de-resolve
	 */
	@Override
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
	
	@Override
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
	
	/**
	 * Whether given identifier has not yet quit.
	 * @param origId identifier to check
	 * @return whether it can still activate
	 */
	@Override
	public boolean identifierMayActivate(final Identifier origId) {
		final Identifier id = getProperId(origId);
		synchronized (registeredHere) {
			if (registeredHere.contains(id)) {
				return true;
			}
		}
		return delegate.willActivate(id);
	}
	
	private Identifier getProperId(Identifier id) {
		if (id.getType() == IDType.port) {
			return ((PortalID)id).getOwnerID();
		} else {
			return id;
		}
	}
}
