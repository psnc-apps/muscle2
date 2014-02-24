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
import muscle.core.conduit.terminal.Sink;
import muscle.core.conduit.terminal.Source;
import muscle.core.kernel.InstanceController;
import muscle.core.kernel.InstanceControllerListener;
import muscle.core.kernel.RawInstance;
import muscle.id.Identifier;
import muscle.id.InstanceClass;
import muscle.id.PortalID;
import muscle.id.Resolver;
import muscle.util.logging.ActivityListener;

/**
 *
 * @author Joris Borgdorff
 */
public abstract class AbstractInstanceController implements InstanceController {
	private final static Logger logger = Logger.getLogger(AbstractInstanceController.class.getName());
	private final static boolean ENTRANCE = true;
	private final static boolean EXIT = false;
	
	private Map<String, ConduitDescription> exitDescriptions;
	private Map<String, ConduitDescription> entranceDescriptions;
	
	protected RawInstance instance;
	private boolean isDone;
	
	protected final Class<?> instanceClass;
	protected final Identifier id;
	protected final List<ConduitExitController<?>> exits = new ArrayList<ConduitExitController<?>>(); // these are the conduit exits
	protected final List<ConduitEntranceController<?>> entrances = new ArrayList<ConduitEntranceController<?>>(); //these are the conduit entrances
	protected final InstanceControllerListener listener;
	protected final ActivityListener actLogger;

	private final Resolver resolver;
	private final PortFactory portFactory;
	private boolean isExecuting;

	public AbstractInstanceController(InstanceClass instanceClass, InstanceControllerListener listener, Resolver res, PortFactory portFactory, ConnectionScheme cs, ActivityListener actLogger) {
		this.id = instanceClass.getIdentifier();
		this.instanceClass = instanceClass.getInstanceClass();
		this.instance = null;
		this.listener = listener;
		this.resolver = res;
		this.portFactory = portFactory;
		this.isDone = false;
		this.isExecuting = false;
		this.entranceDescriptions = cs.entranceDescriptionsForIdentifier(id);
		this.exitDescriptions = cs.exitDescriptionsForIdentifier(id);
		this.actLogger = actLogger;
	}
	
