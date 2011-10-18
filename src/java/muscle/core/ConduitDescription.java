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
import java.util.ArrayList;

import muscle.exception.MUSCLERuntimeException;

import jade.core.AID;


/**
stores info necessary to setup a conduit
@author Jan Hegewald
*/
public class ConduitDescription implements Serializable {

	private String className;
	private String id;
	String additionalArgs[];
	private EntranceDescription entrance;
	private ArrayList<ExitDescription> targetExits = new ArrayList<ExitDescription>();
	private AID conduitAID;

	public boolean equals(Object b) {
		
		return b != null && getClass().isInstance(b)
			&& id.equals(getClass().cast(b).getID())
			&& className.equals(getClass().cast(b).getClassName());
	}

	//
	public ConduitDescription(String newClassName, String newID, String newAdditionalArgs[], EntranceDescription newEntrance) {
	
		className = newClassName;
		id = newID;
		additionalArgs = newAdditionalArgs;
		entrance = newEntrance;
	}
	
	public void addExitDescription(ExitDescription description) {
	
		for(int i = 0; i < targetExits.size(); i++) {
			if(targetExits.get(i).getID().equals(description.getID()))
				throw new MUSCLERuntimeException("Error: can not add exit <"+description.getID()+"> twice to conduit <"+getID()+">");
		}
if(targetExits.size() > 1) {
java.util.logging.Logger l = muscle.logging.Logger.getLogger(getClass());
l.warning("Warning: multifeed conduits are not supported yet -- multiple conduits will be spawned instead");
}
		targetExits.add(description);
	}
	
	public String getClassName() {
	
		return className;
	}

	public String getID() {
	
		return id;
	}

	public String[] getArgs() {
	
		return additionalArgs;
	}

	public String toString() {
	
		return className+"("+id+")";
	}
	
	public EntranceDescription getEntranceDescription() {

		assert entrance.hasConduit(this);
		return entrance;
	}
	
	public boolean hasExit(ExitDescription exit) {
		return targetExits.contains(exit);
	}
	
	public void markAvailable(AID newConduitAID) {
	
		conduitAID = newConduitAID;
	}

	public boolean isAvailable() {
	
		return conduitAID != null;
	}		
}
