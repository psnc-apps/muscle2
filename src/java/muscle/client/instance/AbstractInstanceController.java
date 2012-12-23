/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package muscle.client.instance;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import muscle.client.LocalManager;
import muscle.client.communication.PortFactory;
import muscle.core.ConduitDescription;
import muscle.core.ConduitEntranceController;
import muscle.core.ConduitExitController;
import muscle.core.ConnectionScheme;
import muscle.core.DataTemplate;
import muscle.core.EntranceDescription;
import muscle.core.ExitDescription;
import muscle.core.PortDescription;
import muscle.core.conduit.terminal.Sink;
import muscle.core.conduit.terminal.Source;
import muscle.core.kernel.InstanceController;
import muscle.core.kernel.InstanceControllerListener;
import muscle.core.kernel.RawInstance;
import muscle.id.Identifier;
import muscle.id.InstanceClass;
import muscle.id.PortalID;
import muscle.id.Resolver;
import muscle.id.ResolverFactory;

/**
 *
 * @author Joris Borgdorff
 */
public abstract class AbstractInstanceController implements InstanceController {
	private final static Logger logger = Logger.getLogger(AbstractInstanceController.class.getName());
	private final static boolean ENTRANCE = true;
	private final static boolean EXIT = false;
	
	private Map<String, ExitDescription> exitDescriptions;
	private Map<String, EntranceDescription> entranceDescriptions;
	
	protected RawInstance instance;
	private boolean isDone;
	
	protected final Class<?> instanceClass;
	protected final Identifier id;
	protected final List<ConduitExitController<?>> exits = new ArrayList<ConduitExitController<?>>(); // these are the conduit exits
	protected final List<ConduitEntranceController<?>> entrances = new ArrayList<ConduitEntranceController<?>>(); //these are the conduit entrances
	protected final InstanceControllerListener listener;
	private final ResolverFactory resolverFactory;
	private final PortFactory portFactory;
	private boolean isExecuting;

	public AbstractInstanceController(InstanceClass instanceClass, InstanceControllerListener listener, ResolverFactory rf, PortFactory portFactory) {
		this.id = instanceClass.getIdentifier();
		this.instanceClass = instanceClass.getInstanceClass();
		this.instance = null;
		this.listener = listener;
		this.resolverFactory = rf;
		this.portFactory = portFactory;
		this.isDone = false;
		this.isExecuting = false;
	}
	
	public boolean init() {
		ConnectionScheme cs = ConnectionScheme.getInstance();
		this.exitDescriptions = cs.exitDescriptionsForIdentifier(id);
		this.entranceDescriptions = cs.entranceDescriptionsForIdentifier(id);
		try {
			instance = (RawInstance) this.instanceClass.newInstance();
			instance.setInstanceController(this);
		} catch (InstantiationException ex) {
			logger.log(Level.SEVERE, "Could not instantiate Instance " + getName() + " with class " + this.instanceClass.getName(), ex);
			return false;
		} catch (IllegalAccessException ex) {
			logger.log(Level.SEVERE, "Permission denied to class " + this.instanceClass.getName() + " of Instance " + getName(), ex);
			return false;
		}
		return true;
	}
	
	public synchronized boolean isDisposed() {
		return isDone;
	}
	
	@Override
	public String getName() {
		return id.getName();
	}
	
	public void dispose() {
		synchronized (this) {
			if (isDisposed()) {
				return;
			}
			isDone = true;
		}
		// Deregister with the resolver
		try {
			Resolver r = resolverFactory.getResolver();
			r.deregister(this);
		} catch (InterruptedException ex) {
			logger.log(Level.SEVERE, "Could not deregister {0}: {1}", new Object[] {getName(), ex});
		}

		this.disposeNoDeregister();
	}
	
	@Override
	public void fatalException(Throwable ex) {
		logger.log(Level.SEVERE, "Fatal exception occurred in instance " + getName() + "; shutting down platform.", ex);
		LocalManager.getInstance().shutdown(3);
	}
	
