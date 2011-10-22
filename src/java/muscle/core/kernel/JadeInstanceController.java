/*
 * 
 */
package muscle.core.kernel;

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
import muscle.core.ConduitEntranceController;
import muscle.core.ConduitExitController;
import muscle.core.MultiDataAgent;
import muscle.core.Plumber;
import muscle.core.ident.Identifier;
import muscle.core.ident.JadeAgentID;
import muscle.core.messaging.SinkObserver;
import muscle.core.messaging.jade.ObservationMessage;
import muscle.exception.MUSCLERuntimeException;
import utilities.FastArrayList;
import utilities.JVM;
import utilities.MiscTool;
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
	
	@Override
	public void takeDown() {
		super.takeDown();

		for (ConduitEntranceController entrance : kernel.entrances) {
			entrance.dispose();
		}
		for (ConduitExitController exit : kernel.exits) {
			exit.dispose();
		}

		logger.log(Level.INFO, "kernel tmp dir: {0}", kernel.getTmpPath());
		logger.info("bye");

		if (stopWatch.isCounting()) {
			// probably the agent has been killed and did not call its afterExecute
			afterExecute();
		}
	}
	
	//
	@Override
	final protected void setup() {
		super.setup();
		
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

		kernel.observer.notifyKernelActivated(kernel);

		kernel.connectPortals();

		// log info about this controller
		Level logLevel = Level.INFO;
		if (logger.isLoggable(logLevel)) {
			logger.log(logLevel, kernel.infoText());
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
	
	public <E> void sendData(E data) {
		if (data instanceof ACLMessage) {
			this.send((ACLMessage)data);
		}
		else {
			throw new IllegalArgumentException("JADE agent can only send ACLMessage directly.");
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
		LinkedList<ConduitEntranceController> missingEntrances = new LinkedList<ConduitEntranceController>();
		missingEntrances.addAll(kernel.entrances);
		LinkedList<ConduitExitController> missingExits = new LinkedList<ConduitExitController>();

		MessageTemplate msgTemplate = MessageTemplate.MatchProtocol(Constant.Protocol.PORTAL_ATTACH);

		logger.config("waiting for conduit announcements ...");
		while (missingEntrances.size() > 0 || missingExits.size() > 0) {

			// generate log message
			Level logLevel = Level.FINEST;
			if (logger.isLoggable(logLevel)) {
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
				logger.log(logLevel, waitText.toString());
			}

			// receive attach message from a destination (sink) agent (usually a conduit)
			ACLMessage msg = blockingReceive(msgTemplate);
			AID dstAgent = msg.getSender();
			String dstSink = msg.getContent();

			// see to which entrance this attach message belongs
			if (missingEntrances.size() > 0) {
				String conduitEntranceName = msg.getUserDefinedParameter("entrance");
				for (Iterator<ConduitEntranceController> iter = missingEntrances.iterator(); iter.hasNext();) {
					ConduitEntranceController entrance = iter.next();
					if (entrance.getLocalName().equals(conduitEntranceName)) {
						logger.log(Level.FINEST, "attaching entrance <{0}> to its destination <{1}>", new Object[]{entrance.getLocalName(), dstAgent.getLocalName()});
						entrance.setDestination(dstAgent, dstSink);
						iter.remove();
						break;
					}
				}
			}
		}

		logger.config("... done waiting for conduit announcements");
	}
	
	
	// TODO let every portal announce/attach itelf??
	private void registerPortals() {
		DFAgentDescription agentDescription = new DFAgentDescription();
		agentDescription.setName(getAID());

		ServiceDescription entranceDescription = new ServiceDescription();
		entranceDescription.addProtocols(Constant.Protocol.ANNOUNCE_ENTRANCE);
		entranceDescription.setType(Constant.Service.ENTRANCE); // this is mandatory
		entranceDescription.setName("ConduitEntrance"); // this is mandatory

		// add a property for every entrance
		for (int i = 0; i < kernel.entrances.size(); i++) {
			HashMap<String, String> entranceProperties = new HashMap<String, String>();

			entranceProperties.put("Name", kernel.entrances.get(i).getLocalName());

			XStream xstream = new XStream();
			entranceDescription.addProperties(new Property(Constant.Key.ENTRANCE_INFO, xstream.toXML(entranceProperties)));
		}
		agentDescription.addServices(entranceDescription);

		ServiceDescription exitDescription = new ServiceDescription();
		exitDescription.addProtocols(Constant.Protocol.ANNOUNCE_EXIT);
		exitDescription.setType(Constant.Service.EXIT); // this is mandatory
		exitDescription.setName("ConduitExit"); // this is mandatory
		
		// add a property for every exit
		for (int i = 0; i < kernel.exits.size(); i++) {
			HashMap<String, String> exitProperties = new HashMap<String, String>();
			exitProperties.put("Name", kernel.exits.get(i).getLocalName());

			XStream xstream = new XStream();
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
		logger.finest("searching for an Plumber Agent registering with the DF");
		List<AID> ids = null;
		try {
			ids = DFServiceTool.agentForService(this, true, Plumber.class.getName(), null);
		} catch (FIPAException e) {
			logger.log(Level.SEVERE, "search with DF is not succeeded", e);
			doDelete();
		}
		if (ids.size() != 1) {
			logger.log(Level.SEVERE, "we found {0} plumbers, but only one is allowed", ids.size());
			doDelete();
		}

		AID plumberID = ids.get(0);
		return plumberID;
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
					// the string has wrong format for our args, re-add it
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
}
