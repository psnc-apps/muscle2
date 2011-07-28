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

import muscle.core.ConduitDescription;

/**
description of an edge
@author Jan Hegewald
*/
public class Edge {
	
	private String labelText;
	private ConduitDescription conduit;
	private Vertex source = null;
	private Vertex dest = null; 
	
	
	//
	public Edge(String newLabelText, ConduitDescription newConduit) {

		labelText = newLabelText;
		conduit = newConduit;
	}
	
	public void setSource(Vertex source) {
		this.source = source;
	}

	public void setDest(Vertex dest) {
		this.dest = dest;
	}

	public Vertex getSource() {
		return source;
	}

	public Vertex getDest() {
		return dest;
	} 
	//
	public String toString() {
		return labelText;
	}

	//
	public boolean equals(Object obj) {
		
		if( getClass().isInstance(obj) ) {
			return ((Edge)obj).labelText.equals(labelText);
		}
		return super.equals(obj);
	}

}
