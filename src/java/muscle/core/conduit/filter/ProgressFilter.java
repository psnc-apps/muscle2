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

package muscle.core.conduit.filter;

import muscle.core.wrapper.Observation;
import muscle.gui.ProgressMeter;

/**
forwards the data to the next filter without altering the data,
but allows the user to pause the message passing via a GUI
@author Jan Hegewald
*/
public class ProgressFilter<E> extends AbstractWrapperFilter<E,E> {
	private final ProgressMeter progress = new ProgressMeter("ProgressFilter");

	protected void apply(Observation<E> subject) {
		progress.increment();
		put(subject);
	}
}

