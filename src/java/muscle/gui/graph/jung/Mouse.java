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


import java.awt.ItemSelectable;

import edu.uci.ics.jung.visualization.control.AbstractModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;


/**
load PickPlugin for our JUNG GraphMouse
@author Christian Schöps
@author Jan Hegewald
*/
class Mouse extends AbstractModalGraphMouse implements ModalGraphMouse, ItemSelectable {

	private ConnectionSchemeJUNGPanel parent;
	
	
	//
	Mouse(ConnectionSchemeJUNGPanel parent) {

		this(1.1f, 1/1.1f, parent);
	}
	
	
	//
	protected Mouse(float in, float out, ConnectionSchemeJUNGPanel parent) {

		super(in, out);
		this.parent = parent;
		loadPlugins();
	}


	//
	protected void loadPlugins() {

		add(new PickPlugin<Integer, String>(parent));
	}

}
