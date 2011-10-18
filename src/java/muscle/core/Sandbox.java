///*
//Copyright 2008,2009 Complex Automata Simulation Technique (COAST) consortium
//
//GNU Lesser General Public License
//
//This file is part of MUSCLE (Multiscale Coupling Library and Environment).
//
//    MUSCLE is free software: you can redistribute it and/or modify
//    it under the terms of the GNU Lesser General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    MUSCLE is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU Lesser General Public License for more details.
//
//    You should have received a copy of the GNU Lesser General Public License
//    along with MUSCLE.  If not, see <http://www.gnu.org/licenses/>.
//*/
//
//package muscle.core;
//
//import java.util.Iterator;
//import java.util.LinkedList;
//import java.util.ArrayList;
//import java.util.List;
//
//import muscle.exception.MUSCLERuntimeException;
//import muscle.logging.AgentLogger;
//
//import jade.core.AID;
//import jade.wrapper.AgentController;
//import jade.core.behaviours.TickerBehaviour;
//
//import muscle.core.kernel.SandboxCounterpart;
//import javatool.ArraysTool;
//import java.util.logging.Logger;
//
//
///**
//this is a special kind of plumber which will provide a dummy cxa environment to test a single kernel
//@author Jan Hegewald
//*/
//public class Sandbox extends Plumber {
//
//	private transient List<AID> mockIDs = new ArrayList<AID>();
//	private transient AgentLogger logger;
//	private List<ConnectionInfo> connections = new LinkedList<ConnectionInfo>();
//	private AID firstAnnouncer;
//	private boolean connectionSchemeIsComplete = false;
//	
//	private final static Class CONDUIT_CLASS = muscle.core.conduit.BasicConduit.class;
//
//
//	//
//	protected void setup() {
//		logger = AgentLogger.getLogger(this);
//		super.setup();		
//	}
//	
//	
//	//
//	private void setFirstAnnouncer(AID announcer) {
//		
//		assert firstAnnouncer == null;
//		firstAnnouncer = announcer;
//
//		addBehaviour(new TickerBehaviour(this, 3000) {
//			protected void onTick() {
//				// time is up, no additional portal announcements are allowed from our target kernel
//				connectionSchemeIsComplete = true;
//				doInitConnectionScheme();
//				removeBehaviour(this);
//			}
//		});
//	}
//	
//
//	//
//	public void addEntrance(String entranceID, DataTemplate dataTemplate, AID controllerID, EntranceDependency[] dependencies) {
//
//		if(firstAnnouncer == null) {
//			setFirstAnnouncer(controllerID);
//		}
//		
//		// we only listen to one kernel
//		if( controllerID.equals(firstAnnouncer) ) {
//			if(!connectionSchemeIsComplete)
//				createMockExit(entranceID, dataTemplate);
//		}
//
//		super.addEntrance(entranceID, dataTemplate, controllerID, dependencies);
//	}
//
//
//	//
//	public void addExit(String exitID, DataTemplate dataTemplate, AID controllerID) {
//
//		if(firstAnnouncer == null) {
//			setFirstAnnouncer(controllerID);
//		}
//		
//		// we only listen to one kernel
//		if( controllerID.equals(firstAnnouncer) ) {
//			if(!connectionSchemeIsComplete)
//				createMockEntrance(exitID, dataTemplate);	
//		}
//
//		super.addExit(exitID, dataTemplate, controllerID);
//	}
//
//
//	//
//	private void createMockExit(String entranceID, DataTemplate dataTemplate) {
//		
//		mockIDs.add( new AID("MOCK#"+mockIDs.size(), AID.ISLOCALNAME) );
//		AID lastID = mockIDs.get(mockIDs.size()-1);
//		ConduitExit exit = new ConduitExit(new MOCKPortalID(entranceID, lastID), null, 1, dataTemplate);
//		spawnMockKernel(lastID.getLocalName(), ArraysTool.asArray(exit));
//
//		connections.add( new ConnectionInfo(entranceID, CONDUIT_CLASS.getName(), entranceID+"-->"+exit.getLocalName(), new String[0], exit.getLocalName()) );
//	}
//
//
//	//
//	private void createMockEntrance(String exitID, DataTemplate dataTemplate) {
//
//		mockIDs.add( new AID("MOCK#"+mockIDs.size(), AID.ISLOCALNAME) );
//		AID lastID = mockIDs.get(mockIDs.size()-1);
//		ConduitEntrance entrance = new ConduitEntrance(new MOCKPortalID(exitID, lastID), null, 1, dataTemplate);
//		spawnMockKernel(lastID.getLocalName(), ArraysTool.asArray(entrance));	
//		
//		connections.add( new ConnectionInfo(entrance.getLocalName(), CONDUIT_CLASS.getName(), entrance.getLocalName()+"-->"+exitID, new String[0], exitID) );
//	}
//	
//	
//	/**
//	generates the portal id which which will be used within a sandbox cxa
//	*/
//	public static class MOCKPortalID extends PortalID {
//	
//		private String originalStrippedName;
//	
//		public MOCKPortalID(String localName, AID aid) {
//			super("MOCK("+localName+")", aid);
//			String[] parts = localName.split("@");
//			originalStrippedName = parts[0];
//		}
//		
//		public String getOriginalStrippedName() {
//		
//			return originalStrippedName;
//		}
//	}
//
//
//	//
//	private void spawnMockKernel(String agentName, Portal[] portals) {
//	
//		Class kernelClass = muscle.core.kernel.SandboxCounterpart.class;
//		AgentController agentController;
//		try {
//			logger.info("spawning mock kernel <"+agentName+">");
//			agentController = getContainerController().createNewAgent(agentName, kernelClass.getName(), ArraysTool.asArray(new SandboxCounterpart.Arguments(portals)));
//			agentController.start();
//			
//		} catch (jade.wrapper.StaleProxyException e) {
//			throw new MUSCLERuntimeException(e);
//		}
//	}
//	
//	
//	//	this is called once from the super class
//	public ConnectionScheme initConnectionScheme() {
//
//		return new SandboxConnectionScheme();
//	}
//	// init our fake connection scheme after all moc agents are created
//	private void doInitConnectionScheme() {
//		
//		for(Iterator<ConnectionInfo> iter = connections.iterator(); iter.hasNext();) {
//			ConnectionInfo c = iter.next();
//			addConnection(c.entranceID, c.conduitClassName, c.conduitID, c.conduitArgs, c.exitID);
//		}
//	}
//
//
//	// storage class to simplify the calling of addConnection
//	private static class ConnectionInfo {
//		String entranceID;
//		String conduitClassName;
//		String conduitID;
//		String[] conduitArgs;
//		String exitID;
//		
//		ConnectionInfo(String newEntranceID, String newConduitClassName, String newConduitID, String[] newConduitArgs, String newExitID) {
//		
//			entranceID = newEntranceID;
//			conduitClassName = newConduitClassName;
//			conduitID = newConduitID;
//			conduitArgs = newConduitArgs;
//			exitID = newExitID;
//		}
//	}
//
//	//
//	private class SandboxConnectionScheme extends ConnectionScheme {
//
//		private Logger logger = muscle.logging.Logger.getLogger(SandboxConnectionScheme.class);
//
////		static protected utilities.Env loadEnv() {
////			return CxADescription.ONLY.subenv(ConnectionScheme.class);		
////		}
//		//
//		public boolean isComplete() {
//		
//			return connectionSchemeIsComplete;
//		}
//	}
//
//
////	static private final long STANDBY_TIME = 2000;
////	private List<Object/*can be Entrance or ExitDescription*/> postponedPortals = new LinkedList<Object>();
////	private AID targetKernel;
////	private transient AgentLogger logger;
////
////	protected void setup() {
////		logger = AgentLogger.getLogger(getClass().getName(), getAID());
////		super.setup();
////	}
////	
//////	private void initConnectionScheme() {
//////		// no preconfigured connection scheme possible
//////		// we generate connections as soon as all entrances/exits of our kernel are announced
//////	}
////
////	void addEntrance(String entranceID, DataTemplate dataTemplate, AID controllerID, EntranceDependency[] dependencies) {
////		if( !connectionSchemeInited() ) {
////			if(postponedPortals.size() == 0) {
////				firstContact(controllerID);
////			}
////
////			if(!controllerID.equals(targetKernel)) {
////				logger.severe("ignoring entrance <"+entranceID+"> -- it belongs to a blocked kernel");
////				return;
////			}
////			
////			EntranceDescription entrance = new EntranceDescription(entranceID);
////			entrance.setAvailable(dataTemplate, controllerID, dependencies);
////			postponedPortals.add(entrance);
////		}
////		else {
////			// behave as the plumber would do
////			super.addEntrance(entranceID, dataTemplate, controllerID, dependencies);
////		}
////		
////	}
////	
////	
////	//
////	private void firstContact(AID controllerID) {
////		// first portal announcement at all
////		targetKernel = controllerID;
////		// add our standby behaviour
////		addBehaviour(new TickerBehaviour(this, STANDBY_TIME) {
////			protected void onTick() {
////				// time is up, no additional portal announcements are allowed from our target kernel
////				targetKernelIsReady();
////				stop();
////			}
////		});
////	}
////
////
////	//
////	private void targetKernelIsReady() {
////		
////		// generate our fake entrances/exits
////		List<? extends Portal> fakePortals = new LinkedList<? extends Portal>();
////		for(Iterator<Object> iter = postponedPortals.iterator(); iter.hasNext();) {
////			Object description = iter.next();
////			if(description instanceof EntranceDescription) {
////				fakeExits.add( generateCounterpart((EntranceDescription)e) );
////			}
////			else if(description instanceof ExitDescription) {
////				fakeExits.add( generateCounterpart((ExitDescription)e) );			
////			}
////		}
////		
////		// spawn a fake kernel which can be connected to our target kernel
////
////	}
////
////
////	//
////	private ConduitExit generateCounterpart(EntranceDescription e) {
////		
////		ConduitExit counterpart = new ConduitExit(PortalID newPortalID, CAController newOwner, DataTemplate newDataTemplate);
//////		counterpart.setAvailable(DataTemplate newDataTemplate, AID newControllerID);
////		return counterpart;
////	}
////	
////	
////	//
////	private boolean connectionSchemeInited() {
////		return getConnectionSchemeRoot().size() > 0;
////	}
//
//
//}
//
//
