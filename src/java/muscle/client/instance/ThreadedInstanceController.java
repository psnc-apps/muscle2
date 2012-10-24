/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package muscle.client.instance;

import eu.mapperproject.jmml.util.FastArrayList;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import muscle.Constant;
import muscle.client.LocalManager;
import muscle.client.communication.PortFactory;
import muscle.core.*;
import muscle.core.conduit.terminal.Sink;
import muscle.core.conduit.terminal.Source;
import muscle.core.kernel.InstanceController;
import muscle.core.kernel.InstanceControllerListener;
import muscle.core.kernel.RawInstance;
import muscle.exception.MUSCLEConduitExhaustedException;
import muscle.exception.MUSCLEDatatypeException;
import muscle.id.Identifier;
import muscle.id.PortalID;
import muscle.id.Resolver;
import muscle.id.ResolverFactory;
import muscle.util.FileTool;
import muscle.util.JVM;

/**
 *
 * @author Joris Borgdorff
 */
public class ThreadedInstanceController implements Runnable, InstanceController {
	private final static Logger logger = Logger.getLogger(ThreadedInstanceController.class.getName());
	private final static boolean ENTRANCE = true;
	private final static boolean EXIT = false;
	
	private final Class<?> instanceClass;
	private final Identifier id;
	private final List<ConduitExitController<?>> exits = new ArrayList<ConduitExitController<?>>(); // these are the conduit exits
	private final List<ConduitEntranceController<?>> entrances = new ArrayList<ConduitEntranceController<?>>(); //these are the conduit entrances
	private final InstanceControllerListener listener;
	private final Object[] args;
	private final ResolverFactory resolverFactory;
	private final PortFactory portFactory;
	
	private RawInstance instance;
	private boolean execute;
	private File infoFile;
	private InstanceController mainController;
	private boolean isDone;
	private boolean isExecuting;
	
	private Map<String, ExitDescription> exitDescriptions;
	private Map<String, EntranceDescription> entranceDescriptions;

	public ThreadedInstanceController(Identifier id, Class<?> instanceClass, InstanceControllerListener listener, ResolverFactory rf, Object[] args, PortFactory portFactory) {
		this.id = id;
		this.instanceClass = instanceClass;
		this.instance = null;
		this.execute = true;
		this.listener = listener;
		this.args = args;
		this.resolverFactory = rf;
		this.isDone = false;
		this.portFactory = portFactory;
		this.isExecuting = false;
		this.mainController = this;
	}
	
	public void setMainController(InstanceController ic) {
		this.mainController = ic;
	}
	
	public boolean isExecuting() {
		return this.isExecuting;
	}
	
	public synchronized boolean isDisposed() {
		return isDone;
	}
	
	@Override
	public void run() {		
		logger.log(Level.INFO, "{0}: connecting...", getLocalName());
		
		ConnectionScheme cs = ConnectionScheme.getInstance();
		this.exitDescriptions = cs.exitDescriptionsForIdentifier(id);
		this.entranceDescriptions = cs.entranceDescriptionsForIdentifier(id);
		
		try {
			instance = (RawInstance) this.instanceClass.newInstance();

			instance.setInstanceController(this.mainController);

			instance.beforeExecute();

			instance.setArguments(initFromArgs(args));

			if (!register()) {
				logger.log(Level.SEVERE, "Could not register {0}; it may already have been registered. {0} was halted.", getLocalName());
				if (!this.isDisposed()) {
					this.disposeNoDeregister();
					listener.isFinished(this);
				}
				return;
			}
			instance.connectPortals();
			propagate();

			// log info about this controller
			Level logLevel = Level.INFO;
			if (logger.isLoggable(logLevel)) {
				logger.log(logLevel, instance.infoText());
			}

			if (execute) {
				beforeExecute();
				logger.log(Level.INFO, "{0}: executing", getLocalName());
				try {
					instance.start();
				} catch (MUSCLEConduitExhaustedException ex) {
					logger.log(Level.SEVERE, getLocalName() + " was prematurely halted, by trying to receive a message from a stopped submodel.", ex);
					LocalManager.getInstance().shutdown(6);
				} catch (MUSCLEDatatypeException ex) {
					logger.log(Level.SEVERE, getLocalName() + " communicated a wrong data type. Check the coupling.", ex);
					LocalManager.getInstance().shutdown(7);
				} catch (Exception ex) {
					StringWriter sw = new StringWriter();
					PrintWriter pw = new PrintWriter(sw);
					ex.printStackTrace(pw);
					try {
						pw.close(); sw.close();
					} catch (IOException ex1) {
						Logger.getLogger(ThreadedInstanceController.class.getName()).log(Level.SEVERE, null, ex1);
					}
					logger.log(Level.SEVERE, "{0} was halted due to an error.\n====TRACE====\n{1}==END TRACE==", new Object[]{getLocalName(), sw});
					LocalManager.getInstance().shutdown(8);
				}
				try {
					for (ConduitEntranceController ec : entrances) {
						if (!ec.waitUntilEmpty()) {
							logger.log(Level.WARNING, "After executing {0}, waiting for conduit {1}  was ended prematurely", new Object[]{getLocalName(), ec.getLocalName()});
						}
					}
				} catch (InterruptedException ex) {
					logger.log(Level.SEVERE, "After executing " + getLocalName() + ", waiting for conduit was interrupted", ex);
				}
				afterExecute();
				logger.log(Level.INFO, "{0}: finished", getLocalName());
				dispose();
			}
		} catch (InstantiationException ex) {
			logger.log(Level.SEVERE, "Could not instantiate Instance " + getLocalName() + " with class " + this.instanceClass.getName(), ex);
		} catch (IllegalAccessException ex) {
			logger.log(Level.SEVERE, "Permission denied to class " + this.instanceClass.getName() + " of Instance " + getLocalName(), ex);
		} catch (OutOfMemoryError er) {
			logger.log(Level.SEVERE, "Instance " + getLocalName() + " is out of memory", er);
			LocalManager.getInstance().shutdown(2);
		}
	}

