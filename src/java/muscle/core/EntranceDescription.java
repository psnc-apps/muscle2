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
stores info necessary to setup an entrance
@author Jan Hegewald
*/
public class EntranceDescription implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private String id;
	private ArrayList<ConduitDescription> targetConduits = new ArrayList<ConduitDescription>();
	private DataTemplate dataTemplate;
	private AID controllerID;
	private EntranceDependency[] dependencies;

	@Override
	public boolean equals(Object b) {

		return b != null && this.getClass().isInstance(b) && this.id.equals(this.getClass().cast(b).getID());
	}

	public EntranceDescription(String newID) {

		this.id = newID;
	}

	public void addConduitDescription(ConduitDescription description) {

		for(int i = 0; i < this.targetConduits.size(); i++) {
			if(this.targetConduits.get(i).getID().equals(description.getID())) {
				throw new MUSCLERuntimeException("Error: can not add conduit <"+description.getID()+"> twice to entrance <"+this.getID()+">");
			}
		}
if(this.targetConduits.size() > 1) {
java.util.logging.Logger l = muscle.logging.Logger.getLogger(this.getClass());
l.warning("Warning: multifeed entrances are not supported yet -- multiple conduits will be spawned instead");
}
		this.targetConduits.add(description);
	}

	public String getID() {

		return this.id;
	}

	public boolean hasConduit(ConduitDescription conduit) {
		return this.targetConduits.contains(conduit);
	}

	public void markAvailable(DataTemplate newDataTemplate, AID newControllerID, EntranceDependency[] newDependencies) {

		this.dataTemplate = newDataTemplate;
		this.controllerID = newControllerID;
		this.dependencies = newDependencies;
	}

	public DataTemplate getDataTemplate() {

		return this.dataTemplate;
	}

	public AID getControllerID() {

		return this.controllerID;
	}

	public EntranceDependency[] getDependencies() {

		return this.dependencies;
	}

	public boolean isAvailable() {

		return this.controllerID != null;
	}

	public void markUnavailable() {

		this.dataTemplate = null;
		this.controllerID = null;
		this.dependencies = null;
	}
}

