/*
Copyright 2008,2009 Complex Automata Simulation Technique (COAST) consortium

GNU Lesser General Public License

This file is part of MUSCLE (Multiscale Coupling Library and Environment).

    MUSCLE is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    MUSCLE is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with MUSCLE.  If not, see <http://www.gnu.org/licenses/>.
*/

package muscle.core.kernel;


import com.thoughtworks.xstream.XStream;
import jade.core.AID;
import jade.core.Location;
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
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javatool.ArraysTool;
import javatool.DecimalMeasureTool;
import javatool.LoggerTool;
import javatool.PropertiesTool;
import javax.measure.DecimalMeasure;
import javax.measure.quantity.Duration;
import javax.measure.unit.SI;
import muscle.Constant;
import muscle.core.ConduitEntrance;
import muscle.core.ConduitExit;
import muscle.core.CxADescription;
import muscle.core.DataTemplate;
import muscle.core.EntranceDependency;
import muscle.core.JNIConduitEntrance;
import muscle.core.JNIConduitExit;
import muscle.core.Plumber;
import muscle.core.Portal;
import muscle.core.PortalID;
import muscle.core.Scale;
import muscle.core.messaging.SinkObserver;
import muscle.core.messaging.jade.DataMessage;
import muscle.exception.IgnoredException;
import muscle.exception.MUSCLERuntimeException;
import utilities.JVM;
import utilities.MiscTool;
import utilities.OSTool;
import utilities.PipeTransmuter;
import utilities.Timing;
import utilities.Transmutable;
import utilities.jni.JNIMethod;


// experimental info mode with
// coast sk:coast.cxa.test.sandbox.RawKernel\("execute true"\) --cxa_file src/coast/cxa/test.sandbox --main

/**
JADE agent to wrap a kernel (e.g. CA or MABS)
@author Jan Hegewald
*/
public abstract class RawKernel extends muscle.core.MultiDataAgent implements SinkObserver<DataMessage<?>> {
	
	private List<ConduitEntrance> entrances = new ArrayList<ConduitEntrance>();
	private List<ConduitExit> exits = new ArrayList<ConduitExit>();
	private boolean execute = true;
	private boolean acceptPortals;
	private Object[] remainingArgs;
	private Timing stopWatch;
	private File infoFile;
	private int QUEUE_WARNING_THRESHOLD = 5;
	private long dataCount; // count of data messages in all local buffers (exits) in bytes
	private long lastDataSize; // size of last data message (bytes)
	protected KernelListener observer = new NullKernelListener();
	

