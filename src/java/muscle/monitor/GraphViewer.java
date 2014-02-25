/*
 * 
 */

package muscle.monitor;

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
	private final static String stylesheet = "node { text-alignment: at-right; text-padding: 3px, 2px; text-background-mode: rounded-box; text-color: white; text-background-color: #800C; text-style: bold-italic; text-color: #FFF; text-offset: 5px, 0px; text-size: 16; fill-color: red; }\n"
			+ "node.finished { fill-color: gray; text-background-color: gray; }\n"
			+ "node.notcomputing { fill-color: black; text-background-color: #A7CC; }"
			+ "edge { text-size: 16; }\n"
			+ "edge.receiving { fill-color: #7FF; }\n"
			+ "edge.connected { size: 3px; }\n"
			+ "edge.sending { shadow-mode: plain; shadow-width: 3px; shadow-color: #FC0; shadow-offset: 0px; }\n"
			+ "edge.failed { fill-color: red; }\n";
	private final LinLog layout;
	
	public GraphViewer() {
		System.setProperty("gs.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
		
		graph = new SingleGraph("graph");
		graph.addAttribute("ui.stylesheet", stylesheet);
		graph.addAttribute("ui.quality");
		graph.addAttribute("ui.antialias");
		graph.addAttribute("layout.quality", 2);
//		graph.addAttribute("layout.force", 4);
		graph.addAttribute("layout.weight", 0.5);
		
		layout = new LinLog();
	}
	
	void beginUpdate() {
	}
	void endUpdate() {
	}
	
	void addNode(int hash, String id) {
		Node v = graph.addNode(id);
		v.addAttribute("ui.label", id);

//		Object parent;
//		
//		
//		if (!locations.containsKey(hash)) {
//			String hex = Integer.toHexString(hash);
//			locations.put(hash, graph.insertVertex(graph.getDefaultParent(), hex, hex, 240, 150, 80, 30, "strokeColor=grey"));
//		}
//		parent = locations.get(hash);
//		Object v = graph.insertVertex(parent, id, id, 240, 150, 80, 30, "strokeColor=black;ROUNDED");
//		vertices.put(id, v);
	}

	void addEdge(String from, String to, String label) {
		Node vf = graph.getNode(from), vt = graph.getNode(to);
		String id = from + "->" + to;
		if (vf != null && vt != null && graph.getEdge(id) == null) {
			Edge e = graph.addEdge(id, vf, vt, true);
//			e.addAttribute("ui.label", label);
		}
		layout.shake();
	}

	void removeNode(String id) {
		Node v = graph.getNode(id);
		addClass(v, "finished");
		do {} while (removeClass(v, "notcomputing"));
//		graph.removeNode(id);
	}

	void receive(ActivityProtocol activity, String from, String to) {
		Edge e = graph.getEdge(from + "->" + to);
		if (e == null) return;
		Node vt = graph.getNode(to);
		
		switch (activity) {
			case END_RECEIVE:
				removeClass(e, "receiving");
				removeClass(vt, "notcomputing");
				break;
			case BEGIN_RECEIVE:
				addClass(e, "receiving");
				addClass(vt, "notcomputing");
				break;
			case RECEIVE_FAILED:
				addClass(e, "failed");
				removeClass(vt, "notcomputing");
				break;
		}
	}

	void send(ActivityProtocol activity, String from, String to) {
		Edge e = graph.getEdge(from + "->" + to);
		if (e == null) return;
		Node vf = graph.getNode(from);
		
		switch (activity) {
			case END_SEND:
				removeClass(e, "sending");
				removeClass(vf, "notcomputing");
				break;
			case BEGIN_SEND:
				addClass(e, "sending");
				addClass(vf, "notcomputing");
				break;
			case CONNECTED:
				addClass(e, "connected");
				break;
		}
	}
	
	private void addClass(Element el, String clazz) {
		String clazzes = el.getAttribute("ui.class");
		System.out.println("Adding " + clazz + " to '" + clazzes + "' of " + el.getId());
		if (clazzes == null) {
			clazzes = clazz;
		} else {
			clazzes = clazzes + "," + clazz;
		}
		System.out.println("Result: " + clazzes);
		el.addAttribute("ui.class", clazzes);
	}
	private boolean removeClass(Element el, String clazz) {
		String clazzes = el.getAttribute("ui.class");
		boolean ret = false;
		System.out.println("Removing " + clazz + " from '" + clazzes + "' of " + el.getId());
		if (clazzes != null) {
			String[] clss = clazzes.split(",");
			clazzes = "";
			for (String cls : clss) {
				if (cls.equals(clazz)) {
					clazz = "========================"; // don't match again
					ret = true;
				} else {
					clazzes +=  "," + cls;
				}
			}
			System.out.println("Result: " + clazzes);
			if (clazzes.isEmpty()) {
				el.removeAttribute("ui.class");
			} else {
				el.addAttribute("ui.class", clazzes.substring(1));
			}
		}
		return ret;
	}
	void display() {
		Viewer v = new Viewer(graph, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
		v.addDefaultView(true);

		v.enableAutoLayout(layout);
	}
}
