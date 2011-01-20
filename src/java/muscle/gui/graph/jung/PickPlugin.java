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
@author Christian Sch�ps
@author Jan Hegewald
*/
class PickPlugin<V, E> extends PickingGraphMousePlugin<V, E>{

	private ConnectionSchemeJUNGPanel parent;
	private boolean lastPicked = false;


	//
	PickPlugin(ConnectionSchemeJUNGPanel newParent) {

		this.parent = newParent;
	}


	//
	@Override
	public void mousePressed(MouseEvent e) {

		this.down = e.getPoint();

		@SuppressWarnings("unchecked")
		VisualizationViewer<V,E> vv = (VisualizationViewer<V, E>)e.getSource();
		GraphElementAccessor<V,E> pickSupport = vv.getPickSupport();

		// currently selected vertices and edges
		PickedState<V> pickedVertexState = vv.getPickedVertexState();
		PickedState<E> pickedEdgeState = vv.getPickedEdgeState();

		if(pickSupport != null && pickedVertexState != null) {
			Layout<V,E> layout = vv.getGraphLayout();
			if(e.getModifiers() == this.modifiers) {
				this.rect.setFrameFromDiagonal(this.down,this.down);
				// p is the screen point for the mouse event
				Point2D ip = e.getPoint();
				// add vertex to vertex list
				this.vertex = pickSupport.getVertex(layout, ip.getX(), ip.getY());
				if(this.vertex != null) {
					if(pickedVertexState.isPicked(this.vertex) == false) {
						pickedVertexState.clear();
						this.lastPicked = true;
						pickedVertexState.pick(this.vertex, true);
					}
					// layout.getLocation applies the layout transformer so
					// q is transformed by the layout transformer only
					Point2D q = layout.transform(this.vertex);
					// transform the mouse point to graph coordinate system
					Point2D gp = vv.getRenderContext().getMultiLayerTransformer().inverseTransform(Layer.LAYOUT, ip);

					this.offsetx = (float) (gp.getX()-q.getX());
					this.offsety = (float) (gp.getY()-q.getY());

				}
				// add edge to edge list
				else if((this.edge = pickSupport.getEdge(layout, ip.getX(), ip.getY())) != null) {
					pickedEdgeState.clear();
					pickedEdgeState.pick(this.edge, true);
					// Ansonsten ?? und listen clear
				}
				// clear edge list
				else {
					vv.addPostRenderPaintable(this.lensPaintable);
					pickedEdgeState.clear();
					pickedVertexState.clear();
				}

			} else if(e.getModifiers() == this.addToSelectionModifiers) {
				vv.addPostRenderPaintable(this.lensPaintable);
				this.rect.setFrameFromDiagonal(this.down,this.down);
				Point2D ip = e.getPoint();
				this.vertex = pickSupport.getVertex(layout, ip.getX(), ip.getY());
				if(this.vertex != null) {
					this.lastPicked = true;
					boolean wasThere = pickedVertexState.pick(this.vertex, !pickedVertexState.isPicked(this.vertex));
					if(wasThere) {
						this.vertex = null;
					} else {

						// layout.getLocation applies the layout transformer so
						// q is transformed by the layout transformer only
						Point2D q = layout.transform(this.vertex);
						// translate mouse point to graph coord system
						Point2D gp = vv.getRenderContext().getMultiLayerTransformer().inverseTransform(Layer.LAYOUT, ip);

						this.offsetx = (float) (gp.getX()-q.getX());
						this.offsety = (float) (gp.getY()-q.getY());
					}
				} else if((this.edge = pickSupport.getEdge(layout, ip.getX(), ip.getY())) != null) {
					pickedEdgeState.pick(this.edge, !pickedEdgeState.isPicked(this.edge));
				}
			}
		}
		if(this.vertex != null) {
			e.consume();
		}
		// loop on all selected
		if(pickedVertexState.getPicked().size() > 0 && this.lastPicked) {
			this.lastPicked = false;
			pickedEdgeState.clear();
			this.parent.onVertexSelected(pickedVertexState.getPicked());
		} else if(pickedEdgeState.getPicked().size() > 0) {
			pickedVertexState.clear();
			this.parent.onEdgeSelected(pickedEdgeState.getPicked());
		} else {
			pickedEdgeState.clear();
			pickedVertexState.clear();
			this.parent.onNothingSelected();
		}
	}


	// if the mouse is dragging a rectangle, pick the vertices contained in that rectangle
	// clean up settings from mousePressed
	@Override
	public void mouseReleased(MouseEvent e) {
		@SuppressWarnings("unchecked")
		VisualizationViewer<V,E> vv = (VisualizationViewer<V,E>)e.getSource();
		if(e.getModifiers() == this.modifiers) {
			if(this.down != null) {
				Point2D out = e.getPoint();

				if(this.vertex == null && this.heyThatsTooClose(this.down, out, 5) == false) {
					this.pickContainedVertices(vv, this.down, out, true);
				}
			}
		} else if(e.getModifiers() == this.addToSelectionModifiers) {
			if(this.down != null) {
				Point2D out = e.getPoint();

				if(this.vertex == null && this.heyThatsTooClose(this.down,out,5) == false) {
					this.pickContainedVertices(vv, this.down, out, false);
				}
			}
		}
		// Damit kann man auf die ausgew�hlten Vertices und Edges zugreifen
		PickedState<V> pickedVertexState = vv.getPickedVertexState();
		PickedState<E> pickedEdgeState = vv.getPickedEdgeState();
		if(pickedVertexState.getPicked().size() > 0) {
			this.parent.onVertexSelected(pickedVertexState.getPicked());
		} else if(pickedEdgeState.getPicked().size() > 0) {
			this.parent.onEdgeSelected(pickedEdgeState.getPicked());
		}

		this.down = null;
		this.vertex = null;
		this.edge = null;
		this.rect.setFrame(0,0,0,0);
		vv.removePostRenderPaintable(this.lensPaintable);
		vv.repaint();
	}


	// rejects picking if the rectangle is too small, like if the user meant to select one vertex but moved the mouse slightly
	private boolean heyThatsTooClose(Point2D p, Point2D q, double min) {

		return Math.abs(p.getX()-q.getX()) < min && Math.abs(p.getY()-q.getY()) < min;
	}

}
