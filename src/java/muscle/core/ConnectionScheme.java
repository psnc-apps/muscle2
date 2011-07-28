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

import java.util.ArrayList;
import java.util.Collections;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.net.URI;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import muscle.Constant;
import muscle.exception.MUSCLERuntimeException;
import muscle.gui.graph.ConnectionSchemeViewable;
import muscle.gui.graph.Edge;
import muscle.gui.graph.Vertex;
import muscle.gui.graph.jung.ConnectionSchemeJUNGPanel;
import utilities.Env;
import utilities.MiscTool;

/**
describes the p2p connections between kernels
@author Jan Hegewald
*/
public class ConnectionScheme implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private LinkedList<ExitDescription> targetExitDescriptions = new LinkedList<ExitDescription>(); // leafs of a connection chain entrance->conduit->exit
	private transient Logger logger = muscle.logging.Logger.getLogger(ConnectionScheme.class);
	protected Env env;
	private String cs_file_uri = "cs_file_uri";

	{
//		env = loadEnv();
		this.env = CxADescription.ONLY.subenv(this.getClass());
	}

//	static protected utilities.Env loadEnv() {
//		return CxADescription.ONLY.subenv(this.getClass());
//	}

	public ConnectionScheme() {

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
			connectionSchemeText =  MiscTool.fileToString(new File(new URI((String)this.env.get(this.cs_file_uri))), Constant.Text.COMMENT_INDICATOR);
		}
		catch(java.net.URISyntaxException e) {
			throw new RuntimeException(e);
		}
		catch(java.io.IOException e) {
			throw new RuntimeException(e);
		}

		// parse input file (this should be done in the CxADescription)
		String[] lines = connectionSchemeText.split("(\r\n)|(\n)|(\r)");
		for(String line : lines) {
			String[] items = line.split("( +?)|(\t+?)");
			if(items.length != 3) {
				this.logger.severe("connection scheme has unknown format");
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
			String[] conduitArgs = null;

			if(parts.length > 1) {
				if(items[1].indexOf('#') > -1) {
					conduitID = parts[1];
				}
				if(items[1].indexOf('(') > -1 ) {
					assert items[1].indexOf(')') > -1;
					conduitArgs = parts[parts.length-1].split(","); // last are the args, if any (parts[1] or parts[2])
				}
			}

			if(conduitID == null) {
				conduitID = UUID.randomUUID().toString();
			}
			if(conduitArgs == null) {
				conduitArgs = new String[0];
			}

			this.addConnection(items[2], conduitClassName, conduitID, conduitArgs, items[0]);
		}
	}


	//
	public boolean isComplete() {

		return true;
	}


	/**
	declares a new edge (entrance->conduit->exit) of our graph
	*/
	public Pipeline addConnection(String entranceID, String conduitClassName, String conduitID, String[] conduitArgs, String exitID) {

		// see if this entrance is already added, otherwise create new description for it
		EntranceDescription entrance = this.entranceDescriptionForID(entranceID);
		if( entrance == null ) {
			entrance = new EntranceDescription(entranceID);
		}

		// see if this conduit is already added, otherwise create new description for it
		ConduitDescription conduit = this.conduitDescriptionForNameIDEntrance(conduitClassName, conduitID);
		if( conduit == null ) {
			conduit = new ConduitDescription(conduitClassName, conduitID, conduitArgs, entrance);
		} else {
			if(!conduit.getEntranceDescription().getID().equals(entrance.getID())) {
				throw new MUSCLERuntimeException("Error: can not feed conduit <"+conduit.toString()+"> from different entrances");
			}
		}


		// see if this exit is already there
		ExitDescription exit = this.exitDescriptionForID(exitID);
		if(exit == null) {
			exit = new ExitDescription(exitID, conduit);
			this.targetExitDescriptions.add(exit);
		}
		else {
			exit.addConduitDescription(conduit);
		}

		entrance.addConduitDescription(conduit);

		// see if conduit class is available
		try {
			Class.forName( conduit.getClassName() );
		} catch (ClassNotFoundException e) {
			throw new MUSCLERuntimeException(e);
		}

		conduit.addExitDescription(exit); // conduit has to connect to this exit

		return new Pipeline(entrance, conduit, exit);
	}


	//
	public EntranceDescription entranceDescriptionForID(String entranceID) {

		for (ExitDescription exit : this.targetExitDescriptions) {
			for(int i = 0; ; i++) {
				ConduitDescription conduit = exit.getConduitDescription(i);
				if( conduit == null) {
					break;
				}
				EntranceDescription entranceDescription = conduit.getEntranceDescription();
				// see if the new entrance is in our connection scheme, otherwise we dont know how to connect this entrance
				if(entranceDescription.getID().equals(entranceID)) {
					return entranceDescription; // we located the new entrance in the global connection scheme
				}
			}
		}

		return null;
	}


	//
	public ExitDescription exitDescriptionForID(String exitID) {

		ExitDescription exitDescription = null;
		for(Iterator<ExitDescription> iter = this.targetExitDescriptions.iterator(); iter.hasNext();) {
			exitDescription = iter.next();
			// see if the new exit is in our connection scheme, otherwise we dont know how to connect this exit
			if(exitDescription.getID().equals(exitID)) {
				break; // we located the new exit in the global connection scheme
			}
			exitDescription = null;
		}

		return exitDescription;
	}


	//
	public List<EntranceDescription> entranceDescriptionsForControllerID(AID controllerID) {

		LinkedList<EntranceDescription> entranceDescriptions = new LinkedList<EntranceDescription>();
		for (ExitDescription exit : this.targetExitDescriptions) {
			for(int i = 0; ; i++) {
				ConduitDescription conduit = exit.getConduitDescription(i);
				if( conduit == null) {
					break;
				}
				EntranceDescription description = conduit.getEntranceDescription();

				if(description.isAvailable() && description.getControllerID().equals(controllerID)) {
					entranceDescriptions.add(description);
				}
			}
		}

		return entranceDescriptions;
	}


	//
	public List<ExitDescription> exitDescriptionsForControllerID(AID controllerID) {

		LinkedList<ExitDescription> exitDescriptions = new LinkedList<ExitDescription>();
		for (ExitDescription description : this.targetExitDescriptions) {
			if(description.isAvailable() && description.getControllerID().equals(controllerID)) {
				exitDescriptions.add(description);
			}
		}

		return exitDescriptions;
	}


	//
	private ConduitDescription conduitDescriptionForNameIDEntrance(String className, String conduitID) {

		for (ExitDescription exit : this.targetExitDescriptions) {
			for(int i = 0; ; i++) {
				ConduitDescription conduitDescription = exit.getConduitDescription(i);
				if( conduitDescription == null) {
					break;
				}

				if(conduitDescription.getClassName().equals(className) && conduitDescription.getID().equals(conduitID)) {
					return conduitDescription; // we located the new entrance in the global connection scheme
				}
			}
		}

		return null;
	}


	/**
	search thru our graph for exit descriptions whose kernels are currently not registered
	*/
	public List<ExitDescription> unconnectedExits() {

		LinkedList<ExitDescription> exitDescriptions = new LinkedList<ExitDescription>();
		for (ExitDescription exit : this.targetExitDescriptions) {
			for(int i = 0; ; i++) {
				ConduitDescription conduit = exit.getConduitDescription(i);
				if( conduit == null) {
					break;
				}
				if( !conduit.isAvailable() ) {
					exitDescriptions.add(exit);
				}
			}
		}

		return exitDescriptions;
	}



