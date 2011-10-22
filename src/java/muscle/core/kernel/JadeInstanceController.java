/*
 * 
 */
package muscle.core.kernel;

<<<<<<< HEAD
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
=======
import com.thoughtworks.xstream.XStream;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.Property;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jadetool.DFServiceTool;
import java.io.File;
import java.io.FileWriter;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javatool.ArraysTool;
import javatool.LoggerTool;
import muscle.Constant;
import muscle.core.Boot;
import muscle.core.ConduitEntrance;
import muscle.core.ConduitExit;
import muscle.core.MultiDataAgent;
import muscle.core.Plumber;
import muscle.core.ident.Identifier;
import muscle.core.ident.JadeAgentID;
import muscle.core.messaging.SinkObserver;
import muscle.core.messaging.jade.DataMessage;
import muscle.exception.MUSCLERuntimeException;
import utilities.JVM;
import utilities.MiscTool;
>>>>>>> a8c652eb292cdbcf135af6155a23c69c08aef9c0
import utilities.Timing;

/**
 *
 * @author Joris Borgdorff
 */
<<<<<<< HEAD
public class JadeInstanceController extends MultiDataAgent implements SinkObserver<ObservationMessage<?>>, InstanceController {
=======
public class JadeInstanceController extends MultiDataAgent implements SinkObserver<DataMessage<?>>, InstanceController {
>>>>>>> a8c652eb292cdbcf135af6155a23c69c08aef9c0
	private Timing stopWatch;
	private boolean execute = true;
	private File infoFile;
	private RawKernel kernel;
<<<<<<< HEAD
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

=======
	
>>>>>>> a8c652eb292cdbcf135af6155a23c69c08aef9c0
	@Override
	public void takeDown() {
		super.takeDown();

<<<<<<< HEAD
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
=======
		for (ConduitEntrance entrance : kernel.entrances) {
			entrance.detachDestination();
			entrance.detachOwnerAgent();
		}
		for (ConduitExit exit : kernel.exits) {
			exit.detachOwnerAgent();
		}

		getLogger().log(Level.INFO, "kernel tmp dir: {0}", kernel.getTmpPath());
		getLogger().info("bye");

		if (stopWatch.isCounting()) {
			// probably the agent has been killed and did not call its afterExecute
			afterExecute();
		}
>>>>>>> a8c652eb292cdbcf135af6155a23c69c08aef9c0
	}
	
	//
	@Override
	final protected void setup() {
		super.setup();
		
<<<<<<< HEAD
		Identifier id = getIdentifier();
		ConnectionScheme cs = ConnectionScheme.getInstance();
		exitDescriptions = cs.exitDescriptionsForIdentifier(id);
		entranceDescriptions = cs.entranceDescriptionsForIdentifier(id);
		
		System.out.println(getLocalName() + ": starting kernel");
=======
>>>>>>> a8c652eb292cdbcf135af6155a23c69c08aef9c0
		String kernelName = Boot.getInstance().getAgentClass(this.getLocalName());
		try {
			kernel = (RawKernel) Class.forName(kernelName).newInstance();
		} catch (InstantiationException ex) {
<<<<<<< HEAD
			logger.log(Level.SEVERE, null, ex);
		} catch (IllegalAccessException ex) {
			logger.log(Level.SEVERE, null, ex);
		} catch (ClassNotFoundException ex) {
			logger.log(Level.SEVERE, null, ex);
		}
		
		kernel.setInstanceController(this);

=======
			Logger.getLogger(JadeInstanceController.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IllegalAccessException ex) {
			Logger.getLogger(JadeInstanceController.class.getName()).log(Level.SEVERE, null, ex);
		} catch (ClassNotFoundException ex) {
			Logger.getLogger(JadeInstanceController.class.getName()).log(Level.SEVERE, null, ex);
		}
		kernel.setInstanceController(this);
		
>>>>>>> a8c652eb292cdbcf135af6155a23c69c08aef9c0
		kernel.beforeSetup();

		kernel.setArguments(initFromArgs());

<<<<<<< HEAD
		registerPortals();
		System.out.println(getLocalName() + ": connecting portals");
=======
		kernel.observer.notifyKernelActivated(kernel);

>>>>>>> a8c652eb292cdbcf135af6155a23c69c08aef9c0
		kernel.connectPortals();

		// log info about this controller
		Level logLevel = Level.INFO;
<<<<<<< HEAD
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
=======
		if (getLogger().isLoggable(logLevel)) {
			getLogger().log(logLevel, kernel.infoText());
		}

		if (execute) {
			addBehaviour(new OneShotBehaviour(this) {
				@Override
				public void action() {
					try {
						System.out.println(getLocalName() + " is waiting for plumber");
						waitForPlumber();

						// tell plumber about our portals
						System.out.println(getLocalName() + " is registering portals");
						registerPortals();

						// connect portals to their conduits
						System.out.println(getLocalName() + " is attaching portals");
						attach();

						System.out.println(getLocalName() + " is preparing execution");
						beforeExecute();
						System.out.println(getLocalName() + " is executing");
						kernel.execute();
						System.out.println(getLocalName() + " is finishing execution");
						afterExecute();
					} catch (Exception e) {
						throw new MUSCLERuntimeException(e);
					}
				}

				@Override
				public int onEnd() {
					// we are done, terminate agent
					doDelete();

					return super.onEnd();
				}
			});
		}

	}
	
