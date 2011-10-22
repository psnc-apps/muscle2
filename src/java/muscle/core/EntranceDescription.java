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

import java.util.logging.Logger;
import muscle.core.DataTemplate;
import muscle.core.EntranceDependency;

/**
stores info necessary to setup an entrance
@author Jan Hegewald
 */
public class EntranceDescription implements Serializable {

	private String id;
	private ArrayList<ConduitDescription> targetConduits = new ArrayList<ConduitDescription>();
	private DataTemplate dataTemplate;
	private AID controllerID;
	private EntranceDependency[] dependencies;

	public boolean equals(Object b) {
		return b != null && getClass().isInstance(b) && id.equals(getClass().cast(b).getID());
	}

	public EntranceDescription(String newID) {

		id = newID;
	}

	public void addConduitDescription(ConduitDescription description) {

		for (int i = 0; i < targetConduits.size(); i++) {
			if (targetConduits.get(i).getID().equals(description.getID())) {
				throw new MUSCLERuntimeException("Error: can not add conduit <" + description.getID() + "> twice to entrance <" + getID() + ">");
			}
		}
		if (targetConduits.size() > 1) {
			Logger.getLogger(EntranceDescription.class.getName()).warning("Warning: multifeed entrances are not supported yet -- multiple conduits will be spawned instead");
		}
		targetConduits.add(description);
	}

	public String getID() {

		return id;
	}

	public boolean hasConduit(ConduitDescription conduit) {
		return targetConduits.contains(conduit);
	}

	public void markAvailable(DataTemplate newDataTemplate, AID newControllerID, EntranceDependency[] newDependencies) {

		dataTemplate = newDataTemplate;
		controllerID = newControllerID;
		dependencies = newDependencies;
	}

	public DataTemplate getDataTemplate() {

		return dataTemplate;
	}

	public AID getControllerID() {

		return controllerID;
	}

	public EntranceDependency[] getDependencies() {

		return dependencies;
	}

	public boolean isAvailable() {

		return controllerID != null;
	}

	public void markUnavailable() {

		dataTemplate = null;
		controllerID = null;
		dependencies = null;
	}
}
