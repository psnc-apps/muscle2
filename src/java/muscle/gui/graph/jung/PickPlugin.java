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


import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import edu.uci.ics.jung.algorithms.layout.GraphElementAccessor;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.PickingGraphMousePlugin;
import edu.uci.ics.jung.visualization.picking.PickedState;


/**
clear selection when clicked in void, the standard JUNG implementation seems to be buggy
@author Christian Schöps
@author Jan Hegewald
*/
class PickPlugin<V, E> extends PickingGraphMousePlugin<V, E>{
	
	private ConnectionSchemeJUNGPanel parent;
	private boolean lastPicked = false;
	
	
	//
	PickPlugin(ConnectionSchemeJUNGPanel newParent) {
		
		parent = newParent;
	}
	

	//
	public void mousePressed(MouseEvent e) {
		
		down = e.getPoint();
		
		VisualizationViewer<V,E> vv = (VisualizationViewer<V, E>)e.getSource();
		GraphElementAccessor<V,E> pickSupport = vv.getPickSupport();
		
		// currently selected vertices and edges
		PickedState<V> pickedVertexState = vv.getPickedVertexState();
		PickedState<E> pickedEdgeState = vv.getPickedEdgeState();
		
		if(pickSupport != null && pickedVertexState != null) {
			Layout<V,E> layout = vv.getGraphLayout();
			if(e.getModifiers() == modifiers) {
				rect.setFrameFromDiagonal(down,down);
				// p is the screen point for the mouse event
				Point2D ip = e.getPoint();
				// add vertex to vertex list
				vertex = pickSupport.getVertex(layout, ip.getX(), ip.getY());
				if(vertex != null) {
					if(pickedVertexState.isPicked(vertex) == false) {
						pickedVertexState.clear();
						lastPicked = true;
						pickedVertexState.pick(vertex, true);
					}
					// layout.getLocation applies the layout transformer so
					// q is transformed by the layout transformer only
					Point2D q = layout.transform(vertex);
					// transform the mouse point to graph coordinate system
					Point2D gp = vv.getRenderContext().getMultiLayerTransformer().inverseTransform(Layer.LAYOUT, ip);
					
					offsetx = (float) (gp.getX()-q.getX());
					offsety = (float) (gp.getY()-q.getY());
					
				}
				// add edge to edge list
				else if((edge = pickSupport.getEdge(layout, ip.getX(), ip.getY())) != null) {
					pickedEdgeState.clear();
					pickedEdgeState.pick(edge, true);
					// Ansonsten ?? und listen clear
				}
				// clear edge list
				else {
					vv.addPostRenderPaintable(lensPaintable);
					pickedEdgeState.clear();
					pickedVertexState.clear();
				}
				
			} else if(e.getModifiers() == addToSelectionModifiers) {
				vv.addPostRenderPaintable(lensPaintable);
				rect.setFrameFromDiagonal(down,down);
				Point2D ip = e.getPoint();
				vertex = pickSupport.getVertex(layout, ip.getX(), ip.getY());
				if(vertex != null) {
					lastPicked = true;
					boolean wasThere = pickedVertexState.pick(vertex, !pickedVertexState.isPicked(vertex));
					if(wasThere) {
						vertex = null;
					} else {
						
						// layout.getLocation applies the layout transformer so
						// q is transformed by the layout transformer only
						Point2D q = layout.transform(vertex);
						// translate mouse point to graph coord system
						Point2D gp = vv.getRenderContext().getMultiLayerTransformer().inverseTransform(Layer.LAYOUT, ip);
						
						offsetx = (float) (gp.getX()-q.getX());
						offsety = (float) (gp.getY()-q.getY());
					}
				} else if((edge = pickSupport.getEdge(layout, ip.getX(), ip.getY())) != null) {
					pickedEdgeState.pick(edge, !pickedEdgeState.isPicked(edge));
				}
			}
		}
		if(vertex != null) e.consume();
		// loop on all selected
		if(pickedVertexState.getPicked().size() > 0 && lastPicked) {
			lastPicked = false;
			pickedEdgeState.clear();
			parent.onVertexSelected(pickedVertexState.getPicked());
		} else if(pickedEdgeState.getPicked().size() > 0) {
			pickedVertexState.clear();
			parent.onEdgeSelected(pickedEdgeState.getPicked());	
		} else {
			pickedEdgeState.clear();
			pickedVertexState.clear();
			parent.onNothingSelected();
		}
	}
    
	
	// if the mouse is dragging a rectangle, pick the vertices contained in that rectangle
	// clean up settings from mousePressed
	public void mouseReleased(MouseEvent e) {
		VisualizationViewer<V,E> vv = (VisualizationViewer)e.getSource();
		if(e.getModifiers() == modifiers) {
			if(down != null) {
				Point2D out = e.getPoint();
				
				if(vertex == null && heyThatsTooClose(down, out, 5) == false) {
					pickContainedVertices(vv, down, out, true);
				}
			}
		} else if(e.getModifiers() == this.addToSelectionModifiers) {
			if(down != null) {
				Point2D out = e.getPoint();
				
				if(vertex == null && heyThatsTooClose(down,out,5) == false) {
					pickContainedVertices(vv, down, out, false);
				}
			}
		}
		// Damit kann man auf die ausgewählten Vertices und Edges zugreifen
		PickedState<V> pickedVertexState = vv.getPickedVertexState();
		PickedState<E> pickedEdgeState = vv.getPickedEdgeState();
		if(pickedVertexState.getPicked().size() > 0) {
			parent.onVertexSelected(pickedVertexState.getPicked());
		} else if(pickedEdgeState.getPicked().size() > 0) {
			parent.onEdgeSelected(pickedEdgeState.getPicked());	
		}
		
		down = null;
		vertex = null;
		edge = null;
		rect.setFrame(0,0,0,0);
		vv.removePostRenderPaintable(lensPaintable);
		vv.repaint();
	}
	

	// rejects picking if the rectangle is too small, like if the user meant to select one vertex but moved the mouse slightly
	private boolean heyThatsTooClose(Point2D p, Point2D q, double min) {
	
		return Math.abs(p.getX()-q.getX()) < min && Math.abs(p.getY()-q.getY()) < min;
	}

}
