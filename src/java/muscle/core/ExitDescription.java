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
import java.util.List;

import muscle.exception.MUSCLERuntimeException;

import jade.core.AID;

import muscle.core.DataTemplate;
import java.util.Arrays;


// a leaf in our connection scheme tree
// is now a "node" instead of a leaf because it may have multiple conduits
/**
stores info necessary to setup an exit
@author Jan Hegewald
*/
public class ExitDescription implements Serializable {

	private String id; // name of portal, as in connection scheme
	private List<ConduitDescription> conduits = new ArrayList<ConduitDescription>();
	private DataTemplate dataTemplate;
	private AID controllerID;
	
	public ExitDescription(String newID, ConduitDescription ... newConduits) {
	
		id = newID;
		conduits.addAll(Arrays.asList(newConduits));
	}
	
	public boolean equals(Object b) {
		
		return b != null && getClass().isInstance(b) && id.equals(getClass().cast(b).getID());
	}

	
	public void addConduitDescription(ConduitDescription cd) {
		
		// assure this conduit is not added yet
		for(int i = 0; i < conduits.size(); i++) {
			if(conduits.get(i).getID().equals(cd.getID()))
				throw new MUSCLERuntimeException("Error: can not add conduit <"+cd.getID()+"> twice to exit <"+getID()+">");
		}
		
		conduits.add(cd);
	}
	
	public String getID() {
	
		return id;
	}

	public ConduitDescription getConduitDescription(int index) {
	
		if(index >= conduits.size())
			return null;
		
		return conduits.get(index);
	}
	
	public void markAvailable(DataTemplate newDataTemplate, AID newControllerID) {
	
		dataTemplate = newDataTemplate;
		controllerID = newControllerID;
	}

	public DataTemplate getDataTemplate() {
	
		return dataTemplate;
	}

	public AID getControllerID() {
	
		return controllerID;
	}
	
	public boolean isAvailable() {
	
		return controllerID != null;
	}

	public void markUnavailable() {

		dataTemplate = null;
		controllerID = null;
	}
}

