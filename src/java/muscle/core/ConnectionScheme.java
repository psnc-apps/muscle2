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

import java.io.File;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.net.URI;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import muscle.Constant;
import muscle.core.ident.*;
import utilities.MiscTool;
import utilities.data.ArrayMap;
import utilities.data.Env;
import utilities.data.FastArrayList;

/**
describes the p2p connections between kernels
@author Jan Hegewald
*/
public class ConnectionScheme implements Serializable {
	private static final long serialVersionUID = 1L;
	private final Map<Identifier,Map<String,ExitDescription>> conduitExits = new ArrayMap<Identifier,Map<String,ExitDescription>>();
	private final Map<Identifier,Map<String,EntranceDescription>> conduitEntrances = new ArrayMap<Identifier,Map<String,EntranceDescription>>();
	private LinkedList<ExitDescription> targetExitDescriptions = new LinkedList<ExitDescription>(); // leafs of a connection chain entrance->conduit->exit
	private static final transient Logger logger = Logger.getLogger(ConnectionScheme.class.getName());
	protected Env env;
	private final ResolverFactory rf;
	public List<String> kernelList; // for otf
	public List<String> conduitList; // for otf
	private static final Boolean instanceLock = Boolean.FALSE;
	
	{
		this.env = CxADescription.ONLY.subenv(this.getClass());
	}

	private transient final static String cs_file_uri = "cs_file_uri";
	
	public static ConnectionScheme getInstance(ResolverFactory rf) {
		synchronized(instanceLock) {
			if (instance == null) {
				instance = new ConnectionScheme(rf);
				instanceLock.notifyAll();
			}
		
			return instance;
		}
	}

	private static ConnectionScheme instance;
	
	public static ConnectionScheme getInstance() {
		synchronized (instanceLock) {
			try {
				while (instance == null) {
					instanceLock.wait();
				}
			} catch (InterruptedException ex) {
				Logger.getLogger(ConnectionScheme.class.getName()).log(Level.SEVERE, null, ex);
			}
			return instance;
		}
	}
	
