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


/**
portal identifier
@author Jan Hegewald
*/
public class PortalID implements Serializable {


	private String name; // unique per agent
	private AID agentID;


	//
	public PortalID(String newName, AID newAgentID) {
	
		name =  newName;
		agentID = newAgentID;
	}
	
	
	//
	public AID getOwnerAID() {
		return agentID;
	}


	//
	public String getName() {
	
		return name+"@"+agentID.getLocalName();
	}
	
//	//
//	public String getStrippedName() {
//	
//		return name;
//	}
	
}
