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

import java.io.Serializable;
import java.util.List;

/**
stores info necessary to setup a conduit
@author Jan Hegewald
 */
public class ConduitDescription implements Serializable {
	private final List<String> additionalArgs;
	private final EntranceDescription entrance;
	private final ExitDescription exit;
	
	public ConduitDescription(List<String> newAdditionalArgs, EntranceDescription newEntrance, ExitDescription newExit) {
		additionalArgs = newAdditionalArgs;
		entrance = newEntrance;
		exit = newExit;
		if (entrance == null || exit == null) {
			throw new IllegalArgumentException("Entrance and exit may not be null for ConduitDescription");
		}
	}

	public List<String> getArgs() {
		return additionalArgs;
	}

	public EntranceDescription getEntranceDescription() {
		return entrance;
	}

	public ExitDescription getExitDescription() {
		return this.exit;
	}
	
	public String toString() {
		return entrance.getID().getName() + " -> " + exit.getID().getName();
	}

	public boolean equals(Object b) {
		return b != null && getClass().equals(b.getClass())
				&& entrance.equals(((ConduitDescription)b).entrance)
				&& exit.equals(((ConduitDescription)b).exit);
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 79 * hash + this.entrance.hashCode();
		hash = 79 * hash + this.exit.hashCode();
		return hash;
	}

}
