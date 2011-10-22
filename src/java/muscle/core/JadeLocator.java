/*
 * 
 */
package muscle.core;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.AMSService;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.FIPAManagementOntology;
import jade.domain.FIPAAgentManagement.FIPAManagementVocabulary;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.RequestFIPAServiceBehaviour;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import muscle.Constant;
import muscle.core.ident.IDType;
import muscle.core.ident.Location;
import muscle.core.ident.Identifier;
import muscle.core.ident.JadeAgentID;
import muscle.core.ident.JadeIdentifier;
import muscle.core.ident.JadeLocation;
import muscle.core.kernel.InstanceController;
import utilities.ArrayMap;

/**
 *
 * @author Joris Borgdorff
 */
public class JadeLocator extends Agent implements Locator {
	private final Map<Identifier,Location> locCache;
	private final Map<String,Identifier> idCache;
	private Location here;
	
	protected JadeLocator() {
		locCache = new ArrayMap<Identifier,Location>();
		idCache = new ArrayMap<String,Identifier>();
	}
	
	@Override
	public void setup() {
		this.here = new JadeLocation(here());
		Boot.getInstance().registerLocator(this);
	}
	
	public synchronized Identifier getIdentifier(String name) {
		while (!idCache.containsKey(name)) {
			try {
				wait();
			} catch (InterruptedException ex) {
				Logger.getLogger(JadeLocator.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
		return idCache.get(name);
	}
	
	public synchronized Location getLocation(Identifier id) {
		while (!locCache.containsKey(id)) {
			if (!(id instanceof JadeAgentID)) {
				throw new IllegalArgumentException("JadeLocator can only find the locations of JadeAgentIDs.");
			}
			JadeAgentID jid = (JadeAgentID)id;
			new LocateAID().start();
		}
		Location loc = locCache.get(id);
		if (loc == null) {
		}
		return loc;
	}
	
	public synchronized boolean isLocal(Identifier id) {
		return this.locCache.containsKey(id);
	}

	public synchronized void register(InstanceController controller) {
		Identifier id = controller.getIdentifier();
		this.locCache.put(id, here);
		this.idCache.put(id.getName(), id);
		
		DFAgentDescription agentDescription = new DFAgentDescription();
		agentDescription.setName(((JadeIdentifier)id).getAID());

		ServiceDescription offerSelf = new ServiceDescription();
		offerSelf.setType(id.getType().name()); // this is mandatory
		offerSelf.setName(id.getName()); // this is mandatory
		agentDescription.addServices(offerSelf);
		
		// register
		try {
			DFService.register(this, agentDescription);
		} catch (FIPAException e) {
			throw new RuntimeException(e);
		}
	}

	public void searchIdentifier(final String name, final IDType type) {
		SequentialBehaviour sb = new SequentialBehaviour();
		final AMSAgentDescription aad = new AMSAgentDescription();
		aad.setName(new AID(name, AID.ISLOCALNAME));
		try {
			DF
			RequestFIPAServiceBehaviour search = AMSService.getNonBlockingBehaviour(this, FIPAManagementOntology.SEARCH, aad, new SearchConstraints());
			search.
			sb.addSubBehaviour(search);
			sb.addSubBehaviour(new OneShotBehaviour() {
				@Override
				public void action() {
					Identifier id =  new JadeAgentID(name, aad.getName());
					JadeLocator.this.idCache.put(name, id);
					JadeLocator.this.locCache.put(id, aad.getName());
				}
			});
		} catch (FIPAException ex) {
			Logger.getLogger(JadeLocator.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
	private class IdentifierSearcher extends ConduitSubscriptionInitiator {
		
	}
}
