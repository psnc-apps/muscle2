/*
 * 
 */
package muscle.core.kernel;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import muscle.Constant;
import muscle.core.Boot;
import muscle.core.ConduitDescription;
import muscle.core.PortFactory;
import muscle.core.ConduitEntranceController;
import muscle.core.ConduitExitController;
import muscle.core.ConnectionScheme;
import muscle.core.EntranceDescription;
import muscle.core.ExitDescription;
import muscle.core.Resolver;
import muscle.core.MultiDataAgent;
import muscle.core.PortDescription;
import muscle.core.conduit.communication.JadeReceiver;
import muscle.core.conduit.communication.Receiver;
import muscle.core.ident.Identifier;
import muscle.core.ident.PortalID;
import muscle.core.messaging.SinkObserver;
import muscle.core.messaging.jade.DataMessage;
import muscle.core.messaging.jade.ObservationMessage;
import utilities.FastArrayList;
import utilities.JVM;
import utilities.MiscTool;
import utilities.SafeTriggeredThread;
import utilities.Timing;

/**
 *
 * @author Joris Borgdorff
 */
public class JadeInstanceController extends MultiDataAgent implements SinkObserver<ObservationMessage<?>>, InstanceController {
	private Timing stopWatch;
	private boolean execute = true;
	private File infoFile;
	private RawKernel kernel;
	private final static Logger logger = Logger.getLogger(JadeInstanceController.class.getName());
	private final transient PortFactory factory = PortFactory.getInstance();
	private transient Map<String,ExitDescription> exitDescriptions;
	private transient Map<String,EntranceDescription> entranceDescriptions;
	private final static boolean ENTRANCE = true;
	private final static boolean EXIT = false;
	
	private final List<ConduitExitController<?>> dataSources = new ArrayList<ConduitExitController<?>>(); // these are the conduit exits
	private final List<ConduitEntranceController<?>> dataSinks = new ArrayList<ConduitEntranceController<?>>(); // these are the conduit entrances
	
	public <T> void addSink(ConduitEntranceController<T> s) {
		s.start();
		PortalID other = resolvePort(s.getIdentifier(), entranceDescriptions, ENTRANCE);
		if (other == null) return;
		System.out.println(getLocalName() + ": port " + other + " resolved");
		s.setTransmitter(factory.<T>getTransmitter(this, other));
		System.out.println(getLocalName() + ": port " + other + " transmitter added");
		dataSinks.add(s);
	}

	public <T> void addSource(ConduitExitController<T> s) {
		s.start();
		PortalID other = resolvePort(s.getIdentifier(), exitDescriptions, EXIT);
		if (other == null) return;
		System.out.println(getLocalName() + ": port " + other + " resolved");
		Receiver<DataMessage<T>, ?, ?, ?> recv = factory.<T>getReceiver(this, other);
		s.setReceiver(recv);
		messageProcessor.addReceiver(s.getIdentifier(), (JadeReceiver)recv);
		System.out.println(getLocalName() + ": port " + other + " receiver added");
		dataSources.add(s);
	}
	
	private PortalID resolvePort(PortalID id, Map<String,? extends PortDescription> descriptions, boolean entrance) {
		ConduitDescription desc = null;
		if (descriptions != null)
			desc = descriptions.get(id.getPortName()).getConduitDescription();
		if (desc == null)
			throw new IllegalStateException("Port " + id + " is initialized in code but is not listed in the connection scheme. It will not work until this port is added in the connection scheme.");

		PortalID other = entrance ? desc.getExitDescription().getID() : desc.getExitDescription().getID();
		if (!other.isResolved()) {
			try {
				System.out.println(getLocalName() + ": getting resolver");
				Resolver r = Boot.getInstance().getResolver();
				System.out.println(getLocalName() + ": resolving id");
				r.resolveIdentifier(other);
				if (!other.isResolved()) return null;
			} catch (InterruptedException ex) {
				logger.log(Level.SEVERE, null, ex);
			}
		}
		return other;
	}

	@Override
	public void takeDown() {
		super.takeDown();

		System.out.println("Taking down controller "+ this.getIdentifier() + "...");
		for (ConduitExitController<?> source : dataSources) {
			source.dispose();
		}
		for (ConduitEntranceController<?> sink : dataSinks) {
			sink.dispose();
		}
		
		logger.log(Level.INFO, "kernel tmp dir: {0}", kernel.getTmpPath());
		logger.info("bye");

		if (stopWatch != null && stopWatch.isCounting()) {
			// probably the agent has been killed and did not call its afterExecute
			afterExecute();
		}
		// Deregister with the resolver
		try {
			Resolver r = Boot.getInstance().getResolver();
			r.deregister(this);
		} catch (InterruptedException ex) {
			logger.log(Level.SEVERE, null, ex);
		}
	}
	
	//
	@Override
	final protected void setup() {
		super.setup();
		
		Identifier id = getIdentifier();
		ConnectionScheme cs = ConnectionScheme.getInstance();
		exitDescriptions = cs.exitDescriptionsForIdentifier(id);
		entranceDescriptions = cs.entranceDescriptionsForIdentifier(id);
		
		System.out.println(getLocalName() + ": starting kernel");
		String kernelName = Boot.getInstance().getAgentClass(this.getLocalName());
		try {
			kernel = (RawKernel) Class.forName(kernelName).newInstance();
		} catch (InstantiationException ex) {
			logger.log(Level.SEVERE, null, ex);
		} catch (IllegalAccessException ex) {
			logger.log(Level.SEVERE, null, ex);
		} catch (ClassNotFoundException ex) {
			logger.log(Level.SEVERE, null, ex);
		}
		
		kernel.setInstanceController(this);

		kernel.beforeSetup();

		kernel.setArguments(initFromArgs());

		registerPortals();
		System.out.println(getLocalName() + ": connecting portals");
		kernel.connectPortals();

		// log info about this controller
		Level logLevel = Level.INFO;
		if (logger.isLoggable(logLevel)) {
			logger.log(logLevel, kernel.infoText());
		}

		if (execute) {
			SafeTriggeredThread executor = new SafeTriggeredThread() {
				@Override
				protected void handleInterruption(InterruptedException ex) {
					logger.severe("Kernel interrupted");
				}

				@Override
				protected void execute() throws InterruptedException {
					beforeExecute();
					System.out.println(getLocalName() + " is executing");
					kernel.execute();
					System.out.println(getLocalName() + " is finished executing");
					for (ConduitEntranceController ec : dataSinks) {
						if (!ec.waitUntilEmpty()) {
							break;
						}
					}
					afterExecute();
					System.out.println(getLocalName() + " is finished");
					dispose();
					doDelete();
				}
			};
			executor.start();
			executor.trigger();
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
			writer.write("agent name: " + getName() + nl);
			writer.write("coarsest log level: " + logger.getLevel() + nl);
			writer.write(nl);
			writer.write("executing ..." + nl);

			writer.close();
		} catch (java.io.IOException e) {
			throw new RuntimeException(e);
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

	private void afterExecute() {
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
		} catch (java.io.IOException e) {
			throw new RuntimeException(e);
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
			Resolver locator = Boot.getInstance().getResolver();
			locator.register(this);
		} catch (InterruptedException ex) {
			Logger.getLogger(JadeInstanceController.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
	private Object[] initFromArgs() {
		Object[] rawArgs = super.getArguments();

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
	
	public String toString() {
		return getClass().getSimpleName() + "[" + getIdentifier() + "]";
	}
}
