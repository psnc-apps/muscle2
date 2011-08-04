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
import javax.swing.border.LineBorder;

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
import muscle.core.ConnectionScheme;
import muscle.gui.graph.ConnectionSchemeViewable;
import muscle.gui.graph.Edge;
import muscle.gui.graph.Vertex;
import javax.swing.WindowConstants;


/**
shows the connection scheme graph using JUNG
@author Christian Schöps
@author Jan Hegewald
*/
public class ConnectionSchemeJUNGPanel extends JPanel implements ConnectionSchemeViewable {
	
	private JPanel panelLeft, panelRight;
	private DirectedSparseMultigraph<Vertex, Edge> graph = new DirectedSparseMultigraph<Vertex, Edge>();
	private Layout<Vertex, Edge> layout;
	private VisualizationViewer<Vertex, Edge> vv;
	private ArrayList<JLabel> selectedElement = new ArrayList<JLabel>();
//	private ConnectionSchemeController controller;
	

	//
	public ConnectionSchemeJUNGPanel() {
		
		//Panellayout
		setLayout(null);
		setPreferredSize(new Dimension(1070,420));
		
		//GraphLayout
		layout = new CircleLayout<Vertex, Edge>(graph);
		layout.setSize(new Dimension(800,400));
		vv = new VisualizationViewer<Vertex,Edge>(layout, new Dimension(800,400));
		vv.getRenderer().setEdgeRenderer(new EdgeRenderer<Vertex,Edge>());

		// init transformer
		final Shape shape = new Rectangle(50,50);
		Transformer<Vertex, Shape> trans = new Transformer<Vertex, Shape>() {
			public Shape transform(Vertex arg0) {
				return shape;
			}
		};
        
		// colours and lables
		vv.getRenderContext().setEdgeDrawPaintTransformer(new PickableEdgePaintTransformer<Edge>(vv.getPickedEdgeState(), Color.black, Color.blue));
		vv.getRenderContext().setVertexFillPaintTransformer(new PickableVertexPaintTransformer<Vertex>(vv.getPickedVertexState(), Color.white, Color.green));
		vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller<Vertex>());
		vv.getRenderContext().setEdgeLabelTransformer(new ToStringLabeller<Edge>());
		vv.getRenderContext().setVertexShapeTransformer(trans);

		// set custom mouse
		Mouse mouse = new Mouse(this);
		vv.setGraphMouse(mouse);
			 
		layout.reset();
		final ScalingControl scaler = new CrossoverScalingControl();
		scaler.scale(vv, 0.8f, vv.getCenter());
		vv.scaleToLayout(scaler);
		//vv.getRenderContext().getArrowPlacementTolerance()
		//System.out.println(vv.getRenderContext());
		
		// left panel
		panelLeft = new JPanel();
		panelLeft.setBounds(0, 0, 820, 420);
		panelLeft.setPreferredSize(new Dimension(820,420));
		panelLeft.add(vv);
		panelLeft.setBorder(new LineBorder(Color.BLACK));
		
		// right panel
		panelRight = new JPanel();
		panelRight.setBounds(820, 0, 250, 420);
		panelRight.setPreferredSize(new Dimension(250,420));
		panelRight.setBorder(new LineBorder(Color.BLUE));
		panelRight.setLayout(new GridLayout(20,1));
		
		selectedElement.add(new JLabel("nothing selected"));
		updateView();
		
		this.add(panelLeft);
		this.add(panelRight);

	}


	//
//	public void setController(ConnectionSchemeController newController) {
//	
//		controller = newController;
//	}

	
	// adds a new vertex to the displayed graph
	public Vertex addVertex(Vertex vertex) {
		Vertex ret = null;
		if((ret = contains(vertex)) != null) return ret;
		graph.addVertex(vertex);
		updateLayout();
		return vertex;
	}
	

	// adds a new edge to the displayed graph
	public void addEdge(Edge edge, Vertex source, Vertex dest) {
		graph.addEdge(edge, source, dest);
		updateLayout();
	}
	
	
	// gets called after a selection of vertices via mouse-clicking
	public <V> void onVertexSelected(Set<V> vertices) {
		
		// clear list of labels
		selectedElement.clear();
		
		// loop all selected vertices
    	for(V element:vertices) {
    		selectedElement.add(new JLabel("incoming Conduits :"));
	
    		for(Edge e:graph.getInEdges((Vertex)element)) {
    			selectedElement.add(new JLabel(" - "+e.toString()));
    		}
    		selectedElement.add(new JLabel("selected Kernel : "+element.toString()));
    		selectedElement.add(new JLabel("outgoing Conduits :"));

    		for(Edge e:graph.getOutEdges((Vertex)element)) {
    			selectedElement.add(new JLabel(" - "+e.toString()));
    		}
    	}
    	updateView();
	}
	

	// gets called after a selection of edges via mouse-clicking
	public <E> void onEdgeSelected(Set<E> edges) {

		selectedElement.clear();
    	for(E element:edges) {
    		// Startknoten
    		selectedElement.add(new JLabel("source Kernel: "+graph.getSource((Edge)element)));
    		// Kante
    		selectedElement.add(new JLabel("selected Conduit : "+element.toString()));
    		//Endknoten
    		selectedElement.add(new JLabel("target Kernel: "+graph.getDest((Edge)element)));
    	}
    	updateView();
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

		panelRight.removeAll();
		for(JLabel label : selectedElement) {
			panelRight.add(label);
		}
		panelRight.updateUI();
	}
	

	// needs to be called to actually draw newwly added vertices
	private void updateLayout() {

		layout.reset();
	}


	/**
	checks for an existing vertex within the graph that equals the one given
	@param vertex The vertex that should be checked if it already exists
	@return existing vertex within the graph or null - if it doesn't exists 
	*/
	private Vertex contains(Vertex vertex) {

		for(Vertex v:graph.getVertices()) {
			if(v.equals(vertex)) return v;
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
