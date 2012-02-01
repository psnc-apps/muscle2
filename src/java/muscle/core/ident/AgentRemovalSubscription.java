/*
 * 
 */
package muscle.core.ident;

import cern.colt.Arrays;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.proto.SubscriptionInitiator;
import jade.util.leap.Iterator;
import java.util.logging.Logger;
import muscle.Constant;
import muscle.core.Resolver;
import muscle.core.ident.IDType;
import muscle.core.ident.Identifier;
import muscle.core.ident.JadeAgentID;
import muscle.core.ident.JadeIdentifier;
import muscle.core.ident.JadeLocation;
import muscle.core.ident.Location;
import muscle.exception.MUSCLERuntimeException;

/**
 *
 * @author Joris Borgdorff
 */
public class AgentRemovalSubscription extends SubscriptionInitiator {
	private transient final static Logger logger = Logger.getLogger(AgentRemovalSubscription.class.getName());
	private final Resolver resolver;
	
	private AgentRemovalSubscription(Agent a, Resolver r, ACLMessage msg) {
		super(a, msg);
		this.resolver = r;
	}
	
	public static ServiceDescription getAgentServices() {
		ServiceDescription service = new ServiceDescription();
		
		service.addProtocols(Constant.Protocol.DETACH_AGENT);
		service.addProtocols(Constant.Service.AGENT);
		return service;
	}
	
	public static AgentRemovalSubscription getAgentSubscription(Agent a, Resolver r) {
		DFAgentDescription agentDescription = new DFAgentDescription();
		
		agentDescription.addServices(getAgentServices());
		
		ACLMessage msg = DFService.createSubscriptionMessage(a, a.getDefaultDF(), agentDescription, new SearchConstraints());
		return new AgentRemovalSubscription(a, r, msg);
	}

	protected void handleInform(ACLMessage inform) {
		//System.out.println("Agent "+getLocalName()+": Notification received from DF: msg:\n"+inform.toString()+"\n\n");
		//System.out.println("performative:"+ACLMessage.getPerformative(inform.getPerformative()));
		//System.out.println("replyto:"+inform.getInReplyTo()); // matches the subscription message
		try {
			DFAgentDescription[] results = DFService.decodeNotification(inform.getContent());
			System.out.println("Search completed; " + Arrays.toString(results));
			//System.out.println("results:"+results.length);
			for (int i = 0; i < results.length; ++i) {
				DFAgentDescription dfd = results[i];
				
				// Service contains the location
				Iterator services = dfd.getAllServices();
				if (services.hasNext()) {
					ServiceDescription sd = (ServiceDescription)services.next();
					resolver.removeIdentifier(sd.getName(), IDType.instance);
				}
			}
		} catch (FIPAException e) {
			throw new MUSCLERuntimeException(e);
		}
	}
}
