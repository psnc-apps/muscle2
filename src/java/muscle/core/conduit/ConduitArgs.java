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

import java.io.Serializable;
import jade.core.AID;
import jade.core.Location;
import muscle.core.DataTemplate;

import java.util.List;
import muscle.core.EntranceDescription;
import muscle.core.ExitDescription;
import muscle.core.ident.JadeAgentID;

/**
helps with args for a conduit
@author Jan Hegewald
*/
public class ConduitArgs implements Serializable {
	// mandatory args
	private AID entranceAgent;
	private String entranceName;
	private DataTemplate entranceDataTemplate;

	private JadeAgentID exitAgent;
	private String exitName;
	private DataTemplate exitDataTemplate;
	private Location targetLocation;
	
	// optional args
	private List<String> optionalArgs;

	public ConduitArgs(AID newEntranceAgent, String newEntranceName, DataTemplate newEntranceDataTemplate, JadeAgentID newExitAgent, String newExitName, DataTemplate newExitDataTemplate, Location newTargetLocation, List<String> newOptionalArgs) {
		entranceAgent = newEntranceAgent;
		entranceName = newEntranceName;
		entranceDataTemplate = newEntranceDataTemplate;
		exitAgent = newExitAgent;
		exitName = newExitName;
		exitDataTemplate = newExitDataTemplate;
		targetLocation = newTargetLocation; // set to null if conduit should detect targetLocation automatically
		optionalArgs = newOptionalArgs;
	}

	public ConduitArgs(EntranceDescription entrance, ExitDescription exit, List<String> optionalArgs) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	public AID getEntranceAgent() {
		return entranceAgent;
	}

	public DataTemplate getEntranceDataTemplate() {
		return entranceDataTemplate;
	}

	public String getEntranceName() {
		return entranceName;
	}

	public JadeAgentID getExitAgent() {
		return exitAgent;
	}

	public DataTemplate getExitDataTemplate() {	
		return exitDataTemplate;
	}

	public String getExitName() {
		return exitName;
	}

	public Location getTargetLocation() {
		return targetLocation;
	}

	public List<String> getOptionalArgs() {
		return optionalArgs;
	}
}
