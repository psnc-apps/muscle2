/*
 * 
 */
package muscle.client.id;

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
import muscle.id.Identifier;
import muscle.id.Location;
import muscle.id.Resolver;

/**
 * Looks for agents
 * @author Joris Borgdorff
 */
public class AgentSubscriptionInitiator extends SubscriptionInitiator {
	private transient final static Logger logger = Logger.getLogger(AgentSubscriptionInitiator.class.getName());
	private final Resolver resolver;
	private final JadeIdentifier jid;
	
	private AgentSubscriptionInitiator(Agent a, Resolver r, JadeIdentifier jid, ACLMessage msg) {
		super(a, msg);
		this.resolver = r;
		this.jid = jid;
	}
	
	public static ServiceDescription getAgentServices(Identifier id) {
		ServiceDescription service = new ServiceDescription();
		
		service.addProtocols(Constant.Protocol.ANNOUNCE_AGENT);
		service.addProtocols(Constant.Service.AGENT);
		service.setType(id.getType().name());
		service.setName(id.getName());
		return service;
	}
	
	public static AgentSubscriptionInitiator getAgentSubscription(Agent a, Resolver r, JadeIdentifier id) {
		DFAgentDescription agentDescription = new DFAgentDescription();
		
		agentDescription.addServices(getAgentServices(id));
		
		ACLMessage msg = DFService.createSubscriptionMessage(a, a.getDefaultDF(), agentDescription, new SearchConstraints());
		return new AgentSubscriptionInitiator(a, r, id, msg);
	}

	protected void handleInform(ACLMessage inform) {
		//System.out.println("Agent "+getLocalName()+": Notification received from DF: msg:\n"+inform.toString()+"\n\n");
		//System.out.println("performative:"+ACLMessage.getPerformative(inform.getPerformative()));
		//System.out.println("replyto:"+inform.getInReplyTo()); // matches the subscription message
		try {
			DFAgentDescription[] results = DFService.decodeNotification(inform.getContent());
			for (int i = 0; i < results.length; ++i) {
				DFAgentDescription dfd = results[i];
				
				// Service contains the location
				Iterator services = dfd.getAllServices();
				if (services.hasNext()) {
					ServiceDescription sd = (ServiceDescription)services.next();
					Location loc = new JadeLocation(sd);
					jid.resolve(dfd.getName(), loc);
					resolver.addResolvedIdentifier(jid);
				}
				else {
					resolver.removeIdentifier(jid.getName(), jid.getType());
				}
			}
		} catch (FIPAException e) {
			throw new MUSCLERuntimeException(e);
		}
	}
}
