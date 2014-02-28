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

import eu.mapperproject.jmml.util.FastArrayList;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import muscle.id.IDType;
import muscle.id.Identifier;
import muscle.id.PortalID;
import muscle.id.Resolver;

/**
describes the p2p connections between kernels
@author Jan Hegewald
*/
public class ConnectionScheme implements Serializable {
	private static final long serialVersionUID = 1L;
	private final Map<Identifier,Map<String,ConduitDescription>> conduitExits;
	private final Map<Identifier,Map<String,ConduitDescription>> conduitEntrances;
	private static final transient Logger logger = Logger.getLogger(ConnectionScheme.class.getName());
	public List<Identifier> kernelList; // for otf
	public List<String> conduitList; // for otf
	private final Resolver resolver;
	
	public ConnectionScheme(Resolver res, int size) {
		this.resolver = res;
		kernelList = null;
		conduitList = null;
		this.conduitExits = new HashMap<Identifier,Map<String,ConduitDescription>>(size*4/3);
		this.conduitEntrances = new HashMap<Identifier,Map<String,ConduitDescription>>(size*4/3);
		this.init();
	}

	/**
	ConnectionScheme input consists of a String wherein each line describes a possible communication chain (backwards notation: exit<-conduit<-entrance).
	Every line consists of three parts:<br>
	{exit name} {{full class name of conduit}[#ID][(arg0,argn)]} {entrance name}<br>
	where <exit name> is the name-id of the target exit for the conduit,<br>
	<entrance name> is the name-id of the data source (conduit entrance)<br>
	end the middle part describes the conduit. Only the fully qualified class name of the conduit class is mandatory here.
	Conduit names will be generated at runtime from a UUID. To assign a custom name to a conduit an additional ID field preceded by the hash character e.g. #42 has to be added to the class name.
	An optional list of arguments can be passed to the conduit between braces e.g. (arg0,arg1).
	Example ConnectionScheme:<br>
		LBMD2Q9Velocity@a	muscle.core.conduit.LBMD2Q9Velocity2LBMD2Q9Velocity	LBMD2Q9Velocity@b<br>
		TestExit@a muscle.core.conduit.AutomaticConduit#A(muscle.core.conduit.filter.MultiplyFilterDouble_42) TestEntrance@b<br>
		LBMD2Q9Qs@a	muscle.core.conduit.LBMD2Q9Qs2LBMD2Q9Qs	LBMD2Q9Qs@b
	*/
	private void init() {
		// parse input file (this should be done in the CxADescription)
		try {
			File infile = CxADescription.ONLY.getConnectionSchemeFile();
			BufferedReader reader = new BufferedReader(new FileReader(infile));
			String line;
			
			List<String> exitArgs = new FastArrayList<String>(1);
			List<String> entranceArgs = new FastArrayList<String>(5);
			List<String> conduitArgs = new FastArrayList<String>(1);
			
			// Use shared emptyArgs if no arguments are given. Since it is
			// immutable, it will save space if there are a lot of conduits created.
			String[] emptyArgs = {};
			
			while ((line = reader.readLine()) != null) {
				if (line.startsWith("#") || line.isEmpty()) {
					continue;
				}
				int firstSpace = line.indexOf(' ');
				if (firstSpace == -1) {
					logger.severe("connection scheme has unknown format");
					return;
				}
				int secondSpace = line.indexOf(' ', firstSpace + 1);
				if (secondSpace == -1 || line.lastIndexOf(' ') != secondSpace) {
					logger.severe("connection scheme has unknown format");
					return;
				}
								
				String exit = parseItem(line.substring(0, firstSpace), exitArgs);
				String entrance = parseItem(line.substring(secondSpace + 1), entranceArgs);
				parseItem(line.substring(firstSpace + 1, secondSpace), conduitArgs);
				// get conduit class,id,args from item[1] which is e.g.: conduit.foo.bar#42(arg1,arg2)
		
				int entranceSize = entranceArgs.size();
				int conduitSize = conduitArgs.size();
				int exitSize = exitArgs.size();
				
				this.addConnection(
					entrance, 
					entranceSize == 0 ? emptyArgs : entranceArgs.toArray(new String[entranceSize]), 
					conduitSize == 0  ? emptyArgs : conduitArgs.toArray( new String[conduitSize]), 
					exit, 
					exitSize == 0     ? emptyArgs : exitArgs.toArray(    new String[exitSize]));

				// If a exit, entrance, or conduit was created with arguments, be sure to
				// create a new arraylist to start from scratch
				if (exitSize     > 0) {
					exitArgs =     new FastArrayList<String>(1);
				}
				if (entranceSize > 0) {
					entranceArgs = new FastArrayList<String>(1);
				}
				if (conduitSize  > 0) {
					conduitArgs =  new FastArrayList<String>(5);
				}
			}
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private String parseItem(String item, List<String> args) {
		String ret = item;
		int index = item.indexOf('(');
		if (index != -1) {
			if (index == 0) {
				throw new IllegalArgumentException("May not start item " + item + " with argument list");
			}
			if (item.charAt(item.length() - 1) != ')') {
				throw new IllegalArgumentException("Illegal ending of after arguments: " + item);
			}
			int argsIndex = index;
			
			while (index != -1) {
				int commaIndex = item.indexOf(',', index + 1);
				if (commaIndex == -1) {
					args.add(item.substring(index + 1, item.length() - 1));
				} else {
					args.add(item.substring(index + 1, commaIndex));
				}
				index = commaIndex;
			}
			ret = item.substring(0, argsIndex);
		}
		return ret;
	}
	
	/**
	declares a new edge (entrance->conduit->exit) of our graph
	*/
	public void addConnection(String entranceID, String[] entranceArgs, String[] conduitArgs, String exitID, String[] exitArgs) {
		PortalID entrance = (PortalID)resolver.getIdentifier(entranceID, IDType.port);
		PortalID exit = (PortalID)resolver.getIdentifier(exitID, IDType.port);
		
		ConduitDescription conduit = new ConduitDescription(conduitArgs, entrance, entranceArgs, exit, exitArgs);
		
		addPortal(conduit, this.conduitEntrances, entrance);
		addPortal(conduit, this.conduitExits, exit);
	}
	
	private void addPortal(ConduitDescription conduit, Map<Identifier,Map<String,ConduitDescription>> descs, PortalID port) {
		Map<String, ConduitDescription> conduits = descs.get(port.getOwnerID());
		if (conduits == null) {
			conduits = new HashMap<String,ConduitDescription>();
			descs.put(port.getOwnerID(), conduits);
		}
		conduits.put(port.getPortName(), conduit);
	}
	
	public Map<String,ConduitDescription> entranceDescriptionsForIdentifier(Identifier id) {
		return this.conduitEntrances.get(id);
	}
		
	public Map<String,ConduitDescription> exitDescriptionsForIdentifier(Identifier id) {
		return this.conduitExits.get(id);
	}
	
	public ConduitDescription entranceDescriptionForPortal(PortalID id) {
		Map<String, ConduitDescription> map = this.conduitEntrances.get(id.getOwnerID());
		if (map == null) {
			return null;
		} else {
			return map.get(id.getPortName());
		}
	}
	
	public ConduitDescription exitDescriptionForPortal(PortalID id) {
		Map<String, ConduitDescription> map = this.conduitExits.get(id.getOwnerID());
		if (map == null) {
			return null;
		} else {
			return map.get(id.getPortName());
		}
	}

	/** for otf */
	public void generateLists() {
		{
			Set<Identifier> kernelSet = new HashSet<Identifier>(this.conduitExits.size() + this.conduitEntrances.size());
			kernelSet.addAll(this.conduitExits.keySet());
			kernelSet.addAll(this.conduitEntrances.keySet());
			kernelList = new ArrayList<Identifier>(kernelSet);
		}
		
		{
			Set<String> portSet = new HashSet<String>();
			for (Map<String, ConduitDescription> descs : this.conduitEntrances.values()) {
				for (ConduitDescription conduit : descs.values()) {
					portSet.add(conduit.getExit().getName());
					portSet.add(conduit.getEntrance().getName());
				}
			}
			conduitList = new ArrayList<String>(portSet);
		}

		Collections.sort(kernelList);
		Collections.sort(conduitList);
	}
	
	public List<Identifier> getKernels() {
		if (kernelList == null) {
			this.generateLists();
		}
		return kernelList;
	}
}
