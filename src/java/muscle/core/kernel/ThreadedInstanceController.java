/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package muscle.core.kernel;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import muscle.Constant;
import muscle.core.*;
import muscle.core.conduit.communication.IncomingMessageProcessor;
import muscle.core.ident.Identifier;
import muscle.core.ident.PortalID;
import muscle.core.ident.Resolver;
import muscle.core.ident.ResolverFactory;
import utilities.JVM;
import utilities.MiscTool;
import utilities.Timing;
import utilities.data.FastArrayList;

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
	
	private RawKernel instance;
	private boolean execute;
	private Timing stopWatch;
	private File infoFile;
	private InstanceController mainController;
	
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
		this.portFactory = portFactory;
	}
	
	public void setMainController(InstanceController ic) {
		this.mainController = ic;
	}
	
	@Override
	public void run() {		
		System.out.println(getLocalName() + ": starting kernel");
		
		ConnectionScheme cs = ConnectionScheme.getInstance();
		this.exitDescriptions = cs.exitDescriptionsForIdentifier(id);
		this.entranceDescriptions = cs.entranceDescriptionsForIdentifier(id);
		
		try {
			instance = (RawKernel) this.instanceClass.newInstance();

			instance.setInstanceController(this);

			instance.beforeSetup();

			instance.setArguments(initFromArgs(args));

			registerPortals();
			instance.connectPortals();

			// log info about this controller
			Level logLevel = Level.INFO;
			if (logger.isLoggable(logLevel)) {
				logger.log(logLevel, instance.infoText());
			}

			if (execute) {
				beforeExecute();
				System.out.println(getLocalName() + ": executing");
				instance.execute();
				try {
					for (ConduitEntranceController ec : entrances) {
						if (!ec.waitUntilEmpty()) {
							logger.log(Level.WARNING, "After executing {0}, waiting for conduit {1}  was ended prematurely", new Object[]{getLocalName(), ec.getLocalName()});
						}
					}
					// TODO find out why this timeout is necessary for SimpleExample to succeed consistently
					// Otherwise, w will quit before the agent can send all messages.
					// It seems that the new way of updating the customSITime in ConduitExit/Entrance solved the problem.
					// Thread.sleep(1000);
				} catch (InterruptedException ex) {
					logger.log(Level.SEVERE, "After executing " + getLocalName() + ", waiting for conduit was interrupted", ex);
				}
				afterExecute();
				System.out.println(getLocalName() + ": finished");
				listener.isFinished(this);
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

	public <T extends Serializable> void addConduitExit(ConduitExitController<T> s) {
		PortalID other = getPortalID(s.getIdentifier(), exitDescriptions, EXIT);
		portFactory.<T>getReceiver(s, other);
		exits.add(s);
	}

	public <T extends Serializable> void addConduitEntrance(ConduitEntranceController<T> s) {
		PortalID other = getPortalID(s.getIdentifier(), entranceDescriptions, ENTRANCE);
		portFactory.<T>getTransmitter(this.mainController == null ? this : this.mainController, s, other);
		entrances.add(s);
	}
	
	private PortalID getPortalID(PortalID id, Map<String,? extends PortDescription> descriptions, boolean entrance) {
		ConduitDescription desc = null;
		if (descriptions != null)
			desc = descriptions.get(id.getPortName()).getConduitDescription();
		if (desc == null)
			throw new IllegalStateException("Port " + id + " is initialized in code but is not listed in the connection scheme. It will not work until this port is added in the connection scheme.");

		if (entrance) {
			return desc.getExitDescription().getID();
		} else {
			return desc.getExitDescription().getID();
		}
	}

	public void dispose() {
		IncomingMessageProcessor msgProcessor = portFactory.getMessageProcessor();
		for (ConduitExitController<?> source : exits) {
			msgProcessor.removeReceiver(source.getIdentifier());
			source.dispose();
		}
		for (ConduitEntranceController<?> sink : entrances) {
			sink.dispose();
		}
		
		if (instance != null)
			logger.log(Level.INFO, "kernel tmp dir: {0}", instance.getTmpPath());
		logger.info("bye");

		if (stopWatch != null && stopWatch.isCounting()) {
			// probably the agent has been killed and did not call its afterExecute
			afterExecute();
		}
		// Deregister with the resolver
		try {
			Resolver r = resolverFactory.getResolver();
			r.deregister(this.mainController == null ? this : this.mainController);
		} catch (InterruptedException ex) {
			logger.log(Level.SEVERE, "Could not deregister {0}: {1}", new Object[] {getLocalName(), ex});
		}
	}
		
	private void beforeExecute() {
		logger.info("begin execute");
		stopWatch = new Timing();
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

	}

	public void afterExecute() {
		stopWatch.stop();
		logger.log(Level.INFO, "end execute <{0}>", stopWatch);
		// write info file
		assert infoFile != null;
		String nl = System.getProperty("line.separator");
		FileWriter writer = null;
		try {
			writer = new FileWriter(infoFile, true);

			writer.write(nl);
			writer.write("... done" + nl);
			writer.write(nl);
			writer.write("duration: " + stopWatch + nl);
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
	
	private void registerPortals() {
		try {
			Resolver locator = resolverFactory.getResolver();
			locator.register(this.mainController == null ? this : this.mainController);
		} catch (InterruptedException ex) {
			Logger.getLogger(JadeInstanceController.class.getName()).log(Level.SEVERE, null, ex);
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
}