	@Override
	public <T extends Serializable> ConduitExitController<T> createConduitExit(boolean threaded, String portalName, DataTemplate newDataTemplate) {
		@SuppressWarnings("unchecked")
		PortalID currentID = new PortalID(portalName, getIdentifier());
		PortalID otherID = getOtherPortalID(currentID, EXIT);
		
		EntranceDescription desc = ConnectionScheme.getInstance().entranceDescriptionForPortal(otherID);
		if (desc == null) {
			throw new IllegalArgumentException("Can not create ConduitExit " + currentID + ": it is not defined in the CxA file.");
		}
		List<String> portArgs = desc.getArgs();
		ConduitExitController<T> exit;
		if (portArgs.isEmpty()) {
			@SuppressWarnings("unchecked")
			ConduitExitControllerImpl<T> s = new PassiveConduitExitController<T>(currentID, this, newDataTemplate, threaded);
			portFactory.getReceiver(this, s, otherID);
			exit = s;
		} else {
			String portName = portArgs.get(0);
			try {
				Object srcObj =Class.forName(portName).newInstance();
				if (!(srcObj instanceof Source)) {
					throw new IllegalArgumentException("Given class " + portName + " is not a Source ");
				}
				@SuppressWarnings("unchecked")
				Source<T> src = (Source<T>) srcObj;
				src.setIdentifier(otherID);
				src.beforeExecute();
				exit = src;
			} catch (ClassNotFoundException ex) {
				throw new IllegalArgumentException("Can not find class for Source '" + portName + "' for " + currentID, ex);
			} catch (InstantiationException ex) {
				throw new IllegalArgumentException("Can not instantiate Source '" + portName + "' for " + currentID, ex);
			} catch (IllegalAccessException ex) {
				throw new IllegalArgumentException("Can not access Source '" + portName + "' for " + currentID, ex);
			}
		}
		exits.add(exit);
		return exit;
	}
	
	
	@Override
	public <T extends Serializable> ConduitEntranceController<T> createConduitEntrance(boolean threaded, boolean shared, String portalName, DataTemplate newDataTemplate) {
		@SuppressWarnings("unchecked")
		PortalID currentID = new PortalID(portalName, getIdentifier());
		PortalID otherID = getOtherPortalID(currentID, ENTRANCE);
		ConduitEntranceController<T> entrance;
		ExitDescription desc = ConnectionScheme.getInstance().exitDescriptionForPortal(otherID);
		if (desc == null) {
			throw new IllegalArgumentException("Can not create ConduitEntrance " + currentID + ": it is not defined in the CxA file.");
		}
		List<String> portArgs = desc.getArgs();
		if (portArgs.isEmpty()) {
			@SuppressWarnings("unchecked")
			ConduitEntranceControllerImpl<T> s = new PassiveConduitEntranceController<T>(currentID, this, newDataTemplate, threaded);
			portFactory.<T>getTransmitter(this, s, otherID, shared);
			entrance = s;
		} else {
			String portName = portArgs.get(0);
			try {
				Object sinkObj =Class.forName(portName).newInstance();
				if (!(sinkObj instanceof Sink)) {
					throw new IllegalArgumentException("Given class " + portName + " is not a Sink ");
				}
				@SuppressWarnings("unchecked")
				Sink<T> sink = (Sink<T>) sinkObj;
				sink.setIdentifier(otherID);
				sink.beforeExecute();
				entrance = sink;
			} catch (ClassNotFoundException ex) {
				throw new IllegalArgumentException("Can not find class for Sink '" + portName + "' for " + currentID, ex);
			} catch (InstantiationException ex) {
				throw new IllegalArgumentException("Can not instantiate Sink '" + portName + "' for " + currentID, ex);
			} catch (IllegalAccessException ex) {
				throw new IllegalArgumentException("Can not access Sink '" + portName + "' for " + currentID, ex);
			}
		}
		entrances.add(entrance);
		return entrance;
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + getIdentifier() + "]";
	}

	@Override
	public Identifier getIdentifier() {
		return this.id;
	}
	
		/** Disposes the current instance, without deregistering it.
	 *   It will only be executed once per instance, after this it becomes a no-op.
	 */
	protected void disposeNoDeregister() {
		synchronized (this) {
			isDone = true;
		}
		
		for (ConduitExitController<?> source : exits) {
			portFactory.removeReceiver(source.getIdentifier());
			source.dispose();
		}
		for (ConduitEntranceController<?> sink : entrances) {
			sink.dispose();
		}
		
		if (this.isExecuting()) {
			// probably the agent has been killed and did not call its afterExecute
			afterExecute();
		}
	}
	
		
	private PortalID getOtherPortalID(PortalID id, boolean entrance) {
		ConduitDescription desc = null;
		PortDescription port = null;
		Map<String,? extends PortDescription> descriptions = entrance ? entranceDescriptions : exitDescriptions;
		
		if (descriptions != null) {
			port = descriptions.get(id.getPortName());
		}
		if (port == null) {
			throw new IllegalStateException("Port " + id + " is initialized in code but is not listed in the connection scheme. It will not work until this port is added in the connection scheme.");
		} else {
			desc = port.getConduitDescription();
		}
		if (desc == null) {
			throw new IllegalStateException("Port " + id + " is initialized in code but is not listed in the connection scheme. It will not work until this port is added in the connection scheme.");
		}

		if (entrance) {
			return desc.getExitDescription().getID();
		} else {
			return desc.getEntranceDescription().getID();
		}
	}
				
	protected synchronized void beforeExecute() {
		this.isExecuting = true;
	}

	public synchronized void afterExecute() {
		instance.afterExecute();
		this.isExecuting = false;
	}

	public boolean isExecuting() {
		return this.isExecuting;
	}
	
	protected boolean register() {
		try {
			Resolver locator = resolverFactory.getResolver();
			return locator.register(this);
		} catch (InterruptedException ex) {
			Logger.getLogger(ThreadedInstanceController.class.getName()).log(Level.SEVERE, null, ex);
			return false;
		} catch (Exception ex) {
			return false;
		}
	}
	
	protected void propagate() {
		try {
			Resolver locator = resolverFactory.getResolver();
			locator.makeAvailable(this);
		} catch (InterruptedException ex) {
			Logger.getLogger(ThreadedInstanceController.class.getName()).log(Level.SEVERE, null, ex);
		} catch (Exception ex) {
		}
	}
}
