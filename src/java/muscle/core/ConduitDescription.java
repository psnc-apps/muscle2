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

import muscle.id.PortalID;

/**
stores info necessary to setup a conduit
@author Jan Hegewald
 */
public class ConduitDescription {
	private final String[] conduitArgs;
	private final PortalID entrance;
	private final String[] entranceArgs;
	private final PortalID exit;
	private final String[] exitArgs;
	
	public ConduitDescription(String[] newAdditionalArgs, PortalID newEntrance, String[] newEntranceArgs, PortalID newExit, String[] newExitArgs) {
		conduitArgs = newAdditionalArgs;
		entrance = newEntrance;
		entranceArgs = newEntranceArgs;
		exit = newExit;
		exitArgs = newExitArgs;
		if (entrance == null || exit == null) {
			throw new IllegalArgumentException("Entrance and exit may not be null for ConduitDescription");
		}
	}

	public String[] getArgs() {
		return conduitArgs;
	}
	
	public String[] getExitArgs() {
		return exitArgs;
	}

	public PortalID getExit() {
		return exit;
	}
	
	public String[] getEntranceArgs() {
		return entranceArgs;
	}
	
	public PortalID getEntrance() {
		return entrance;
	}
	
	public String toString() {
		return entrance + " -> " + exit;
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
