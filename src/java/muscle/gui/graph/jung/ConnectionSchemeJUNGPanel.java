/*
Copyright 2008,2009 Complex Automata Simulation Technique (COAST) consortium

GNU Lesser General Public License

This file is part of MUSCLE (Multiscale Coupling Library and Environment).

    MUSCLE is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    MUSCLE is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with MUSCLE.  If not, see <http://www.gnu.org/licenses/>.
*/

package muscle.gui.graph.jung;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.Shape;
import java.util.ArrayList;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import javax.swing.border.LineBorder;

import muscle.core.ConnectionScheme;
import muscle.gui.graph.ConnectionSchemeViewable;
import muscle.gui.graph.Edge;
import muscle.gui.graph.Vertex;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.decorators.PickableEdgePaintTransformer;
import edu.uci.ics.jung.visualization.decorators.PickableVertexPaintTransformer;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;


/**
shows the connection scheme graph using JUNG
@author Christian Sch�ps
@author Jan Hegewald
*/
public class ConnectionSchemeJUNGPanel extends JPanel implements ConnectionSchemeViewable {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private JPanel panelLeft, panelRight;
	private DirectedSparseMultigraph<Vertex, Edge> graph = new DirectedSparseMultigraph<Vertex, Edge>();
	private Layout<Vertex, Edge> layout;
	private VisualizationViewer<Vertex, Edge> vv;
	private ArrayList<JLabel> selectedElement = new ArrayList<JLabel>();
//	private ConnectionSchemeController controller;


	//
	public ConnectionSchemeJUNGPanel() {

		//Panellayout
		this.setLayout(null);
		this.setPreferredSize(new Dimension(1070,420));

		//GraphLayout
		this.layout = new CircleLayout<Vertex, Edge>(this.graph);
		this.layout.setSize(new Dimension(800,400));
		this.vv = new VisualizationViewer<Vertex,Edge>(this.layout, new Dimension(800,400));
		this.vv.getRenderer().setEdgeRenderer(new EdgeRenderer<Vertex,Edge>());

		// init transformer
		final Shape shape = new Rectangle(50,50);
		Transformer<Vertex, Shape> trans = new Transformer<Vertex, Shape>() {
			public Shape transform(Vertex arg0) {
				return shape;
			}
		};

		// colours and lables
		this.vv.getRenderContext().setEdgeDrawPaintTransformer(new PickableEdgePaintTransformer<Edge>(this.vv.getPickedEdgeState(), Color.black, Color.blue));
		this.vv.getRenderContext().setVertexFillPaintTransformer(new PickableVertexPaintTransformer<Vertex>(this.vv.getPickedVertexState(), Color.white, Color.green));
		this.vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller<Vertex>());
		this.vv.getRenderContext().setEdgeLabelTransformer(new ToStringLabeller<Edge>());
		this.vv.getRenderContext().setVertexShapeTransformer(trans);

		// set custom mouse
		Mouse mouse = new Mouse(this);
		this.vv.setGraphMouse(mouse);

		this.layout.reset();
		final ScalingControl scaler = new CrossoverScalingControl();
		scaler.scale(this.vv, 0.8f, this.vv.getCenter());
		this.vv.scaleToLayout(scaler);
		//vv.getRenderContext().getArrowPlacementTolerance()
		//System.out.println(vv.getRenderContext());

		// left panel
		this.panelLeft = new JPanel();
		this.panelLeft.setBounds(0, 0, 820, 420);
		this.panelLeft.setPreferredSize(new Dimension(820,420));
		this.panelLeft.add(this.vv);
		this.panelLeft.setBorder(new LineBorder(Color.BLACK));

		// right panel
		this.panelRight = new JPanel();
		this.panelRight.setBounds(820, 0, 250, 420);
		this.panelRight.setPreferredSize(new Dimension(250,420));
		this.panelRight.setBorder(new LineBorder(Color.BLUE));
		this.panelRight.setLayout(new GridLayout(20,1));

		this.selectedElement.add(new JLabel("nothing selected"));
		this.updateView();

		this.add(this.panelLeft);
		this.add(this.panelRight);

	}


	//
