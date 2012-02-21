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
import jade.core.AID;
import java.util.List;

/**
stores info necessary to setup a conduit
@author Jan Hegewald
 */
public class ConduitDescription implements Serializable {
	private String className;
	private String id;
	List<String> additionalArgs;
	private EntranceDescription entrance;
	private ExitDescription exit;
	private AID conduitAID;

	public boolean equals(Object b) {
		return b != null && getClass().equals(b.getClass())
				&& id.equals(((ConduitDescription)b).id);
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 79 * hash + (this.id != null ? this.id.hashCode() : 0);
		return hash;
	}

	public ConduitDescription(String newClassName, String newID, List<String> newAdditionalArgs, EntranceDescription newEntrance, ExitDescription newExit) {
		className = newClassName;
		id = newID;
		additionalArgs = newAdditionalArgs;
		entrance = newEntrance;
		exit = newExit;
	}

	public String getClassName() {
		return className;
	}

	public String getID() {
		return id;
	}

	public List<String> getArgs() {
		return additionalArgs;
	}

	public String toString() {
		return className + "(" + id + ")";
	}

	public EntranceDescription getEntranceDescription() {
		return entrance;
	}

	public ExitDescription getExitDescription() {
		return this.exit;
	}

	public void markAvailable(AID newConduitAID) {
		conduitAID = newConduitAID;
	}

	public boolean isAvailable() {
		return conduitAID != null;
	}
}
