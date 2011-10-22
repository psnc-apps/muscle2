/*
 * 
 */
package muscle.core;

import jade.core.Agent;
import jade.core.behaviours.SequentialBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.Property;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.proto.SubscriptionInitiator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import muscle.behaviour.KillPlatformBehaviour;
import muscle.behaviour.PrintPlatformAgentsBehaviour;
import muscle.core.ident.IDType;
import muscle.core.ident.Identifier;
import muscle.core.ident.JadeAgentID;
import muscle.core.ident.JadeIdentifier;
import muscle.core.ident.JadeLocation;
import muscle.core.ident.JadePortalID;
import muscle.core.ident.Location;
import muscle.core.ident.PortalID;
import utilities.ArraySet;

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
		Set<String> agents = null;
		if (boot.monitorQuit()) {
			agents = new ArraySet<String>(boot.getAgentNames());
		}
		owner = new DelegatingResolver(this, agents);
		boot.registerResolver(owner);
	}
	
	@Override
	public void takeDown() {
		if (owner != null) owner.dispose();
	}
	
	public void resolve(Identifier id) throws InterruptedException {
		if (id.getType() == IDType.port) {
			resolve(((PortalID)id).getOwnerID());
			return;
		}
		
		checkJade(id);
		Identifier newID = owner.getResolvedIdentifier(id.getName(), id.getType());
		checkJade(newID);
		
		JadeIdentifier newJID = (JadeIdentifier)newID;
		((JadeIdentifier)id).resolve(newJID.getAID(), newJID.getLocation());
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
	
	public void delete(Identifier id) {
		try {
			DFService.deregister(this, ((JadeIdentifier)id).getAID());
		} catch (FIPAException ex) {
			throw new RuntimeException(ex);
		}
	}

	public void propagate(Identifier id, Location loc) {
		DFAgentDescription agentDescription = new DFAgentDescription();
		agentDescription.setName(((JadeIdentifier)id).getAID());

		ServiceDescription offerSelf = new ServiceDescription();
		offerSelf.setType(id.getType().name()); // this is mandatory
		offerSelf.setName(id.getName()); // this is mandatory
		// Add location
		for (Property p : (JadeLocation)loc) {
			offerSelf.addProperties(p);
		}
		
		agentDescription.addServices(offerSelf);
		
		// register
		try {
			DFService.register(this, agentDescription);
		} catch (FIPAException e) {
			throw new RuntimeException(e);
		}
	}

	public Identifier create(String name, IDType type) {
		if (type == IDType.instance) {
			return new JadeAgentID(name);
		}
		else if (type == IDType.port) {
			String[] portId = name.split("@");
			if (portId.length != 2) {
				throw new IllegalArgumentException("A port identifier <" + portId + "> must feature a port and owner name, separated by an '@'.");
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
