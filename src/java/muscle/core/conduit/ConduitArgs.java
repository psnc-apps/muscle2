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

package muscle.core.conduit;

import jade.core.AID;
import jade.core.Location;

import java.io.Serializable;
import java.util.ArrayList;

import muscle.core.DataTemplate;


/**
helps with args for a conduit
@author Jan Hegewald
*/
public class ConduitArgs implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	// mandatory args
	private AID entranceAgent;
	private String entranceName;
	private DataTemplate entranceDataTemplate;

	private AID exitAgent;
	private String exitName;
	private DataTemplate exitDataTemplate;
	private Class<? extends ResourceStrategy> strategyClass;
	private Location targetLocation;

	// optional args
	private ArrayList<Object> optionalArgs;


	//
	public ConduitArgs(AID newEntranceAgent, String newEntranceName, DataTemplate newEntranceDataTemplate, AID newExitAgent, String newExitName, DataTemplate newExitDataTemplate, Class<? extends ResourceStrategy> newStrategyClass, Location newTargetLocation, ArrayList<Object> newOptionalArgs) {

		this.entranceAgent = newEntranceAgent;
		this.entranceName = newEntranceName;
		this.entranceDataTemplate = newEntranceDataTemplate;
		this.exitAgent = newExitAgent;
		this.exitName = newExitName;
		this.exitDataTemplate = newExitDataTemplate;
		this.strategyClass = newStrategyClass;
		this.targetLocation = newTargetLocation; // set to null if conduit should detect targetLocation automatically
		this.optionalArgs = newOptionalArgs;
	}


	//
	public AID getEntranceAgent() {

		return this.entranceAgent;
	}


	//
	public DataTemplate getEntranceDataTemplate() {

		return this.entranceDataTemplate;
	}


	//
	public String getEntranceName() {

		return this.entranceName;
	}


	//
	public AID getExitAgent() {

		return this.exitAgent;
	}


	//
	public DataTemplate getExitDataTemplate() {

		return this.exitDataTemplate;
	}


	//
	public String getExitName() {

		return this.exitName;
	}


	//
	public Class<? extends ResourceStrategy> getStrategyClass() {

		return this.strategyClass;
	}


	//
	public Location getTargetLocation() {

		return this.targetLocation;
	}


	//
	public ArrayList<Object> getOptionalArgs() {

		return this.optionalArgs;
	}

}