	public <E extends Serializable> void sendData(E data) {
		if (data instanceof ACLMessage) {
			this.send((ACLMessage)data);
		}
		else {
			throw new IllegalArgumentException("JADE agent can only send ACLMessage directly.");
		}
	}
	
	
	private void beforeExecute() {
		getLogger().info("begin execute");
>>>>>>> a8c652eb292cdbcf135af6155a23c69c08aef9c0
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
<<<<<<< HEAD
			writer.write("coarsest log level: " + logger.getLevel() + nl);
=======
			writer.write("coarsest log level: " + LoggerTool.loggableLevel(getLogger()) + nl);
>>>>>>> a8c652eb292cdbcf135af6155a23c69c08aef9c0
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
<<<<<<< HEAD
		logger.log(Level.INFO, "end execute <{0}>", stopWatch);
=======
		getLogger().log(Level.INFO, "end execute <{0}>", stopWatch);
>>>>>>> a8c652eb292cdbcf135af6155a23c69c08aef9c0
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
	
<<<<<<< HEAD
	private void registerPortals() {
		try {
			Resolver locator = Boot.getInstance().getResolver();
			locator.register(this);
		} catch (InterruptedException ex) {
			Logger.getLogger(JadeInstanceController.class.getName()).log(Level.SEVERE, null, ex);
		}
=======
	void sendException(Throwable t, AID dst) {
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg = new ACLMessage(ACLMessage.INFORM);
		msg.setProtocol("" + Throwable.class);
		msg.addReceiver(dst);

		try {
			msg.setContentObject(t);
		} catch (java.io.IOException e) {
			throw new MUSCLERuntimeException(e);
		}
	}
	
		/**
	connect all portals of this RawKernel with their conduits,
	the conduit will initiate the communication 
	 */
	private void attach() {
		LinkedList<ConduitEntrance> missingEntrances = new LinkedList<ConduitEntrance>();
		missingEntrances.addAll(kernel.entrances);
		LinkedList<ConduitExit> missingExits = new LinkedList<ConduitExit>();

		MessageTemplate msgTemplate = MessageTemplate.MatchProtocol(Constant.Protocol.PORTAL_ATTACH);

		getLogger().config("waiting for conduit announcements ...");
		while (missingEntrances.size() > 0 || missingExits.size() > 0) {

			// generate log message
			Level logLevel = Level.FINEST;
			if (getLogger().isLoggable(logLevel)) {
				StringBuilder waitText = new StringBuilder(100 + 50*missingEntrances.size() + 50*missingExits.size());
				waitText.append("waiting for conduits for <").append(missingEntrances.size()).append("> entrances: ");
				for (int i = 0; i < missingEntrances.size(); i++) {
					waitText.append(missingEntrances.get(i).getLocalName());
					if ((i + 1) < missingEntrances.size()) {
						waitText.append(", ");
					}
				}
				waitText.append(" and <").append(missingExits.size()).append("> exits: ");
				for (int i = 0; i < missingExits.size(); i++) {
					waitText.append(missingExits.get(i).getLocalName());
					if ((i + 1) < missingExits.size()) {
						waitText.append(", ");
					}
				}
				getLogger().log(logLevel, waitText.toString());
			}

			// receive attach message from a destination (sink) agent (usually a conduit)
			ACLMessage msg = blockingReceive(msgTemplate);
			AID dstAgent = msg.getSender();
			String dstSink = msg.getContent();

			// see to which entrance this attach message belongs
			if (missingEntrances.size() > 0) {
				String conduitEntranceName = msg.getUserDefinedParameter("entrance");
				for (Iterator<ConduitEntrance> iter = missingEntrances.iterator(); iter.hasNext();) {
					ConduitEntrance entrance = iter.next();
					if (entrance.getLocalName().equals(conduitEntranceName)) {
						getLogger().log(Level.FINEST, "attaching entrance <{0}> to its destination <{1}>", new Object[]{entrance.getLocalName(), dstAgent.getLocalName()});
						entrance.setDestination(dstAgent, dstSink);
						iter.remove();
						break;
					}
				}
			}
		}

		getLogger().config("... done waiting for conduit announcements");
	}
	
	
	// TODO let every portal announce/attach itelf??
	private void registerPortals() {
		DFAgentDescription agentDescription = new DFAgentDescription();
		agentDescription.setName(getAID());

		ServiceDescription entranceDescription = new ServiceDescription();
//		entranceDescription.addLanguages("serialized ConduitEntrance");
		entranceDescription.addProtocols(Constant.Protocol.ANNOUNCE_ENTRANCE);
		entranceDescription.setType(Constant.Service.ENTRANCE); // this is mandatory
		entranceDescription.setName("ConduitEntrance"); // this is mandatory
		// add a property for every entrance
		for (int i = 0; i < kernel.entrances.size(); i++) {

//System.out.println("settin value:"+entrances.get(i).getClass().getName());

//// quick workaround because the property value is always a String when received by other agents
//LinkedList<String> propList = new LinkedList<String>();
////propList.add(entrances.get(i).getConduitPackageName());
//propList.add(entrances.get(i).getName());
//propList.add(entrances.get(i).getDataTemplate().getDataType());
//Property prop = new Property("ConduitEntrance", utilities.MiscTool.listToString(propList));
			//Property prop = new Property("ConduitEntrance", entrances.get(i));

//System.out.println("propname:"+prop.getName());
//System.out.println("value:"+prop.getValue().getClass().getName());


			HashMap<String, String> entranceProperties = new HashMap<String, String>();

			//Property prop = new Property("ConduitEntrance.Name", entrances.get(i).getName());
			//entranceDescription.addProperties(prop);
			entranceProperties.put("Name", kernel.entrances.get(i).getLocalName());

			XStream xstream = new XStream();
			//prop = new Property("ConduitEntrance.DataTemplate", xstream.toXML(entrances.get(i).getDataTemplate()));
			//entranceDescription.addProperties(prop);
			entranceProperties.put("DataTemplate", xstream.toXML(kernel.entrances.get(i).getDataTemplate()));

			//prop = new Property("ConduitEntrance.Dependencies", xstream.toXML(entrances.get(i).getDependencies()));
			//entranceDescription.addProperties(prop);
			entranceProperties.put("Dependencies", xstream.toXML(kernel.entrances.get(i).getDependencies()));

			entranceDescription.addProperties(new Property(Constant.Key.ENTRANCE_INFO, xstream.toXML(entranceProperties)));
		}
		agentDescription.addServices(entranceDescription);

		ServiceDescription exitDescription = new ServiceDescription();
//		exitDescription.addLanguages("serialized ConduitExit");
		exitDescription.addProtocols(Constant.Protocol.ANNOUNCE_EXIT);
		exitDescription.setType(Constant.Service.EXIT); // this is mandatory
		exitDescription.setName("ConduitExit"); // this is mandatory
		// add a property for every exit
		for (int i = 0; i < kernel.exits.size(); i++) {
//
//// quick workaround because the property value is always a String when received by other agents
//LinkedList<String> propList = new LinkedList<String>();
////propList.add(exits.get(i).getConduitPackageName());
//propList.add(exits.get(i).getName());
//propList.add(exits.get(i).getDataTemplate().getDataType());
//Property prop = new Property("ConduitExit", utilities.MiscTool.listToString(propList));
			//Property prop = new Property("ConduitExit", exits.get(i));


			HashMap<String, String> exitProperties = new HashMap<String, String>();
			exitProperties.put("Name", kernel.exits.get(i).getLocalName());

			XStream xstream = new XStream();
			exitProperties.put("DataTemplate", xstream.toXML(kernel.exits.get(i).getDataTemplate()));

			exitDescription.addProperties(new Property(Constant.Key.EXIT_INFO, xstream.toXML(exitProperties)));
		}
		agentDescription.addServices(exitDescription);

		// register
		try {
			DFService.register(this, agentDescription);
		} catch (FIPAException e) {
			throw new RuntimeException(e);
		}


	}

	private AID waitForPlumber() {
		getLogger().finest("searching for an Plumber Agent registering with the DF");
		List<AID> ids = null;
		try {
			ids = DFServiceTool.agentForService(this, true, Plumber.class.getName(), null);
		} catch (FIPAException e) {
			getLogger().log(Level.SEVERE, "search with DF is not succeeded", e);
			doDelete();
		}
		if (ids.size() != 1) {
			getLogger().log(Level.SEVERE, "we found {0} plumbers, but only one is allowed", ids.size());
			doDelete();
		}

		AID plumberID = ids.get(0);
		return plumberID;
>>>>>>> a8c652eb292cdbcf135af6155a23c69c08aef9c0
	}
	
	private Object[] initFromArgs() {
		Object[] rawArgs = super.getArguments();

		if (rawArgs == null || rawArgs.length == 0) {
			return rawArgs;
		}

<<<<<<< HEAD
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
=======
		int index = ArraysTool.indexForInstanceOf(rawArgs, KernelArgs.class);
		if (index > -1) {
			KernelArgs args = (KernelArgs) rawArgs[index];
			execute = args.execute;
		}
		// try to load args from a string
		try {
			index = ArraysTool.indexForInstanceOf(rawArgs, String.class);
			if (index > -1) {
				KernelArgs args = new KernelArgs((String) rawArgs[index]);
				execute = args.execute;
			} else {
				// no args for us here
			}
		} catch (java.lang.IllegalArgumentException e) {
			index = -1;
			// no args for us here
			// the string has wrong format for our args
			// just init with default values
		}

		// remove used arg from args
		if (index == 0) {
			rawArgs = Arrays.copyOfRange(rawArgs, index + 1, rawArgs.length);
		} else if (index == rawArgs.length) {
			rawArgs = Arrays.copyOfRange(rawArgs, 0, index - 1);
		} else if (index > -1) {
			Object[] begin = Arrays.copyOfRange(rawArgs, 0, index - 1);
			Object[] end = Arrays.copyOfRange(rawArgs, index + 1, rawArgs.length);

			rawArgs = ArraysTool.joinArrays(begin, end);
		}

		return rawArgs;
	}
	
	public Identifier getID() {
		return new JadeAgentID(this.getAID());
>>>>>>> a8c652eb292cdbcf135af6155a23c69c08aef9c0
	}
}