	private ConnectionScheme(ResolverFactory rf) {
		this.rf = rf;
		kernelList = null;
		conduitList = null;
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
		String connectionSchemeText = null;
		try {
			connectionSchemeText =  MiscTool.fileToString(new File(new URI((String)this.env.get(cs_file_uri))), Constant.Text.COMMENT_INDICATOR);
		}
		catch(java.net.URISyntaxException e) {
			throw new RuntimeException(e);
		}
		catch(java.io.IOException e) {
			throw new RuntimeException(e);
		}
		
		// parse input file (this should be done in the CxADescription)
		String[] lines = connectionSchemeText.split("(\r\n)|(\n)|(\r)");
		try {
			for(String line : lines) {
				String[] items = line.split("( +?)|(\t+?)");
				if(items.length != 3) {
					logger.severe("connection scheme has unknown format");
					return;
				}

				// get conduit class,id,args from item[1] which is e.g.: conduit.foo.bar#42(arg1,arg2)
				String[] parts = items[1].split("[#()]");
				assert parts.length > 0;
				assert parts.length <= 3;
				assert items[1].indexOf('#') != (items[1].indexOf('(')-1) : "Error: empty id, omit # for an empty id";
				assert items[1].indexOf('#') != (items[1].length()-1) : "Error: empty id, omit # for an empty id";
				assert items[1].indexOf('(') != (items[1].indexOf(')')-1) : "Error: empty (), omit the '()' for an empty arg list";

				String conduitClassName = parts[0];
				String conduitID = null;
				List<String> conduitArgs = null;

				if(parts.length > 1) {
					if(items[1].indexOf('#') > -1) {
						conduitID = parts[1];
					}
					if(items[1].indexOf('(') > -1 ) {
						assert items[1].indexOf(')') > -1;
						conduitArgs = new FastArrayList<String>(parts[parts.length-1].split(",")); // last are the args, if any (parts[1] or parts[2])
					}
				}

				if(conduitID == null) {
					conduitID = UUID.randomUUID().toString();
				}
				if(conduitArgs == null) {
					conduitArgs = new FastArrayList<String>(0);
				}
				
				this.addConnection(items[2], conduitClassName, conduitID, conduitArgs, items[0]);
			}
		} catch (InterruptedException ex) {
			Logger.getLogger(ConnectionScheme.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	/**
	declares a new edge (entrance->conduit->exit) of our graph
	*/
	public void addConnection(String entranceID, String conduitClassName, String conduitID, List<String> conduitArgs, String exitID) throws InterruptedException {
		Resolver r = rf.getResolver();
		PortalID pid = (PortalID)r.getIdentifier(entranceID, IDType.port);
		Identifier ownerID = pid.getOwnerID();
		EntranceDescription entrance = new EntranceDescription(pid);
		if (!this.conduitEntrances.containsKey(ownerID)) this.conduitEntrances.put(ownerID, new ArrayMap<String,EntranceDescription>());
		this.conduitEntrances.get(ownerID).put(pid.getPortName(), entrance);

		pid = (PortalID)r.getIdentifier(exitID, IDType.port);
		ownerID = pid.getOwnerID();
		ExitDescription exit = new ExitDescription(pid);
		if (!this.conduitExits.containsKey(ownerID)) this.conduitExits.put(ownerID, new ArrayMap<String,ExitDescription>());
		this.conduitExits.get(ownerID).put(pid.getPortName(), exit);

		ConduitDescription conduit = new ConduitDescription(conduitClassName, conduitID, conduitArgs, entrance, exit);

		exit.setConduitDescription(conduit);
		this.targetExitDescriptions.add(exit);

		entrance.setConduitDescription(conduit);
	}

	public Map<String,EntranceDescription> entranceDescriptionsForIdentifier(Identifier id) {
		return this.conduitEntrances.get(id);
	}
	
	
	public Map<String,ExitDescription> exitDescriptionsForIdentifier(Identifier id) {
		return this.conduitExits.get(id);
	}
	
	public EntranceDescription entranceDescriptionForPortal(PortalID id) {
		Map<String, EntranceDescription> map = this.conduitEntrances.get(id.getOwnerID());
		if (map == null) return null;
		else return map.get(id.getPortName());
	}
	
	public LinkedList<ExitDescription> getConnectionSchemeRoot() {
		return this.targetExitDescriptions;
	}

	@Override
	public String toString() {

		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);

		// print header
		out.printf("\n========== target connection scheme <%s>\n  %-20s <- %-60s -< %-20s\n  ----------\n", ""/*COASTTool.getCXARessource("ConnectionScheme")*/, "<exit name>", "<conduit>#<id>(<ARG1,..,ARGN>)", "<entrance name>");

		for(ExitDescription exit : this.targetExitDescriptions) {
			ConduitDescription conduit = exit.getConduitDescription();
			EntranceDescription entrance = conduit.getEntranceDescription();

			String args = MiscTool.joinItems(java.util.Arrays.asList(conduit.getArgs()), ",");
			if(args == null) {
				args = "";
			}
			out.printf("  %-20s <- %-60s -< %-20s\n", exit.getID(), conduit.getClassName()+"#"+conduit.getID()+"("+args+")", entrance.getID());
		}
		
		out.println("==========");

		return writer.toString();
	}
	
	/** for otf */
	public String getEntrance(String dstSink) {
		for (ExitDescription exit : targetExitDescriptions) {
			ConduitDescription conduit = exit.getConduitDescription();
			EntranceDescription entrance = conduit.getEntranceDescription();

			conduitList.add(exit.getID().getName());
			conduitList.add(entrance.getID().getName());

			if (dstSink.equals(exit.getID().getName()))
				return (entrance.getID().getName());
		}
		return "";
	}

	/** for otf */
	public void generateLists() {
		conduitList = new ArrayList<String>();
		kernelList = new ArrayList<String>();

		for (ExitDescription exit : targetExitDescriptions) {
			ConduitDescription conduit = exit.getConduitDescription();
			EntranceDescription entrance = conduit.getEntranceDescription();
			String exitName = exit.getID().getName();
			
			conduitList.add(exitName);

			try{
			//	System.out.println("Adding entrance" + entrance.getID());
				String entranceName = entrance.getID().getName();
				conduitList.add(entranceName);					
			}
			catch (Exception e)
			{
				//conduitList.add(entrance.getID());
			}


			String temp1 = exitName.substring(exitName.indexOf("@") + 1);
			if (!kernelList.contains(temp1))
				kernelList.add(temp1);

			String temp2 = null;
			try{
				String entranceName = entrance.getID().getName();
				temp2 = entranceName.substring(entranceName.indexOf("@") + 1);
				if (!kernelList.contains(temp2))
				{
					//System.out.println("Adding kernel "+temp2);
					kernelList.add(temp2);
				}
			}
			catch (Exception e)
			{

			}
		}
		Collections.sort(kernelList);
		Collections.sort(conduitList);
	}
	
	public List<String> getKernels() {
		if (kernelList == null) {
			this.generateLists();
		}
		return kernelList;
	}
}
