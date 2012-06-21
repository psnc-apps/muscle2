/*
 * 
 */
package muscle.core.ident;

import jade.core.Agent;
import jade.core.behaviours.SequentialBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.Property;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.proto.SubscriptionInitiator;
import java.util.Set;
import muscle.behaviour.KillPlatformBehaviour;
import muscle.behaviour.PrintPlatformAgentsBehaviour;
import muscle.core.Boot;

/**
 *
 * @author Joris Borgdorff
 */
public class JadeAgentIDManipulator extends Agent implements IDManipulator {
	private DelegatingResolver owner;
		
	public Location getLocation() {
		return new JadeLocation(here());
	}
	
	@Override
	public void setup() {
		Boot boot = Boot.getInstance();
		Set<String> agentNames = boot.monitorQuit();
		owner = new DelegatingResolver(this, agentNames);
		if (agentNames != null) {
			SubscriptionInitiator si = AgentRemovalSubscription.getAgentSubscription(this, owner);
			addBehaviour(si);
		}

		boot.registerResolver(owner);
	}
	
	@Override
	public void takeDown() {
		if (owner != null) owner.dispose();
	}

	public void search(Identifier id) {
		checkJade(id);
		SubscriptionInitiator si = AgentSubscriptionInitiator.getAgentSubscription(this, owner, (JadeIdentifier)id);
		addBehaviour(si);
	}
	
	private static void checkJade(Identifier id) {
		if (!(id instanceof JadeIdentifier)) {
			throw new IllegalArgumentException("Can only resolve JADE ids.");
		}
	}
	
	public boolean delete(Identifier id) {
		DFAgentDescription agentDescription = new DFAgentDescription();
		agentDescription.setName(((JadeIdentifier)id).getAID());

		ServiceDescription offerSelf = AgentRemovalSubscription.getAgentServices();
		offerSelf.setName(id.getName());
		offerSelf.setType(id.getType().name());
		
		agentDescription.addServices(offerSelf);
		
		// register
		try {
			DFService.modify(this, agentDescription);
			return true;
		} catch (FIPAException e) {
			return false;
		}
	}

	@Override
	public boolean register(Identifier id, Location loc) {
		DFAgentDescription agentDescription = new DFAgentDescription();
		agentDescription.setName(((JadeIdentifier)id).getAID());

		ServiceDescription offerSelf = AgentSubscriptionInitiator.getAgentServices(id);

		// Add location
		for (Property p : (JadeLocation)id.getLocation()) {
			offerSelf.addProperties(p);
		}
		
		agentDescription.addServices(offerSelf);
		
		// register
		try {
			DFService.register(this, agentDescription);
			return true;
		} catch (FIPAException e) {
			return false;
		}
	}
	
	@Override
	public boolean propagate(Identifier id) {
		// Natively done by JADE
		return true;
	}

	public Identifier create(String name, IDType type) {
		if (type == IDType.instance) {
			return new JadeAgentID(name);
		}
		else if (type == IDType.port) {
			String[] portId = name.split("@");
			if (portId.length != 2) {
				throw new IllegalArgumentException("A port identifier <" + portId + "> must feature a port and owner name, separated by an '@' symbol.");
			}
			return new JadePortalID(portId[0], portId[1]);
		}
		else {
			throw new IllegalArgumentException("JadeResolver can only resolve ports and instances, not '" + type + "'.");
		}
	}

	public void deletePlatform() {
		SequentialBehaviour tearDownBehaviour = new SequentialBehaviour(this);

		tearDownBehaviour.addSubBehaviour(new PrintPlatformAgentsBehaviour(this));
		tearDownBehaviour.addSubBehaviour(new KillPlatformBehaviour(this));	

		addBehaviour(tearDownBehaviour);
	}
}
