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

package muscle.core;

/**
information about any exit an entrance may depend on
@author Jan Hegewald
*/
public class EntranceDependency {

	private ConduitExitController exit;
	private int dtOffset;
	
	public EntranceDependency(ConduitExitController newExit, int newDtOffset) {
		exit = newExit;
		if(exit == null)
			throw new IllegalArgumentException("exit can not be null");
		dtOffset = newDtOffset;
		if(dtOffset > 0)
			throw new IllegalArgumentException("entrance can not depend on an exit which will be fed in the future");
	}
	
	public String toString() {
		return "exit <"+exit.getLocalName()+"> offset <"+dtOffset+">";
	}
}
