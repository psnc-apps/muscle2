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

package muscle.core.kernel;

import muscle.core.Scale;
import muscle.core.model.Distance;

/**
kernel which does nothing, usefulf for testing purposes e.g. to use the QuitMonitor with a non empty CxA
@author Jan Hegewald
*/
public class VoidKernel extends RawInstance {
	protected void addPortals() {
		// add no portals
	}

	public void execute() {
		// do nothing
	}

	public Scale getScale() {
		Distance delta = new Distance(1);
		return new Scale(delta,delta);
	}
}