	@Override
	public String getLocalName() {
		return id.getName();
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
			r.deregister(this.mainController == null ? this : this.mainController);
		} catch (InterruptedException ex) {
			logger.log(Level.SEVERE, "Could not deregister {0}: {1}", new Object[] {getLocalName(), ex});
		}

		listener.isFinished(this);

		this.disposeNoDeregister();
	}
	
	/** Disposes the current instance, without deregistering it.
	 *   It will only be executed once per instance, after this it becomes a no-op.
	 */
	private void disposeNoDeregister() {
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
		
	private synchronized void beforeExecute() {
		this.isExecuting = true;
	}

	public synchronized void afterExecute() {
		this.isExecuting = false;
	}
	
	private boolean register() {
		try {
			Resolver locator = resolverFactory.getResolver();
			return locator.register(this.mainController);
		} catch (InterruptedException ex) {
			Logger.getLogger(ThreadedInstanceController.class.getName()).log(Level.SEVERE, null, ex);
			return false;
		} catch (Exception ex) {
			return false;
		}
	}
	
	private void propagate() {
		try {
			Resolver locator = resolverFactory.getResolver();
			locator.makeAvailable(this.mainController);
		} catch (InterruptedException ex) {
			Logger.getLogger(ThreadedInstanceController.class.getName()).log(Level.SEVERE, null, ex);
		} catch (Exception ex) {
		}
	}
	
	private Object[] initFromArgs(Object[] rawArgs) {
		if (rawArgs == null || rawArgs.length == 0) {
			return rawArgs;
		}

		List<Object> list = new FastArrayList<Object>(rawArgs);
		KernelArgs kargs = null;
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i) instanceof KernelArgs) {
				kargs = (KernelArgs)list.remove(i);
				break;
			}
			if (list.get(i) instanceof String) {
				String str = (String)list.remove(i);
				try {
					kargs = new KernelArgs(str);
				} catch (IllegalArgumentException ex) {
					list.add(i, str);
					// the string has wrong format for our args, re-add it to the original arguments
				}
				// Don't break for string, a KernelArgs might still come by
			}
		}
		
		if (kargs != null) {
			execute = kargs.execute;
			return list.toArray();
		}
		
		return rawArgs;
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + getIdentifier() + "]";
	}

	@Override
	public Identifier getIdentifier() {
		return this.id;
	}

	@Override
	public <T extends Serializable> ConduitEntranceController<T> createConduitEntrance(boolean threaded, String portalName, DataTemplate newDataTemplate) {
		PortalID currentID = new PortalID(portalName, getIdentifier());
		PortalID otherID = getOtherPortalID(currentID, ENTRANCE);
		ConduitEntranceController<T> entrance;
		ExitDescription desc = ConnectionScheme.getInstance().exitDescriptionForPortal(otherID);
		if (desc == null) {
			throw new IllegalArgumentException("Can not create ConduitEntrance " + currentID + ": it is not defined in the CxA file.");
		}
		List<String> portArgs = desc.getArgs();
		if (portArgs.isEmpty()) {
			ConduitEntranceControllerImpl<T> s = threaded ? new ThreadedConduitEntranceController<T>(currentID, this, newDataTemplate) : new PassiveConduitEntranceController<T>(currentID, this, newDataTemplate);
			portFactory.<T>getTransmitter(this.mainController, s, otherID);
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
	public <T extends Serializable> ConduitExitController<T> createConduitExit(boolean threaded, String portalName, DataTemplate newDataTemplate) {
		PortalID currentID = new PortalID(portalName, getIdentifier());
		PortalID otherID = getOtherPortalID(currentID, EXIT);
		
		EntranceDescription desc = ConnectionScheme.getInstance().entranceDescriptionForPortal(otherID);
		if (desc == null) {
			throw new IllegalArgumentException("Can not create ConduitExit " + currentID + ": it is not defined in the CxA file.");
		}
		List<String> portArgs = desc.getArgs();
		ConduitExitController<T> exit;
		if (portArgs.isEmpty()) {
			ConduitExitControllerImpl<T> s = threaded ? new ThreadedConduitExitController<T>(currentID, this, newDataTemplate) : new PassiveConduitExitController<T>(currentID, this, newDataTemplate);
			portFactory.<T>getReceiver(this.mainController, s, otherID);
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
	public void fatalException(Throwable ex) {
		logger.log(Level.SEVERE, "Fatal exception occurred in instance " + getLocalName() + "; shutting down platform.", ex);
		LocalManager.getInstance().shutdown(3);
	}
}
