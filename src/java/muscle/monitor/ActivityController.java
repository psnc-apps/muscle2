/*
 * 
 */

package muscle.monitor;

import cern.colt.map.AbstractIntObjectMap;
import cern.colt.map.OpenIntObjectHashMap;
import eu.mapperproject.jmml.util.ArraySet;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import muscle.client.id.DelegatingResolver;
import muscle.client.id.TcpIDManipulator;
import muscle.core.ConduitDescription;
import muscle.core.ConnectionScheme;
import muscle.id.IDType;
import muscle.id.Identifier;
import muscle.id.Location;
import muscle.id.PortalID;
import muscle.util.logging.ActivityProtocol;

/**
 *
 * @author Joris Borgdorff
 */
public class ActivityController {
	private final BlockingQueue<Activity> activities;
	private final AbstractIntObjectMap locations;
	private final DelegatingResolver resolver;
	private final ConnectionScheme conns;
	private final GraphViewer viewer;
	
	public static void main(String[] args) throws SocketException, InterruptedException, UnknownHostException {
		TcpIDManipulator idRes = new TcpIDManipulator(null, null, null);
		DelegatingResolver resolver = new DelegatingResolver(idRes, new ArraySet<String>(0));
		idRes.setResolver(resolver);
		ConnectionScheme conns = new ConnectionScheme(resolver, 10);

		ActivityController controller = new ActivityController(conns, resolver);
		UDPActivityListener udp = new UDPActivityListener(controller);
		udp.start();
		
		controller.execute();
	}
	
	public ActivityController(ConnectionScheme conns, DelegatingResolver resolver) {
		this.conns = conns;
		this.resolver = resolver;
		activities = new LinkedBlockingQueue();
		locations = new OpenIntObjectHashMap();
		viewer = new GraphViewer();
	}
	
	public void execute() throws InterruptedException {
		viewer.display();
		
		while (true) {
			Activity act = activities.take();
			
			if (act.activity.type == IDType.instance) {
				switch (act.activity) {
					case START:
						viewer.addNode(act.hash, act.id);
						Identifier id = resolver.getIdentifier(act.id, IDType.instance);
						Map<String,ConduitDescription> descs = conns.entranceDescriptionsForIdentifier(id);
						if (descs != null) {
							for (ConduitDescription desc :  descs.values()) {
								String to = desc.getExit().getOwnerID().getName();
								viewer.addEdge(act.id, to, desc.toString());
							}
						}
						descs = conns.exitDescriptionsForIdentifier(id);
						if (descs != null) {
							for (ConduitDescription desc :  descs.values()) {
								String from = desc.getEntrance().getOwnerID().getName();
								viewer.addEdge(from, act.id, desc.toString());
							}
						}
						break;
					case STOP:
						viewer.removeNode(act.id);
						break;
				}
			} else if (act.activity.type == IDType.port) {
				PortalID portid = (PortalID)resolver.getIdentifier(act.id, IDType.port);
				ConduitDescription desc;
				
				switch (act.activity) {
					case BEGIN_RECEIVE:
					case END_RECEIVE:
					case RECEIVE_FAILED:
						desc = conns.exitDescriptionForPortal(portid);
						viewer.receive(act.activity, desc.getEntrance().getOwnerID().getName(), portid.getOwnerID().getName());
						break;
					case BEGIN_SEND:
					case END_SEND:
					case CONNECTED:
						desc = conns.entranceDescriptionForPortal(portid);
						viewer.send(act.activity, portid.getOwnerID().getName(), desc.getExit().getOwnerID().getName());
						break;
				}
			}

			Thread.sleep(200);
		}
	}
	
	public void addContainer(int hash, Location loc) {	
		locations.put(hash, loc);
	}

	public void removeContainer(int hash) {
	}

	public void action(int hash, String id, ActivityProtocol activity) {
		activities.add(new Activity(hash, (Location)locations.get(hash), id, activity));
	}
	
	class Activity {
		final int hash;
		final Location loc;
		final String id;
		final ActivityProtocol activity;
		Activity(int hash, Location loc, String id, ActivityProtocol activity) {
			this.hash = hash;
			this.loc = loc;
			this.id = id;
			this.activity = activity;
		}
	}
}
