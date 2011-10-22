///*
// * 
// */
//package muscle.core;
//
//import com.thoughtworks.xstream.XStream;
//import jade.domain.DFService;
//import jade.domain.FIPAAgentManagement.DFAgentDescription;
//import jade.domain.FIPAAgentManagement.Property;
//import jade.domain.FIPAAgentManagement.SearchConstraints;
//import jade.domain.FIPAAgentManagement.ServiceDescription;
//import jade.domain.FIPAException;
//import jade.lang.acl.ACLMessage;
//import jade.proto.SubscriptionInitiator;
//import java.util.HashMap;
//import java.util.Iterator;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//import muscle.Constant;
//
///**
// *
// * @author Joris Borgdorff
// */
//public class ConduitSubscriptionInitiator extends SubscriptionInitiator {
//	private transient final static Logger logger = Logger.getLogger(ConduitSubscriptionInitiator.class.getName());
//	private final Plumber outer;
//	private transient XStream xstream = new XStream();
//	private final boolean ofEntrance;
//	public final static boolean ENTRANCE = true;
//	public final static boolean EXIT = false;
//
//	private ConduitSubscriptionInitiator(Plumber a, ACLMessage msg, boolean ofEntrance) {
//		super(a, msg);
//		this.outer = a;
//		this.ofEntrance = ofEntrance;
//	}
//	
//	public static ConduitSubscriptionInitiator initConduitSubscription(Plumber a, boolean ofEntrance) {
//		DFAgentDescription agentDescription = new DFAgentDescription();
//		ServiceDescription pointDescription = new ServiceDescription();
//		
//		if (ofEntrance) {
//			pointDescription.addProtocols(Constant.Protocol.ANNOUNCE_ENTRANCE);
//		}
//		else {
//			pointDescription.addProtocols(Constant.Protocol.ANNOUNCE_EXIT);
//			pointDescription.setType(Constant.Service.EXIT);
//		}
//		agentDescription.addServices(pointDescription);
//		ACLMessage msg = DFService.createSubscriptionMessage(a, a.getDefaultDF(), agentDescription, new SearchConstraints());
//		return new ConduitSubscriptionInitiator(a, msg, ofEntrance);
//	}
//
//	protected void handleInform(ACLMessage inform) {
//		//System.out.println("Agent "+getLocalName()+": Notification received from DF: msg:\n"+inform.toString()+"\n\n");
//		//System.out.println("performative:"+ACLMessage.getPerformative(inform.getPerformative()));
//		//System.out.println("replyto:"+inform.getInReplyTo()); // matches the subscription message
//		try {
//			DFAgentDescription[] results = DFService.decodeNotification(inform.getContent());
//			//System.out.println("results:"+results.length);
//			for (int i = 0; i < results.length; ++i) {
//				DFAgentDescription dfd = results[i];
//				//AID provider = dfd.getName();
//				//System.out.println("provider:"+provider.getLocalName());
//				// The same agent may provide several services
//				Iterator serviceIter = dfd.getAllServices(); // does not contain deregistered services of the service provider
//				// something deregistered
//				if (!serviceIter.hasNext()) {
//					//throw new MUSCLERuntimeException("\n--- deregister of portals not supported ---\n");
//					outer.removePortalsForControllerID(dfd.getName());
//				}
//				while (serviceIter.hasNext()) {
//					if (ofEntrance) {
//						parseEntranceDescription((ServiceDescription) serviceIter.next(), dfd);
//					}
//					else {
//						parseExitDescription((ServiceDescription) serviceIter.next(), dfd);
//					}
//				}
//			}
//		} catch (FIPAException e) {
//			e.printStackTrace();
//		}
//	}
//
//	private void parseEntranceDescription(ServiceDescription sd, DFAgentDescription dfd) {
//		if (sd.getType().equals(Constant.Service.ENTRANCE)) { // there might be other services as well
//			Iterator entranceIter = sd.getAllProperties();
//			while (entranceIter.hasNext()) {
//				Property content = (Property) entranceIter.next();
//				assert content.getName().equals(Constant.Key.ENTRANCE_INFO);
//				HashMap<String, String> entranceProperties = (HashMap<String, String>) xstream.fromXML((String) content.getValue());
//
//				String entranceID = entranceProperties.get("Name");
//				DataTemplate dataTemplate = (DataTemplate) xstream.fromXML(entranceProperties.get("DataTemplate"));
//				EntranceDependency[] dependencies = (EntranceDependency[]) xstream.fromXML(entranceProperties.get("Dependencies"));
//
//				logger.log(Level.INFO, "found an entrance: <{0}:{1}>", new Object[]{dfd.getName().getLocalName(), entranceID});
//				outer.addEntrance(entranceID, dataTemplate, dfd.getName(), dependencies);
//			}
//		}
//	}
//
//	private void parseExitDescription(ServiceDescription sd, DFAgentDescription dfd) {
//		if (sd.getType().equals(Constant.Service.EXIT)) {
//			// there might be other services as well
//			Iterator exitIter = sd.getAllProperties();
//			while (exitIter.hasNext()) {
//				Property content = (Property) exitIter.next();
//				assert content.getName().equals(Constant.Key.EXIT_INFO);
//				HashMap<String, String> exitProperties = (HashMap<String, String>) xstream.fromXML((String) content.getValue());
//				String exitID = exitProperties.get("Name");
//				DataTemplate dataTemplate = (DataTemplate) xstream.fromXML(exitProperties.get("DataTemplate"));
//				logger.log(Level.INFO, "found an exit: <{0}:{1}>", new Object[]{dfd.getName().getLocalName(), exitID});
//				outer.addExit(exitID, dataTemplate, dfd.getName());
//			}
//		}
//	}
//}
