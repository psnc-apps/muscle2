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
import muscle.client.communication.PortFactory;
import muscle.client.communication.message.IncomingMessageProcessor;
import muscle.core.*;
import muscle.core.kernel.InstanceController;
import muscle.core.kernel.InstanceControllerListener;
import muscle.core.kernel.RawKernel;
import muscle.id.Identifier;
import muscle.id.PortalID;
import muscle.id.Resolver;
import muscle.id.ResolverFactory;
import muscle.util.JVM;
import muscle.util.MiscTool;

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
	private final List<ConduitExitControllerImpl<?>> exits = new ArrayList<ConduitExitControllerImpl<?>>(); // these are the conduit exits
	private final List<ConduitEntranceControllerImpl<?>> entrances = new ArrayList<ConduitEntranceControllerImpl<?>>(); //these are the conduit entrances
	private final InstanceControllerListener listener;
	private final Object[] args;
	private final ResolverFactory resolverFactory;
	private final PortFactory portFactory;
	
	private RawKernel instance;
	private boolean execute;
	private File infoFile;
	private InstanceController mainController;
	private boolean isDone, isExecuting;
	
	private Map<String, ExitDescription> exitDescriptions;
	private Map<String, EntranceDescription> entranceDescriptions;

	public ThreadedInstanceController(Identifier id, Class<?> instanceClass, InstanceControllerListener listener, ResolverFactory rf, Object[] args, PortFactory portFactory) {
		this.id = id;
		this.instanceClass = instanceClass;
		this.instance = null;
		this.execute = true;
		this.listener = listener;
		this.args = args;
		this.mainController = null;
		this.resolverFactory = rf;
		this.isDone = false;
		this.portFactory = portFactory;
		this.isExecuting = false;
	}
	
	public void setMainController(InstanceController ic) {
		this.mainController = ic;
	}
	
	public boolean isExecuting() {
		return this.isExecuting;
	}
	
	@Override
	public void run() {		
		logger.log(Level.INFO, "{0}: starting kernel", getLocalName());
		
		ConnectionScheme cs = ConnectionScheme.getInstance();
		this.exitDescriptions = cs.exitDescriptionsForIdentifier(id);
		this.entranceDescriptions = cs.entranceDescriptionsForIdentifier(id);
		
		try {
			instance = (RawKernel) this.instanceClass.newInstance();

			instance.setInstanceController(this);

			instance.beforeExecute();

			instance.setArguments(initFromArgs(args));

			if (!register()) {
				logger.log(Level.SEVERE, "Could not register {0}; it may already have been registered. Submodel execution aborted.", getLocalName());
				if (this.disposeNoDeregister()) {
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
				} catch (Exception ex) {
					StringWriter sw = new StringWriter();
					PrintWriter pw = new PrintWriter(sw);
					ex.printStackTrace(pw);
					try {
						pw.close(); sw.close();
					} catch (IOException ex1) {
						Logger.getLogger(ThreadedInstanceController.class.getName()).log(Level.SEVERE, null, ex1);
					}
					logger.log(Level.SEVERE, "{0} was terminated due to an error: {1}\n====TRACE====\n{2}==END TRACE==", new Object[]{getLocalName(), ex, sw});
				}
				try {
					for (ConduitEntranceControllerImpl ec : entrances) {
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
		
		if (descriptions != null)
			port = descriptions.get(id.getPortName());
		if (port == null)
			throw new IllegalStateException("Port " + id + " is initialized in code but is not listed in the connection scheme. It will not work until this port is added in the connection scheme.");
		else
			desc = port.getConduitDescription();
		if (desc == null)
			throw new IllegalStateException("Port " + id + " is initialized in code but is not listed in the connection scheme. It will not work until this port is added in the connection scheme.");

		if (entrance) {
			return desc.getExitDescription().getID();
		} else {
			return desc.getEntranceDescription().getID();
		}
	}

	public void dispose() {
		if (this.disposeNoDeregister()) {
			// Deregister with the resolver
			try {
				Resolver r = resolverFactory.getResolver();
				r.deregister(this.mainController == null ? this : this.mainController);
			} catch (InterruptedException ex) {
				logger.log(Level.SEVERE, "Could not deregister {0}: {1}", new Object[] {getLocalName(), ex});
			}

			listener.isFinished(this);
		}
	}
	
	/** Disposes the current instance, without deregistering it.
	 *   It will only be executed once per instance, after this it becomes a no-op.
	 * @return whether the method was executed, false if it was a no-op.
	 */
	private boolean disposeNoDeregister() {
		synchronized (this) {
			if (isDone) {
				return false;
			} else {
				isDone = true;
			}
		}
		IncomingMessageProcessor msgProcessor = portFactory.getMessageProcessor();
		for (ConduitExitControllerImpl<?> source : exits) {
			msgProcessor.removeReceiver(source.getIdentifier());
			source.dispose();
		}
		for (ConduitEntranceControllerImpl<?> sink : entrances) {
			sink.dispose();
		}
		
		if (this.isExecuting()) {
			// probably the agent has been killed and did not call its afterExecute
			afterExecute();
		}
		return true;
	}
		
	private void beforeExecute() {
		// write info file
		infoFile = new File(MiscTool.joinPaths(JVM.ONLY.tmpDir().toString(), Constant.Filename.AGENT_INFO_PREFIX + getLocalName() + Constant.Filename.AGENT_INFO_SUFFIX));

		String nl = System.getProperty("line.separator");
		FileWriter writer = null;
		try {
			writer = new FileWriter(infoFile);

			writer.write("this is file <" + infoFile + "> created by <" + getClass() + ">" + nl);
			writer.write("start date: " + (new java.util.Date()) + nl);
			writer.write("agent name: " + getLocalName() + nl);
			writer.write("coarsest log level: " + logger.getLevel() + nl);
			writer.write(nl);
			writer.write("executing ..." + nl);

			writer.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (RuntimeException e) {
			throw e;
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (java.io.IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
		
		synchronized (this) {
			this.isExecuting = true;
		}
	}

	public void afterExecute() {
		synchronized (this) {
			this.isExecuting = false;
		}

		// write info file
		assert infoFile != null;
		String nl = System.getProperty("line.separator");
		FileWriter writer = null;
		try {
			writer = new FileWriter(infoFile, true);

			writer.write(nl);
			writer.write("... done" + nl);
			writer.write(nl);
			writer.write("end date: " + (new java.util.Date()) + nl);

			writer.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (RuntimeException e) {
			throw e;
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (java.io.IOException e) {
					throw new RuntimeException(e);
				}
			}
		}

	}
	
	private boolean register() {
		try {
			Resolver locator = resolverFactory.getResolver();
			return locator.register(this.mainController == null ? this : this.mainController);
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
			locator.makeAvailable(this.mainController == null ? this : this.mainController);
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
		ConduitEntranceControllerImpl<T> s = threaded ? new ThreadedConduitEntranceController(currentID, this, newDataTemplate) : new PassiveConduitEntranceController(currentID, this, newDataTemplate);
		PortalID other = getOtherPortalID(currentID, ENTRANCE);
		portFactory.<T>getTransmitter(this.mainController == null ? this : this.mainController, s, other);
		entrances.add(s);
		return s;
	}

	@Override
	public <T extends Serializable> ConduitExitController<T> createConduitExit(boolean threaded, String portalName, DataTemplate newDataTemplate) {
		PortalID currentID = new PortalID(portalName, getIdentifier());
		ThreadedConduitExitController<T> s = new ThreadedConduitExitController(currentID, this, newDataTemplate);
		PortalID otherID = getOtherPortalID(currentID, EXIT);
		portFactory.<T>getReceiver(s, otherID);
		exits.add(s);
		return s;
	}
}
