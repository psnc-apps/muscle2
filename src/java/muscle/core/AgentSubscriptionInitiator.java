/*
 * 
 */
package muscle.core;

import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.proto.SubscriptionInitiator;
import java.util.logging.Logger;
import muscle.Constant;
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
public class AgentSubscriptionInitiator extends SubscriptionInitiator {
	private transient final static Logger logger = Logger.getLogger(AgentSubscriptionInitiator.class.getName());
	private final Resolver outer;
	private final JadeIdentifier jid;
	
	private AgentSubscriptionInitiator(Agent a, Resolver r, JadeIdentifier jid, ACLMessage msg) {
		super(a, msg);
		this.outer = r;
		this.jid = jid;
	}
	
	public static AgentSubscriptionInitiator getAgentSubscription(Agent a, Resolver r, JadeIdentifier id) {
		DFAgentDescription agentDescription = new DFAgentDescription();
		ServiceDescription pointDescription = new ServiceDescription();
		
		pointDescription.addProtocols(Constant.Protocol.ANNOUNCE_AGENT);
		pointDescription.addProtocols(Constant.Service.AGENT);
		pointDescription.setType(id.getType().name());
		pointDescription.setName(id.getName());
		
		agentDescription.addServices(pointDescription);
		
		ACLMessage msg = DFService.createSubscriptionMessage(a, a.getDefaultDF(), agentDescription, new SearchConstraints());
		return new AgentSubscriptionInitiator(a, r, id, msg);
	}

	protected void handleInform(ACLMessage inform) {
		//System.out.println("Agent "+getLocalName()+": Notification received from DF: msg:\n"+inform.toString()+"\n\n");
		//System.out.println("performative:"+ACLMessage.getPerformative(inform.getPerformative()));
		//System.out.println("replyto:"+inform.getInReplyTo()); // matches the subscription message
		try {
			DFAgentDescription[] results = DFService.decodeNotification(inform.getContent());
			//System.out.println("results:"+results.length);
			for (int i = 0; i < results.length; ++i) {
				DFAgentDescription dfd = results[i];
				ServiceDescription sd = (ServiceDescription)dfd.getAllServices().next();
				Location loc = new JadeLocation(sd);
				jid.resolve(dfd.getName(), loc);
				outer.addIdentifier(jid);
			}
		} catch (FIPAException e) {
			throw new MUSCLERuntimeException(e);
		}
	}
}