	//
   @Override
	public void takeDown() {

		super.takeDown();
	
		for( ConduitEntrance entrance : entrances ) {
			entrance.detachDestination();
			entrance.detachOwnerAgent();
		}
		for( ConduitExit exit : exits ) {
//			exit.detachConduit(this);
			exit.detachOwnerAgent();
		}
	
//  		getLogger().config("messages in administrative message queue: <"+messageQueue.size()+">"
//				+"\tmessages in general message queue: <"+getCurQueueSize()+">");
		getLogger().info("kernel tmp dir: "+getTmpPath());
		getLogger().info("bye");

		if( stopWatch.isCounting() ) {
			// probably the agent has been killed and did not call its afterExecute
			afterExecute();
		}
	}

	
	// return the remaining args if any
   @Override
	public Object[] getArguments() {
	
		if(remainingArgs == null)
			return new Object[0];
			
		return remainingArgs;
	}
	
	
	//
   @Override
	final protected void setup() {
		super.setup();

		beforeSetup();

		remainingArgs = initFromArgs();
		
		observer.notifyKernelActivated(this);

		// currently we can not add portals dynamically during runtime
		// add all portals here (and only here) at once
		acceptPortals = true;
		addPortals();
		acceptPortals = false;
		
		// log info about this controller
		Level logLevel = Level.INFO;
		if( getLogger().isLoggable(logLevel) )
			getLogger().log(logLevel, infoText());
		
		if(execute) {

         addBehaviour(new OneShotBehaviour(this) {

            @Override
            public void action() {

					try {
						waitForPlumber();
	
						// tell plumber about our portals
						registerPortals();
	
						// connect portals to their conduits
						attach();
	
						beforeExecute();
						execute();
						afterExecute();
					}
					catch(Exception e) {
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


	/**
	prints info about a given kernel to stdout
	*/
	static final public String info(Class<? extends RawKernel> controllerClass) {
		
		// instantiates a controller which is not intended to run as a JADE agent
		RawKernel kernel = null;
		try {
			kernel = controllerClass.newInstance();
		}
		catch (java.lang.InstantiationException e) {
			throw new RuntimeException(e);
		}
		catch (java.lang.IllegalAccessException e) {
			throw new RuntimeException(e);
		}
		
//		kernel.logger = muscle.logging.Logger.getLogger(kernel.getClass());

		kernel.acceptPortals = true;
		kernel.addPortals();
		kernel.acceptPortals = false;

		return kernel.infoText();
	}
	
	
	/**
	currently returns true if all portals of this kernel have passed a maximum timestep given in the cxa properties
	(this is a temporary implemantation because this mechanism is going to change anyway)
	note: if init portals are being used, they do increment their time only once!
	do not change signature! (used from native code)
	*/
	public boolean willStop() {

		int maxSeconds = CxADescription.ONLY.getIntProperty(CxADescription.Key.MAX_TIMESTEPS);
		DecimalMeasure<Duration> maxTime = new DecimalMeasure<Duration>(new BigDecimal(maxSeconds), SI.SECOND);
		DecimalMeasure<Duration> portalTime = maxTime;
		
		// search for the smallest "time" in our portals
		for( Portal p : entrances ) {
			if(p.getSITime().compareTo(portalTime) < 0)
				portalTime = p.getSITime();
		}
		for( Portal p : exits ) {
			if(p.getSITime().compareTo(portalTime) < 0)
				portalTime = p.getSITime();
		}

		return portalTime.compareTo(maxTime) > -1;
	}


	//
	public JNIMethod stopJNIMethod() {

		return new JNIMethod(this, "willStop");
	}
	
	
	//
	public KernelBootInfo getKernelBootInfo() {
	
		return new KernelBootInfo(getLocalName(), getClass());
	}

	
	// in case we want do do custom initialization where we need e.g. the JADE services already activated
	protected void beforeSetup() {
	}
	
	// use addExit/addEntrance to add portals to this controller
	protected abstract void addPortals();


	//
	protected abstract void execute();
	
	
	/**
	return the SI scale of a kernel<br>
	for a 1D kernel with dx=1meter and dt=1second this would e.g. be<br>
	javax.measure.DecimalMeasure<javax.measure.quantity.Duration> dt = javax.measure.DecimalMeasure.valueOf(new java.math.BigDecimal(1), javax.measure.unit.SI.SECOND);<br>
	javax.measure.DecimalMeasure<javax.measure.quantity.Length> dx = javax.measure.DecimalMeasure.valueOf(new java.math.BigDecimal(1), javax.measure.unit.SI.METER);<br>
	return new Scale(dt,dx);
	*/
	public abstract Scale getScale();
	
	
	/**
	helper method to create a scale object where dt is a multiple of the kernel scale
	*/
	private Scale getPortalScale(int rate) {
	
		if(rate == Portal.LOOSE) {
			return new Scale(getScale().getDt(), getScale().getAllDx());
		}
		else {
			DecimalMeasure<Duration> originalDt = getScale().getDt();
			DecimalMeasure<Duration> portalDt = DecimalMeasureTool.multiply(originalDt, new BigDecimal(rate));
			return new Scale(portalDt, getScale().getAllDx());
		}
	}


	//
	protected void addEntrance(ConduitEntrance entrance) {

		if(!acceptPortals)
			throw new IgnoredException("adding of porals not allowed here");
	
		// only add if not already added
		for( ConduitEntrance e : entrances ) {
			if( e.equals(entrance) )
				throw new MUSCLERuntimeException("can not add entrance twice <"+entrance+">");
		}
		
		addSink(entrance);
		entrances.add(entrance);
		observer.notifyEntranceAdded(entrance);
	}


	//
	protected void addExit(ConduitExit exit) {

		if(!acceptPortals)
			throw new IgnoredException("adding of porals not allowed here");
		
		// only add if not already added
		for( ConduitExit e : exits ) {
			if( e.equals(exit) )
				throw new MUSCLERuntimeException("can not add exit twice <"+exit+">");
		}
		
		addSource(exit);
		exits.add(exit);
		observer.notifyExitAdded(exit);
	}


	//
	protected <T> ConduitExit<T> addExit(String newPortalName, int newRate, Class<T> newDataClass) {

		if(newRate < 1)
			throw new IllegalArgumentException("portal use rate is <"+newRate+"> but can not be < 1");
			
		ConduitExit<T> e = new ConduitExit<T>(new PortalID(newPortalName, getAID()), this, newRate, new DataTemplate<T>(newDataClass, getPortalScale(newRate)));
		addExit(e);
		return e;
	}
	
	//
	protected <T,R> JNIConduitExit<T,R> addJNIExit(String newPortalName, int newRate, Class<T> newDataClass, Class<R> newJNIClass, Transmutable<T,R> newTransmuter) {

		JNIConduitExit<T,R> e = new JNIConduitExit<T,R>(newTransmuter, newJNIClass, new PortalID(newPortalName, getAID()), this, newRate, new DataTemplate<T>(newDataClass, getPortalScale(newRate)));
		addExit(e);
		return e;
	}
	//
	protected <T,R> JNIConduitExit<T,R> addJNIExit(String newPortalName, int newRate, Class<T> newDataClass) {

		Class<R> newJNIClass = (Class<R>)newDataClass;
		Transmutable<T,R> newTransmuter = new PipeTransmuter<T,R>();
		return addJNIExit(newPortalName, newRate, newDataClass, newJNIClass, newTransmuter);
	}


	//
	protected <T extends java.io.Serializable> ConduitEntrance<T> addEntrance(String newPortalName, int newRate, Class<T> newDataClass, EntranceDependency ... newDependencies) {

		ConduitEntrance<T> e = new ConduitEntrance<T>(new PortalID(newPortalName, getAID()), this, newRate, new DataTemplate<T>(newDataClass, getPortalScale(newRate)), newDependencies);
		addEntrance(e);
		return e;
	}
	//
	protected <R,T extends java.io.Serializable> JNIConduitEntrance<R,T> addJNIEntrance(String newPortalName, int newRate, Class<R> newJNIClass, Class<T> newDataClass, Transmutable<R,T> newTransmuter, EntranceDependency ... newDependencies) {

		JNIConduitEntrance<R,T> e = new JNIConduitEntrance<R,T>(newTransmuter, newJNIClass, new PortalID(newPortalName, getAID()), this, newRate, new DataTemplate<T>(newDataClass, getPortalScale(newRate)), newDependencies);
		addEntrance(e);
		return e;
	}
	//
	protected <R,T extends java.io.Serializable> JNIConduitEntrance<R,T> addJNIEntrance(String newPortalName, int newRate, Class<T> newDataClass, EntranceDependency ... newDependencies) {

		Class<R> newJNIClass = (Class<R>)newDataClass;
		Transmutable<R,T> newTransmuter = new PipeTransmuter<R,T>();
		return addJNIEntrance(newPortalName, newRate, newJNIClass, newDataClass, newTransmuter, newDependencies);
	}




//	/**
//	add to our local message queue (FIFO)
//	*/
//	public void addMessage(ACLMessage msg) {
//
//		if( messageQueue.size() > QUEUE_WARNING_THRESHOLD )
//			getLogger().warning("<"+messageQueue.size()+"> messages in administrative queue");
//
//		messageQueue.add(msg);
//	}
	
	
	/**
	returns path to a tmp directory for this kernel<br>
	do not change signature! (used from native code)
	*/
	public String getTmpPath() {
		
		String tmpPath = MiscTool.joinPaths(CxADescription.ONLY.getTmpRootPath(), OSTool.portableFileName(getLocalName(), ""));
		File tmpDir = new File(tmpPath);
		// create our kernel tmp dir if not already there
		tmpDir.mkdir();
		if(!tmpDir.isDirectory())
			getLogger().severe("invalid tmp path <"+tmpDir+"> for kernel <"+getLocalName()+">");
		
		return tmpDir.getPath();
	}



	
//	/**
//	returns a previously added portal<br>
//	do not change signature! (used from native code)
//	*/
//	private ConduitEntrance getEntrance(String name) {
//
//		for( ConduitEntrance e : entrances ) {
//			if( e.getLocalName().equals(name) )
//				return e;
//		}
//
//		return null;
//	}
//
//	/**
//	returns a previously added portal<br>
//	do not change signature! (used from native code)
//	*/
//	private ConduitExit getExit(String name) {
//
//		for( ConduitExit e : exits ) {
//			if( e.getLocalName().equals(name) )
//				return e;
//		}
//
//		return null;
//	}


	/**
	do not change signature! (used from native code)
	*/
	static public String getCxAProperty(String key) {
		
		return CxADescription.ONLY.getProperty(key);
	}


	/**
	reference to the CxA of this kernel
	do not change signature! (used from native code)
	*/
	static public CxADescription getCxA() {
		
		return CxADescription.ONLY;
	}
	
	
	//
	void sendException(Throwable t, AID dst) {
		
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg = new ACLMessage(ACLMessage.INFORM);		
		msg.setProtocol(""+Throwable.class);
		msg.addReceiver(dst);
		
		try {
			msg.setContentObject(t);
		}
		catch (java.io.IOException e) {
			throw new MUSCLERuntimeException(e);
		}
	}


	//
	private void beforeExecute() {

		getLogger().info("begin execute");
		stopWatch = new Timing();
		// write info file
		infoFile = new File(MiscTool.joinPaths(JVM.ONLY.tmpDir().toString(), Constant.Filename.AGENT_INFO_PREFIX+getLocalName()+Constant.Filename.AGENT_INFO_SUFFIX));

		String nl = System.getProperty("line.separator");
		FileWriter writer = null;
		try {
			writer = new FileWriter(infoFile);
			
			writer.write( "this is file <"+infoFile+"> created by <"+getClass()+">"+nl );
			writer.write( "start date: "+(new java.util.Date())+nl );
			writer.write( "agent name: "+getName()+nl );
			writer.write( "coarsest log level: "+LoggerTool.loggableLevel(getLogger())+nl );
			writer.write(nl);
			writer.write( "executing ..."+nl );
			
			writer.close();
		}
		catch (java.io.IOException e) {
			throw new RuntimeException(e);
		}
		finally {
			if(writer != null) {
				try {
					writer.close();
				}
				catch (java.io.IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
	
	}


	//
	private void afterExecute() {

		stopWatch.stop();
		getLogger().info("end execute <"+stopWatch+">");
		// write info file
		assert infoFile != null;
		String nl = System.getProperty("line.separator");
		FileWriter writer = null;
		try {
			writer = new FileWriter(infoFile, true);
			
			writer.write(nl);
			writer.write( "... done"+nl );
			writer.write(nl);
			writer.write( "duration: "+stopWatch+nl );
			writer.write( "end date: "+(new java.util.Date())+nl );
			
			writer.close();
		}
		catch (java.io.IOException e) {
			throw new RuntimeException(e);
		}
		finally {
			if(writer != null) {
				try {
					writer.close();
				}
				catch (java.io.IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
	
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
		for(int i = 0; i < entrances.size(); i++) {

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
			entranceProperties.put("Name", entrances.get(i).getLocalName());
			
			XStream xstream = new XStream();
			//prop = new Property("ConduitEntrance.DataTemplate", xstream.toXML(entrances.get(i).getDataTemplate()));
			//entranceDescription.addProperties(prop);
			entranceProperties.put("DataTemplate", xstream.toXML(entrances.get(i).getDataTemplate()));

			//prop = new Property("ConduitEntrance.Dependencies", xstream.toXML(entrances.get(i).getDependencies()));
			//entranceDescription.addProperties(prop);
			entranceProperties.put("Dependencies", xstream.toXML(entrances.get(i).getDependencies()));
  		
			entranceDescription.addProperties(new Property(Constant.Key.ENTRANCE_INFO, xstream.toXML(entranceProperties)));
		}
		agentDescription.addServices(entranceDescription);

		ServiceDescription exitDescription = new ServiceDescription();
//		exitDescription.addLanguages("serialized ConduitExit");
		exitDescription.addProtocols(Constant.Protocol.ANNOUNCE_EXIT);
		exitDescription.setType(Constant.Service.EXIT); // this is mandatory
		exitDescription.setName("ConduitExit"); // this is mandatory
		// add a property for every exit
		for(int i = 0; i < exits.size(); i++) {
//
//// quick workaround because the property value is always a String when received by other agents
//LinkedList<String> propList = new LinkedList<String>();
////propList.add(exits.get(i).getConduitPackageName());
//propList.add(exits.get(i).getName());
//propList.add(exits.get(i).getDataTemplate().getDataType());
//Property prop = new Property("ConduitExit", utilities.MiscTool.listToString(propList));
			//Property prop = new Property("ConduitExit", exits.get(i));


			HashMap<String, String> exitProperties = new HashMap<String, String>();
			exitProperties.put("Name", exits.get(i).getLocalName());

			XStream xstream = new XStream();
			exitProperties.put("DataTemplate", xstream.toXML(exits.get(i).getDataTemplate()));
			
			exitDescription.addProperties(new Property(Constant.Key.EXIT_INFO, xstream.toXML(exitProperties)));
  		}
		agentDescription.addServices(exitDescription);

		// register
  		try {
			DFService.register(this, agentDescription);
		} catch (FIPAException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}


	//
	private AID waitForPlumber() {

		getLogger().finest("searching for an Plumber Agent registering with the DF");
		List<AID>ids = null;
		try {
			ids = DFServiceTool.agentForService(this, true, Plumber.class.getName(), null);
		} catch (FIPAException e) {
			e.printStackTrace();
			getLogger().severe("search with DF is not succeeded because of " + e.getMessage());
			doDelete();
		}
		if( ids.size() != 1 ) {
			getLogger().severe("we found "+ids.size()+" plumbers, but only one is allowed");
			doDelete();
		}
		
		AID plumberID = ids.get(0);
      return plumberID;
	}
	
	
	/**
	connect all portals of this RawKernel with their conduits,
	the conduit will initiate the communication 
	*/
	private void attach() {
		LinkedList<ConduitEntrance> missingEntrances = new LinkedList<ConduitEntrance>();
		missingEntrances.addAll(entrances);		
		LinkedList<ConduitExit> missingExits = new LinkedList<ConduitExit>();

		MessageTemplate msgTemplate = MessageTemplate.MatchProtocol(Constant.Protocol.PORTAL_ATTACH);
		
		getLogger().config("waiting for conduit announcements ...");
		while(missingEntrances.size() > 0 || missingExits.size() > 0) {
			
			// generate log message
			Level logLevel = Level.FINEST;
			if( getLogger().isLoggable(logLevel) ) {
				StringBuilder waitText = new StringBuilder();
				waitText.append("waiting for conduits for <"+missingEntrances.size()+"> entrances: ");
				for(int i = 0; i < missingEntrances.size(); i++) {
					waitText.append(missingEntrances.get(i).getLocalName());
					if((i+1) < missingEntrances.size())
						waitText.append(", ");
				}
				waitText.append(" and <"+missingExits.size()+"> exits: ");
				for(int i = 0; i < missingExits.size(); i++) {
					waitText.append(missingExits.get(i).getLocalName());
					if((i+1) < missingExits.size())
						waitText.append(", ");
				}
				getLogger().log(logLevel, waitText.toString());
			}
			
			// receive attach message from a destination (sink) agent (usually a conduit)
			ACLMessage msg = blockingReceive(msgTemplate);
			AID dstAgent = msg.getSender();
			String dstSink = msg.getContent();
			
			// see to which entrance this attach message belongs
			if(missingEntrances.size() > 0) {
				String conduitEntranceName = msg.getUserDefinedParameter("entrance");
				for(Iterator<ConduitEntrance> iter = missingEntrances.iterator(); iter.hasNext();) {
					ConduitEntrance entrance = iter.next();
					if(entrance.getLocalName().equals(conduitEntranceName)) {
						getLogger().finest("attaching entrance <"+entrance.getLocalName()+"> to its destination <"+dstAgent.getLocalName()+">");
						entrance.setDestination(dstAgent, dstSink);
						iter.remove();
						break;
					}
				}
			}		
		}

		getLogger().config("... done waiting for conduit announcements");
	}
	
	
	//
	private String infoText() {
	
		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);
		
		out.printf("\nExits ("+exits.size()+")\n");
		for(ConduitExit exit : exits) {
			out.printf(" %-20s\n", exit.getLocalName());
		}		
		out.printf("\nEntrances ("+entrances.size()+")\n");
		for(ConduitEntrance entrance : entrances) {
			out.printf(" %-5s%-20s\n", "", entrance.getLocalName());
		}		
		
		out.println("==========");

		return writer.toString();
	}
	

	//
	private Object[] initFromArgs() {

		Object[] rawArgs = super.getArguments();
		
		if(rawArgs == null || rawArgs.length == 0)
			return rawArgs;
			
		int index = ArraysTool.indexForInstanceOf(rawArgs, Args.class);
		if(index > -1) {
			Args args = (Args)rawArgs[index];
			execute = args.execute;
		}
		// try to load args from a string
		try {
			index = ArraysTool.indexForInstanceOf(rawArgs, String.class);
			if(index > -1) {
				Args args = new Args((String)rawArgs[index]);
				execute = args.execute;
			}
			else {
				// no args for us here
			}
		}
		catch(java.lang.IllegalArgumentException e) {
			index = -1;
			// no args for us here
			// the string has wrong format for our args
			// just init with default values
		}
		
		// remove used arg from args
		if(index == 0) {
			rawArgs = Arrays.copyOfRange(rawArgs, index+1, rawArgs.length);			
		}
		else if(index == rawArgs.length) {
			rawArgs = Arrays.copyOfRange(rawArgs, 0, index-1);			
		}
		else if(index > -1) {
			Object[] begin = Arrays.copyOfRange(rawArgs, 0, index-1);
			Object[] end = Arrays.copyOfRange(rawArgs, index+1, rawArgs.length);
			
			rawArgs = ArraysTool.joinArrays(begin, end);			
		}
		
		return rawArgs;
	}
	
	
	//
	public static class Args {
	
		private boolean execute = true;
		private Logger logger = muscle.logging.Logger.getLogger(Args.class);
		
		public Args(boolean newExecute) {
		
			execute = newExecute;
		}

		public Args(String text) {

			Properties props = new Properties();
			try {
				props.load(new StringReader(text));
			}
			catch(java.io.IOException e) {
				e.printStackTrace();
			}

			// see if properties contains valid values
			Boolean val = null;
			try {
				val = PropertiesTool.getPropertyWithType(props, "execute", Boolean.class);
			}
			catch(java.lang.reflect.InvocationTargetException e) {
				throw new IllegalArgumentException("can not instantiate from <"+props.toString()+">");
			}
			if(val != null)
				execute = val;
			else
				throw new IllegalArgumentException("can not instantiate from <"+props.toString()+">");
		}
	}


	/**
	custom message queue for the RawKernel which sorts arriving messages to the individual message queues of the exits and the RawKernel itself
	@author Jan Hegewald
	*/
//	class SortingMessageQueue extends jade.core.PublicMessageQueue {
//		//
//		public void addFirst(ACLMessage msg) {
//		
//			if(sort(msg) != null)
//				super.addFirst(msg);
//		}
//
//		//
//		public void addLast(ACLMessage msg) {
//					
//			if(sort(msg) != null)
//				super.addLast(msg);
//		}
//
//
//		//
//		public ACLMessage sort(ACLMessage msg) {
//
//			if( administrativeWhiteListTemplate.match(msg) ) { // this is e.g a stop notification
//				addMessage(msg);
//			}
//			else {
//				// see if this message is intended for one of our other exits
//				for(Iterator<ConduitExit> exitIterator = exits.iterator(); exitIterator.hasNext();) {
//					ConduitExit exit = exitIterator.next();
//					
//					if( exit.getTemplate().match(msg) ) {
//// todo: put the deserialization in a separate thread
//						// deserialize message content
//						DataWrapper wrapper = null;
//						byte[] rawData = msg.getByteSequenceContent();
//						lastDataSize = rawData.length;
//						dataCount ++;
//						
//						if(dataCount > QUEUE_WARNING_THRESHOLD) {
//							sendException(new muscle.exception.QueueSizeExceededException(""+dataCount), msg.getSender());
//						}
//						
//						try {
//							wrapper = (DataWrapper)MiscTool.deserialize(rawData);
//							rawData = null;
//						}
//						catch(java.io.IOException e) {
//							throw new MUSCLERuntimeException(e);
//						}
//						catch(java.lang.ClassNotFoundException ee) {
//							throw new MUSCLERuntimeException(ee);
//						}
//
//						exit.put(wrapper);
//						wrapper = null;
//						msg = null;
//						break; // it is not possible for a message to feed multiple exits
//					}
//				}			
//			}
//			
//			return msg;
//		}
//	}


	/**
	pass one or more controller class names to get an info about these controllers
	*/
	public static void main(String[] args) throws java.lang.ClassNotFoundException {

		if(args.length > 0) {
		
			ArrayList<Class> classes = new ArrayList<Class>();
			for(String arg : args) {
				Class c = Class.forName(arg);
				classes.add(c);
			}
		
			for(Class c : classes.toArray(new Class[0])) {
				
				System.out.println(RawKernel.info(c)+"\n");
			}
			
		}
	}

}