	public boolean init() {
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
	
	@Override
	public synchronized boolean isDisposed() {
		return isDone;
	}
	
	@Override
	public String getName() {
		return id.getName();
	}
		
	@Override
	public void dispose() {
		synchronized (this) {
			if (isDisposed()) {
				return;
			}
			isDone = true;
		}
		// Deregister with the resolver
		this.resolver.deregister(this);

		this.disposeNoDeregister(true);
	}
	
	@Override
	public void fatalException(Throwable ex) {
		logger.log(Level.SEVERE, "Fatal exception occurred in instance " + getName() + "; shutting down platform.", ex);
		LocalManager.getInstance().shutdown(3);
	}
	
	@Override
	public <T extends Serializable> ConduitExitController<T> createConduitExit(boolean threaded, String portalName, Class<T> newDataClazz) {
		@SuppressWarnings("unchecked")
		PortalID currentID = new PortalID(portalName, getIdentifier());
		PortalID otherID = getOtherPortalID(currentID, EXIT);
		
		ConduitDescription desc = this.exitDescriptions.get(portalName);
		if (desc == null) {
			throw new IllegalArgumentException("Can not create ConduitExit " + currentID + ": it is not defined in the CxA file.");
		}
		String[] portArgs = desc.getEntranceArgs();
		ConduitExitController<T> exit;
		if (portArgs.length == 0) {
			@SuppressWarnings("unchecked")
			ConduitExitControllerImpl<T> s = new PassiveConduitExitController<T>(currentID, this, newDataClazz, threaded, desc, actLogger);
			portFactory.getReceiver(this, s, otherID);
			exit = s;
		} else {
			String portName = portArgs[0];
			try {
				Object srcObj =Class.forName(portName).newInstance();
				if (!(srcObj instanceof Source)) {
					throw new IllegalArgumentException("Given class " + portName + " is not a Source ");
				}
				@SuppressWarnings("unchecked")
				Source<T> src = (Source<T>) srcObj;
				src.setActivityLogger(actLogger);
				src.setIdentifier(otherID, currentID);
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
	public <T extends Serializable> ConduitEntranceController<T> createConduitEntrance(boolean threaded, boolean shared, String portalName, Class<T> newDataClazz) {
		@SuppressWarnings("unchecked")
		PortalID currentID = new PortalID(portalName, getIdentifier());
		PortalID otherID = getOtherPortalID(currentID, ENTRANCE);
		ConduitEntranceController<T> entrance;
		ConduitDescription desc = this.entranceDescriptions.get(portalName);
		if (desc == null) {
			throw new IllegalArgumentException("Can not create ConduitEntrance " + currentID + ": it is not defined in the CxA file.");
		}
		String[] portArgs = desc.getExitArgs();
		if (portArgs.length == 0) {
			@SuppressWarnings("unchecked")
			ConduitEntranceControllerImpl<T> s = new PassiveConduitEntranceController<T>(currentID, this, newDataClazz, threaded, desc, actLogger);
			portFactory.<T>getTransmitter(this, s, otherID, shared);
			entrance = s;
		} else {
			String portName = portArgs[0];
			try {
				Object sinkObj =Class.forName(portName).newInstance();
				if (!(sinkObj instanceof Sink)) {
					throw new IllegalArgumentException("Given class " + portName + " is not a Sink ");
				}
				@SuppressWarnings("unchecked")
				Sink<T> sink = (Sink<T>) sinkObj;
				sink.setActivityLogger(actLogger);
				sink.setIdentifier(otherID, currentID);
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
	 *   It will only be executed once per instance, after this it becomes a no-op, unless force is true.
	 * @param force whether the dispose will be executed again, even if already called
	 */
	protected void disposeNoDeregister(boolean force) {
		synchronized (this) {
			if (!force && this.isDisposed()) {
				return;
			}
			isDone = true;
		}
		
		for (ConduitExitController<?> in : exits) {
			portFactory.removeReceiver(in.getIdentifier());
			in.dispose();
		}
		for (ConduitEntranceController<?> out : entrances) {
			out.dispose();
		}
		
		if (this.isExecuting()) {
			// probably the agent has been killed and did not call its afterExecute
			afterExecute();
		}
	}
	
		
	private PortalID getOtherPortalID(PortalID id, boolean entrance) {
		ConduitDescription desc = null;
		Map<String,ConduitDescription> descriptions = entrance ? entranceDescriptions : exitDescriptions;
		
		if (descriptions != null) {
			desc = descriptions.get(id.getPortName());
		}
		if (desc == null) {
			throw new IllegalStateException("Port " + id + " is initialized in code but is not listed in the connection scheme. It will not work until this port is added in the connection scheme.");
		}

		if (entrance) {
			return desc.getExit();
		} else {
			return desc.getEntrance();
		}
	}
				
	protected synchronized void beforeExecute() {
		this.isExecuting = true;
	}

	public synchronized void afterExecute() {
		instance.afterExecute();
		this.isExecuting = false;
	}

	@Override
	public boolean isExecuting() {
		return this.isExecuting;
	}
	
	protected boolean register() {
		try {
			return this.resolver.register(this);
		} catch (Exception ex) {
			return false;
		}
	}
	
	protected void propagate() {
		// All ports are connected, so we don't need the descriptions anymore
		this.entranceDescriptions = null;
		this.exitDescriptions = null;
		this.resolver.makeAvailable(this);
	}
	
	@Override
	public Map<String,ConduitDescription> getExitDescriptions() {
		return this.exitDescriptions;
	}
	@Override
	public Map<String,ConduitDescription> getEntranceDescriptions() {
		return this.entranceDescriptions;
	}
}
