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

	private ConduitExit exit;
	private int dtOffset;

	//
	public EntranceDependency(ConduitExit newExit, int newDtOffset) {

		this.exit = newExit;
		if(this.exit == null) {
			throw new IllegalArgumentException("exit can not be null");
		}
		this.dtOffset = newDtOffset;
		if(this.dtOffset > 0) {
			throw new IllegalArgumentException("entrance can not depend on an exit which will be fed in the future");
		}
	}

	//
	public ConduitExit getExit() {

		return this.exit;
	}

	//
	public int getDtOffset() {

		return this.dtOffset;
	}


	//
	@Override
	public String toString() {

		return "exit <"+this.exit.getLocalName()+"> offset <"+this.dtOffset+">";
	}
}
