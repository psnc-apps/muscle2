/*
 * 
 */

package muscle.monitor;

import cern.colt.map.OpenIntIntHashMap;
import java.awt.BorderLayout;
import java.awt.Container;
import javax.swing.JFrame;
import javax.swing.WindowConstants;
import muscle.id.Location;
import muscle.id.TcpLocation;
import muscle.util.logging.ActivityProtocol;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Element;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.layout.springbox.implementations.LinLog;
import org.graphstream.ui.swingViewer.Viewer;

/**
 *
 * @author Joris Borgdorff
 */
public class GraphViewer {
	private final Graph graph;
	private final static String stylesheet = "graph { padding: 100px; }\n"
			+ "node { text-alignment: at-right; text-padding: 3px, 2px; text-background-mode: rounded-box; text-color: white; text-background-color: #800C; text-style: bold-italic; text-color: #FFF; text-offset: 5px, 0px; text-size: 16; fill-color: red; }\n"
			+ "node.finished { fill-color: gray; text-background-color: gray; }\n"
			+ "node.notcomputing { fill-color: black; text-background-color: #A7CC; }"
			+ "edge { text-size: 16; text-padding: 3px, 2px; text-background-mode: rounded-box; text-background-color: #CCCC; }\n"
			+ "edge.receiving { fill-color: #7FF; }\n"
			+ "edge.connected { size: 3px; }\n"
			+ "edge.sending { shadow-mode: plain; shadow-width: 3px; shadow-color: #FC0; shadow-offset: 0px; }\n"
			+ "edge.failed { fill-color: red; }\n"
			+ "sprite { size: 0px; text-color: rgb(150,100,100); text-size: 14; }\n";
	private final LinLog layout;
	private final OpenIntIntHashMap hashes;
	private final GraphPanel panel;
	
	public GraphViewer() {
		System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
		
		graph = new SingleGraph("graph");
		graph.addAttribute("ui.stylesheet", stylesheet);
		graph.addAttribute("ui.quality");
		graph.addAttribute("ui.antialias");
		graph.addAttribute("layout.quality", 2);
		graph.addAttribute("layout.weight", 0.5);
		
		hashes = new OpenIntIntHashMap();
		
		layout = new LinLog();
		panel = new GraphPanel();
	}
	
	void beginUpdate() {
	}
	void endUpdate() {
	}
	
	void addNode(int hash, String id) {
		Node v = graph.addNode(id);
		int loc = hashes.get(hash) + 1;
		v.addAttribute("ui.label", String.format("%s [%d]", id, loc));
	}

	void addEdge(String from, String to, String label) {
		Node vf = graph.getNode(from), vt = graph.getNode(to);
		String id = from + "->" + to;
		if (vf != null && vt != null && graph.getEdge(id) == null) {
			graph.addEdge(id, vf, vt, true);
		}
		layout.shake();
	}

	void removeNode(String id) {
		Node v = graph.getNode(id);
		addClass(v, "finished");
	}

	void receive(ActivityProtocol activity, String from, String to, String receivePort) {
		Node vt = graph.getNode(to);
		switch (activity) {
			case BEGIN_RECEIVE:
				addClass(vt, "notcomputing");
				break;
			case END_RECEIVE:
				removeClass(vt, "notcomputing");
				break;
			case RECEIVE_FAILED:
				removeClass(vt, "notcomputing");
				break;
		}
		
		Edge e = graph.getEdge(from + "->" + to);
		if (e == null) return; // don't show actions on edges that do not yet exist
		
		switch (activity) {
			case BEGIN_RECEIVE:
				addClass(e, "receiving");
				addLabel(e, "receiving " + receivePort, "receiving");
				break;
			case END_RECEIVE:
				removeClass(e, "receiving");
				removeLabel(e, "receiving");
				break;
			case RECEIVE_FAILED:
				addClass(e, "failed");
				break;
		}
	}

	void send(ActivityProtocol activity, String from, String to, String sendPort) {
		Node vf = graph.getNode(from);
		
		switch (activity) {
			case BEGIN_SEND:
				addClass(vf, "notcomputing");
				break;
			case END_SEND:
				removeClass(vf, "notcomputing");
				break;
		}
		
		Edge e = graph.getEdge(from + "->" + to);
		if (e == null) return; // don't show actions on edges that do not yet exist
		
		switch (activity) {
			case BEGIN_SEND:
				addClass(e, "sending");
				addLabel(e, "sending " + sendPort, "sending");
				break;
			case END_SEND:
				removeClass(e, "sending");
				removeLabel(e, "sending");
				break;
			case CONNECTED:
				addClass(e, "connected");
				break;
		}		
	}
		
	private void addClass(Element el, String clazz) {
		String clazzes = el.getAttribute("ui.class");
		el.addAttribute("ui.class", addToList(clazzes, clazz, clazz, ","));
	}
	private void removeClass(Element el, String clazz) {
		String clazzes = el.getAttribute("ui.class");
		el.addAttribute("ui.class", removeFromList(clazzes, clazz, ","));
	}
	private void addLabel(Element el, String label, String match) {
		String oldLabel = el.getAttribute("ui.label");
		el.addAttribute("ui.label", addToList(oldLabel, label, match, "; "));
	}
	private void removeLabel(Element el, String match) {
		String oldLabel = el.getAttribute("ui.label");
		el.addAttribute("ui.label", removeFromList(oldLabel, match, "; "));
	}
	
	private String addToList(String list, String elem, String match, String delimiter) {
		String newList = removeFromList(list, match, delimiter);
		return newList == null ? elem : newList + delimiter + elem;
	}
	
	private String removeFromList(String list, String match, String delimiter) {
		if (list == null)
			return null;
		
		String newList = null;
		boolean matched = false;
		for (String line : list.split(delimiter)) {
			if (!matched && line.startsWith(match)) {
				matched = true; // remove this match, once
			} else if (newList == null) {
				newList = line;
			} else {
				newList += delimiter + line;
			}
		}
		return newList;
	}

	void display() {
		Viewer v = new Viewer(graph, Viewer.ThreadingModel.GRAPH_IN_SWING_THREAD);
		panel.setView(v.addDefaultView(false));
		v.enableAutoLayout(layout);

		JFrame main = new JFrame("MUSCLE monitor");
		main.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		main.setSize(800, 600);
		Container content = main.getContentPane();
		content.setLayout(new BorderLayout());
		content.add(panel, BorderLayout.CENTER);
		main.setVisible(true);
	}

	void addContainer(int hash, String id, Location loc) {
		int num = hashes.size();
		hashes.put(hash, num);
		TcpLocation tloc = (TcpLocation) loc;
		String hostname = tloc.getAddress().getHostName(); // resolve the host name for pretty printing
		int port = tloc.getPort();
		panel.addMuscleText(String.format("%d: %s:%d", num + 1, hostname, port));
	}

	void removeContainer(int hash, String id) {
	}

	public boolean isClosed() {
		return graph.hasAttribute("ui.viewClosed");
	}
}
