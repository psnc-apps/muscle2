/*
 * 
 */
package muscle.core.ident;

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
import muscle.exception.MUSCLERuntimeException;

/**
 * Listens for removed agents
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
		try {
			DFAgentDescription[] results = DFService.decodeNotification(inform.getContent());
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