public LinkedList<ExitDescription> getConnectionSchemeRoot() {

	return this.targetExitDescriptions;
}


	//
	@Override
	public String toString() {

		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);

		// print header
		out.printf("\n========== target connection scheme <%s>\n  %-20s <- %-60s -< %-20s\n  ----------\n", ""/*COASTTool.getCXARessource("ConnectionScheme")*/, "<exit name>", "<conduit>#<id>(<ARG1,..,ARGN>)", "<entrance name>");

		for(ExitDescription exit : this.targetExitDescriptions) {

			for(int i = 0; ; i++) {
				ConduitDescription conduit = exit.getConduitDescription(i);
				if( conduit == null) {
					break;
				}
				EntranceDescription entrance = conduit.getEntranceDescription();
				String args = MiscTool.joinItems(java.util.Arrays.asList(conduit.getArgs()), ",");
				if(args == null) {
					args = "";
				}
				out.printf("  %-20s <- %-60s -< %-20s\n", exit.getID(), conduit.getClassName()+"#"+conduit.getID()+"("+args+")", entrance.getID());
			}
		}

		out.println("==========");

		return writer.toString();
	}


	//
	public String availableConnectionsToString() {

		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);

		// print header
		out.printf("\n========== current active pipelines\n  %-20s <- %-60s <- %-20s\n  ----------\n", "<exit>", "<conduit>(<id>)", "<entrance>");

		for(ExitDescription exit : this.targetExitDescriptions) {
			for(int i = 0; ; i++) {
				ConduitDescription conduit = exit.getConduitDescription(i);
				if( conduit == null) {
					break;
				}
				EntranceDescription entrance = conduit.getEntranceDescription();

				if( exit.isAvailable() && conduit.isAvailable() && entrance.isAvailable() ) {
					out.printf("  %-20s <- %-60s <- %-20s\n", exit.getID(), conduit.getClassName()+"("+conduit.getID()+")", entrance.getID());
				}
			}
		}

		out.println("==========");

		return writer.toString();
	}


	// deserialize
	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {

		// do default deserialization
		in.defaultReadObject();

		// init transient fields
		this.logger = muscle.logging.Logger.getLogger(ConnectionScheme.class);
	}


	//
	public void toView(ConnectionSchemeViewable view) {

		for(ExitDescription exit : this.targetExitDescriptions) {

			String[] parts = exit.getID().split("@");
			if(parts.length != 2) {
				throw new RuntimeException("can not create vertex from <"+exit.getID()+">");
			}
			//Vertex v1 = new Vertex(exit.getControllerID().getName()); // might not be announced yet
			Vertex v1 = new Vertex(parts[1], exit);
			v1 = view.addVertex(v1);

			for(int i = 0; ; i++) {
				ConduitDescription conduit = exit.getConduitDescription(i);
				if( conduit == null) {
					break;
				}

				EntranceDescription entrance = conduit.getEntranceDescription();

				parts = entrance.getID().split("@");
				if(parts.length != 2) {
					throw new RuntimeException("can not create vertex from <"+entrance.getID()+">");
				}
				//Vertex v2 = new Vertex(entrance.getControllerID().getName()); // might not be announced yet
				Vertex v2 = new Vertex(parts[1], entrance);
				v2 = view.addVertex(v2);
				view.addEdge(new Edge(conduit.getID(), conduit), v2, v1);
			}
		}
	}


	//
	public static class Pipeline {

		public EntranceDescription entrance;
		public ConduitDescription conduit;
		public ExitDescription exit;

		public Pipeline(EntranceDescription newEntrance, ConduitDescription newConduit, ExitDescription newExit) {

			this.entrance = newEntrance;
			this.conduit = newConduit;
			this.exit = newExit;
		}
	}
	
	public List kernelList; // for otf
	public List conduitList; // for otf

	// for otf
	public String getEntrance(String dstSink) {
		for (ExitDescription exit : targetExitDescriptions) {
			for (int i = 0;; i++) {
				ConduitDescription conduit = exit.getConduitDescription(i);
				if (conduit == null)
					break;
				EntranceDescription entrance = conduit.getEntranceDescription();

				conduitList.add(exit.getID());
				conduitList.add(entrance.getID());

				if (dstSink.equals(exit.getID()))
					return (entrance.getID());
			}
		}
		return "";
	}

	// for otf
	public void generateLists() {
		conduitList = new ArrayList();
		kernelList = new ArrayList();

		for (ExitDescription exit : targetExitDescriptions) {

			for (int i = 0;; i++) {
				ConduitDescription conduit = exit.getConduitDescription(i);
				if (conduit == null)
					break;
				EntranceDescription entrance = conduit.getEntranceDescription();

				conduitList.add(exit.getID());
				
				try{
				//	System.out.println("Adding entrance" + entrance.getID());
					conduitList.add(entrance.getID());					
				}
				catch (Exception e)
				{
					//conduitList.add(entrance.getID());
				}
				

				String temp1 = exit.getID().substring(exit.getID().indexOf("@") + 1);
				if (!kernelList.contains(temp1))
					kernelList.add(temp1);

				String temp2 = null;
				try{
					temp2 = entrance.getID().substring(entrance.getID().indexOf("@") + 1);
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
		}
		Collections.sort(kernelList);
		Collections.sort(conduitList);
	}


	//
	public static void main(String[] args) {

		ConnectionScheme cs = null;
		// instantiate our connection scheme class
		Class<? extends ConnectionScheme> csClass = CxADescription.ONLY.getConnectionSchemeClass();
		try {
			cs = csClass.newInstance();
		}
		catch(java.lang.InstantiationException e) {
			throw new MUSCLERuntimeException(e);
		}
		catch(java.lang.IllegalAccessException e) {
			throw new MUSCLERuntimeException(e);
		}

		ConnectionSchemeJUNGPanel.create(cs);
	}

}
