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

package muscle.gui.graph;

import java.util.LinkedList;


/**
a graph where one can distinguish source and destination of an edge
@author Jan Hegewald
*/
public class DirectedGraph {

	LinkedList<Edge> edges = new LinkedList<Edge>();
	LinkedList<Vertex> vertices = new LinkedList<Vertex>();
	
	
	//
	public void addEdge(Edge edge, Vertex source, Vertex dest) {
	
		if(!vertices.contains(source) || !vertices.contains(dest))
			return;
		edge.setSource(source);
		edge.setDest(dest);
		edges.add(edge);
	}
	
	
	//
	public void addVertex(Vertex vertex) {

		if(!vertices.contains(vertex))
			vertices.add(vertex);
	}

	
	//
	public LinkedList<Edge> getEdges() {

		return edges;
	}

	
	//
	public LinkedList<Vertex> getVertices() {

		return vertices;
	}

}
