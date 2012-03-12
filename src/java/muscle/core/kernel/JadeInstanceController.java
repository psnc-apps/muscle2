/*
 * 
 */
package muscle.core.kernel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import muscle.core.*;
import muscle.core.conduit.communication.JadeReceiver;
import muscle.core.conduit.communication.Receiver;
import muscle.core.ident.Identifier;
import muscle.core.ident.PortalID;
import muscle.core.ident.Resolver;
import muscle.core.messaging.jade.DataMessage;

/**
 *
 * @author Joris Borgdorff
 */
public class JadeInstanceController extends MultiDataAgent implements InstanceController, InstanceControllerListener {
	private final static Logger logger = Logger.getLogger(JadeInstanceController.class.getName());
	private final transient PortFactory factory = PortFactory.getInstance();
	private transient Map<String,ExitDescription> exitDescriptions;
	private transient Map<String,EntranceDescription> entranceDescriptions;
	private final static boolean ENTRANCE = true;
	private final static boolean EXIT = false;
	
	private final List<ConduitExitController<?>> dataSources = new ArrayList<ConduitExitController<?>>(); // these are the conduit exits
	private final List<ConduitEntranceController<?>> dataSinks = new ArrayList<ConduitEntranceController<?>>(); // these are the conduit entrances
	private ThreadedInstanceController realController = null;
	
	public <T extends Serializable> void addConduitEntrance(ConduitEntranceController<T> s) {
		s.start();
		PortalID other = resolvePort(s.getIdentifier(), entranceDescriptions, ENTRANCE);
		if (other == null) return;
		s.setTransmitter(factory.<T>getTransmitter(this, other));
		dataSinks.add(s);
	}

	public <T extends Serializable> void addConduitExit(ConduitExitController<T> s) {
		s.start();
		PortalID other = resolvePort(s.getIdentifier(), exitDescriptions, EXIT);
		if (other == null) return;
		Receiver<DataMessage<T>, ?, ?, ?> recv = factory.<DataMessage<T>>getReceiver(this, other);
		s.setReceiver(recv);
		messageProcessor.addReceiver(s.getIdentifier(), (JadeReceiver)recv);
		dataSources.add(s);
	}
	
	private PortalID resolvePort(PortalID id, Map<String,? extends PortDescription> descriptions, boolean entrance) {
		ConduitDescription desc = null;
		if (descriptions != null)
			desc = descriptions.get(id.getPortName()).getConduitDescription();
		if (desc == null)
			throw new IllegalStateException("Port " + id + " is initialized in code but is not listed in the connection scheme. It will not work until this port is added in the connection scheme.");

		PortalID other = entrance ? desc.getExitDescription().getID() : desc.getExitDescription().getID();
		if (!other.isResolved()) {
			try {
				Resolver r = Boot.getInstance().getResolver();
				r.resolveIdentifier(other);
				if (!other.isResolved()) return null;
			} catch (InterruptedException ex) {
				logger.log(Level.SEVERE, "Resolving port interrupted", ex);
			}
		}
		return other;
	}

	@Override
	public void takeDown() {
		super.takeDown();
		
		for (ConduitExitController<?> source : dataSources) {
			source.dispose();
		}
		for (ConduitEntranceController<?> sink : dataSinks) {
			sink.dispose();
		}

		this.realController.dispose();
		doDelete();
	}
	
	@Override
	final protected void setup() {
		super.setup();

		Identifier id = getIdentifier();
		ConnectionScheme cs = ConnectionScheme.getInstance();
		exitDescriptions = cs.exitDescriptionsForIdentifier(id);
		entranceDescriptions = cs.entranceDescriptionsForIdentifier(id);
		
		try {
			Boot boot = Boot.getInstance();
			Class clazz = Class.forName(boot.getAgentClass(this.getLocalName()));
			realController = new ThreadedInstanceController(id, clazz, this, boot, super.getArguments());
			realController.setMainController(this);
			new Thread(realController).start();
		} catch (ClassNotFoundException ex) {
			Logger.getLogger(JadeInstanceController.class.getName()).log(Level.SEVERE, "Could not load class " + Boot.getInstance().getAgentClass(this.getLocalName()) + " of instance " + getLocalName(), ex);
		}
	}

	public String toString() {
		return getClass().getSimpleName() + "[" + getIdentifier() + "]";
	}

	@Override
	public void isFinished(InstanceController ic) {
		takeDown();
	}
}
