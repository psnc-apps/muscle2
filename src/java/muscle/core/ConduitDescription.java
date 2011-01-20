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

import jade.core.AID;

import java.io.Serializable;
import java.util.ArrayList;

import muscle.exception.MUSCLERuntimeException;


/**
stores info necessary to setup a conduit
@author Jan Hegewald
*/
public class ConduitDescription implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private String className;
	private String id;
	String additionalArgs[];
	private EntranceDescription entrance;
	private ArrayList<ExitDescription> targetExits = new ArrayList<ExitDescription>();
	private AID conduitAID;

	@Override
	public boolean equals(Object b) {

		return b != null && this.getClass().isInstance(b)
			&& this.id.equals(this.getClass().cast(b).getID())
			&& this.className.equals(this.getClass().cast(b).getClassName());
	}

	//
	public ConduitDescription(String newClassName, String newID, String newAdditionalArgs[], EntranceDescription newEntrance) {

		this.className = newClassName;
		this.id = newID;
		this.additionalArgs = newAdditionalArgs;
		this.entrance = newEntrance;
	}

	public void addExitDescription(ExitDescription description) {

		for(int i = 0; i < this.targetExits.size(); i++) {
			if(this.targetExits.get(i).getID().equals(description.getID())) {
				throw new MUSCLERuntimeException("Error: can not add exit <"+description.getID()+"> twice to conduit <"+this.getID()+">");
			}
		}
if(this.targetExits.size() > 1) {
java.util.logging.Logger l = muscle.logging.Logger.getLogger(this.getClass());
l.warning("Warning: multifeed conduits are not supported yet -- multiple conduits will be spawned instead");
}
		this.targetExits.add(description);
	}

	public String getClassName() {

		return this.className;
	}

	public String getID() {

		return this.id;
	}

	public String[] getArgs() {

		return this.additionalArgs;
	}

	@Override
	public String toString() {

		return this.className+"("+this.id+")";
	}

	public EntranceDescription getEntranceDescription() {

		assert this.entrance.hasConduit(this);
		return this.entrance;
	}

	public boolean hasExit(ExitDescription exit) {
		return this.targetExits.contains(exit);
	}

	public void markAvailable(AID newConduitAID) {

		this.conduitAID = newConduitAID;
	}

	public boolean isAvailable() {

		return this.conduitAID != null;
	}
}
