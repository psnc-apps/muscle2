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
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.net.URI;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import muscle.id.IDType;
import muscle.id.Identifier;
import muscle.id.PortalID;
import muscle.id.Resolver;
import muscle.id.ResolverFactory;
import muscle.util.data.Env;

/**
describes the p2p connections between kernels
@author Jan Hegewald
*/
public class ConnectionScheme implements Serializable {
	private static final long serialVersionUID = 1L;
	private final Map<Identifier,Map<String,ExitDescription>> conduitExits;
	private final Map<Identifier,Map<String,EntranceDescription>> conduitEntrances;
	private LinkedList<ExitDescription> targetExitDescriptions = new LinkedList<ExitDescription>(); // leafs of a connection chain entrance->conduit->exit
	private static final transient Logger logger = Logger.getLogger(ConnectionScheme.class.getName());
	protected Env env;
	private final ResolverFactory rf;
	public List<String> kernelList; // for otf
	public List<String> conduitList; // for otf
	private static final Object instanceLock = new Object();
	private final static Pattern spacePattern = Pattern.compile("( +?)|(\t+?)");
	private final static Pattern parensPattern = Pattern.compile("[()]");
	private final static Pattern commaPattern = Pattern.compile(",");
	
	{
		this.env = CxADescription.ONLY.subenv(this.getClass());
	}

	private transient final static String cs_file_uri = "cs_file_uri";
	
	public static ConnectionScheme getInstance(ResolverFactory rf, int size) {
		synchronized(instanceLock) {
			if (instance == null) {
				instance = new ConnectionScheme(rf, size);
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
	
	private ConnectionScheme(ResolverFactory rf, int size) {
		this.rf = rf;
		kernelList = null;
		conduitList = null;
		this.conduitExits = new HashMap<Identifier,Map<String,ExitDescription>>(size*4/3);
		this.conduitEntrances = new HashMap<Identifier,Map<String,EntranceDescription>>(size*4/3);
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
			File infile = new File(new URI((String)this.env.get(cs_file_uri)));
			BufferedReader reader = new BufferedReader(new FileReader(infile));
			String line;		
			while ((line = reader.readLine()) != null) {
				if (line.startsWith("#")) {
					continue;
				}
				String[] items = spacePattern.split(line);
				if(items.length != 3) {
					logger.severe("connection scheme has unknown format");
					return;
				}
				List<String> exitArgs = new FastArrayList<String>(0);
				List<String> conduitArgs = new FastArrayList<String>(0);
				List<String> entranceArgs = new FastArrayList<String>(0);

				String exit = parseItem(items[0], exitArgs);
				String entrance = parseItem(items[2], entranceArgs);
				parseItem(items[1], conduitArgs);
				// get conduit class,id,args from item[1] which is e.g.: conduit.foo.bar#42(arg1,arg2)
				
				
				this.addConnection(entrance, entranceArgs, conduitArgs, exit, exitArgs);
			}
		} catch (InterruptedException ex) {
			throw new RuntimeException(ex);
		} catch(IOException e) {
			throw new RuntimeException(e);
		} catch(java.net.URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}
	
	private String parseItem(String item, List<String> args) {
//		assert item.indexOf('(') != 0 : "Error: can not start parenthesis at first character";
//		assert item.indexOf('(') != (item.indexOf(')')-1) : "Error: empty (), omit the '()' for an empty arg list";
		String[] parts = parensPattern.split(item);
//		assert parts.length > 0;
//		assert parts.length <= 2;
		
		if(parts.length > 1) {
			String[] sArgs = commaPattern.split(parts[1]);
			args.addAll(Arrays.asList(sArgs));
		}
		return parts[0];
	}
	
	/**
	declares a new edge (entrance->conduit->exit) of our graph
	*/
	public void addConnection(String entranceID, List<String> entranceArgs, List<String> conduitArgs, String exitID, List<String> exitArgs) throws InterruptedException {
		Resolver r = rf.getResolver();
		EntranceDescription entrance = addEntrance((PortalID)r.getIdentifier(entranceID, IDType.port), entranceArgs);
		ExitDescription exit = addExit((PortalID)r.getIdentifier(exitID, IDType.port), exitArgs);
		ConduitDescription conduit = new ConduitDescription(conduitArgs, entrance, exit);

		exit.setConduitDescription(conduit);
		entrance.setConduitDescription(conduit);

		this.targetExitDescriptions.add(exit);
	}

	
	private EntranceDescription addEntrance(PortalID pid, List<String> args) {
		Identifier ownerID = pid.getOwnerID();
		EntranceDescription entrance = new EntranceDescription(pid, args);
		Map<String,EntranceDescription> entrances = this.conduitEntrances.get(ownerID);
		if (entrances == null) {
			entrances = new HashMap<String,EntranceDescription>();
			this.conduitEntrances.put(ownerID, entrances);
		}
		entrances.put(pid.getPortName(), entrance);
		return entrance;
	}
	
	
	private ExitDescription addExit(PortalID pid, List<String> args) {
		Identifier ownerID = pid.getOwnerID();
		ExitDescription exit = new ExitDescription(pid, args);
		Map<String,ExitDescription> exits = this.conduitExits.get(ownerID);
		if (exits == null) {
			exits = new HashMap<String,ExitDescription>();
			this.conduitExits.put(ownerID, exits);
		}
		exits.put(pid.getPortName(), exit);
		return exit;
	}
	
	public Map<String,EntranceDescription> entranceDescriptionsForIdentifier(Identifier id) {
		return this.conduitEntrances.get(id);
	}
	
	
	public Map<String,ExitDescription> exitDescriptionsForIdentifier(Identifier id) {
		return this.conduitExits.get(id);
	}
	
	public EntranceDescription entranceDescriptionForPortal(PortalID id) {
		Map<String, EntranceDescription> map = this.conduitEntrances.get(id.getOwnerID());
		if (map == null) {
			return null;
		} else {
			return map.get(id.getPortName());
		}
	}
	
	public ExitDescription exitDescriptionForPortal(PortalID id) {
		Map<String, ExitDescription> map = this.conduitExits.get(id.getOwnerID());
		if (map == null) {
			return null;
		} else {
			return map.get(id.getPortName());
		}
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

			List<String> args = new FastArrayList<String>(conduit.getArgs().toArray(new String[0]));
			out.printf("  %-20s -- %-20s -> %-20s\n", exit.getID(), args, entrance.getID());
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

			if (dstSink.equals(exit.getID().getName())) {
				return (entrance.getID().getName());
			}
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
			if (!kernelList.contains(temp1)) {
				kernelList.add(temp1);
			}

			String temp2;
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