//	public void setController(ConnectionSchemeController newController) {
//
//		controller = newController;
//	}


	// adds a new vertex to the displayed graph
	public Vertex addVertex(Vertex vertex) {
		Vertex ret = null;
		if((ret = this.contains(vertex)) != null) {
			return ret;
		}
		this.graph.addVertex(vertex);
		this.updateLayout();
		return vertex;
	}


	// adds a new edge to the displayed graph
	public void addEdge(Edge edge, Vertex source, Vertex dest) {
		this.graph.addEdge(edge, source, dest);
		this.updateLayout();
	}


	// gets called after a selection of vertices via mouse-clicking
	public <V> void onVertexSelected(Set<V> vertices) {

		// clear list of labels
		this.selectedElement.clear();

		// loop all selected vertices
    	for(V element:vertices) {
    		this.selectedElement.add(new JLabel("incoming Conduits :"));

    		for(Edge e:this.graph.getInEdges((Vertex)element)) {
    			this.selectedElement.add(new JLabel(" - "+e.toString()));
    		}
    		this.selectedElement.add(new JLabel("selected Kernel : "+element.toString()));
    		this.selectedElement.add(new JLabel("outgoing Conduits :"));

    		for(Edge e:this.graph.getOutEdges((Vertex)element)) {
    			this.selectedElement.add(new JLabel(" - "+e.toString()));
    		}
    	}
    	this.updateView();
	}


	// gets called after a selection of edges via mouse-clicking
	public <E> void onEdgeSelected(Set<E> edges) {

		this.selectedElement.clear();
    	for(E element:edges) {
    		// Startknoten
    		this.selectedElement.add(new JLabel("source Kernel: "+this.graph.getSource((Edge)element)));
    		// Kante
    		this.selectedElement.add(new JLabel("selected Conduit : "+element.toString()));
    		//Endknoten
    		this.selectedElement.add(new JLabel("target Kernel: "+this.graph.getDest((Edge)element)));
    	}
    	this.updateView();
	}


	// called if the user deselects all elements
	public void onNothingSelected() {

// currently disabled because we experience severe performance problems using JUNG remote
// TODO: test if this affects performance at all
//		selectedElement.clear();
//		selectedElement.add(new JLabel("nothing selected"));
//		updateView();
	}


	// updates the graphics and draws all existing labels to the right panel
	private void updateView() {

		this.panelRight.removeAll();
		for(JLabel label : this.selectedElement) {
			this.panelRight.add(label);
		}
		this.panelRight.updateUI();
	}


	// needs to be called to actually draw newwly added vertices
	private void updateLayout() {

		this.layout.reset();
	}


	/**
	checks for an existing vertex within the graph that equals the one given
	@param vertex The vertex that should be checked if it already exists
	@return existing vertex within the graph or null - if it doesn't exists
	*/
	private Vertex contains(Vertex vertex) {

		for(Vertex v:this.graph.getVertices()) {
			if(v.equals(vertex)) {
				return v;
			}
		}
		return null;
	}


	//
	public static void create(ConnectionScheme cs) {

		// show experimantal gui using JUNG
		ConnectionSchemeJUNGPanel view = new ConnectionSchemeJUNGPanel();

		cs.toView(view);

		javax.swing.JFrame f = new javax.swing.JFrame();
		f.getContentPane().add(view);
		f.setVisible(true);
		f.pack();
		f.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
	}


	/**
	only for testing purposes
	*/
	public static void main(String[] args) {

		ConnectionSchemeJUNGPanel gui = new ConnectionSchemeJUNGPanel();
		//Knoten und Kanten
		Vertex[] a = {new Vertex("1", null),new Vertex("2", null),new Vertex("3", null)};
		Edge[] e = {new Edge("1zu2", null),new Edge("1zu2", null),new Edge("2zu1", null),new Edge("2zu3", null)};

		gui.addVertex(a[0]);
		gui.addVertex(a[1]);
		gui.addVertex(a[2]);

		gui.addEdge(e[0], a[0], a[1]);
		gui.addEdge(e[1], a[0], a[1]);
		gui.addEdge(e[2], a[1], a[0]);
		gui.addEdge(e[3], a[0], a[2]);

		JFrame f = new JFrame();
		f.getContentPane().add(gui);
		f.setVisible(true);
		f.pack();
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

}
