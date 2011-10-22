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

package muscle.core.kernel;

import java.io.File;
import java.util.ArrayList;

import utilities.MiscTool;
import muscle.core.CxADescription;
import muscle.core.DataTemplate;
import muscle.core.wrapper.DataWrapper;
import muscle.core.Portal;
import muscle.core.ident.PortalID;
import muscle.utilities.RemoteOutputStream;

import com.thoughtworks.xstream.XStream;
import muscle.exception.MUSCLERuntimeException;
import javatool.ArraysTool;
import javax.measure.unit.SI;

/**
used for internal testing<br>
to run this agent, give at least one set of three args:
<portal type> <portal name> <resource path>
where <portal type> == entrance|exit
<portal name> a portal name included in current connection scheme of the plumber
DataTemplate and DataWrapper(s) will be generated from text files in
<resource path>
example for plain JADE:
a:muscle.core.kernel.TestController(exit AssertionExitA ~/path1 entrance FileReaderEntranceA ~/path2)
example for JADE LEAP:
a:muscle.core.kernel.TestController(exit,AssertionExitA,~/path1,entrance,FileReaderEntranceA,~/path2)
@author Jan Hegewald
*/
public class TestController extends CAController {

	// intermix entrances and exits because we need their correct ordering
	private ArrayList<Portal> portals = new ArrayList<Portal>();		

	protected void addPortals() {
		boolean parseError = false;
		Object[] args = getArguments();
		if( args != null && args.length %3 == 0 ) {
			for(int i = 0; i < args.length; i+=6) {
				String portalType = (String)args[i];
				String name = (String)args[i+1];
				String resourceDir = (String)args[i+2];
				resourceDir = MiscTool.resolveTilde(resourceDir);
				
				// if resource path is absolute, simply use it
				// else we assume the resource dir is located in the root dir of a cxa
				if( !(new File(resourceDir)).isAbsolute() ) {
					resourceDir = MiscTool.joinPaths(getCxAPath(), resourceDir);
				}

				if(portalType.equals("entrance")) {
					portals.add(createFileReaderEntrance(new PortalID(name, controller.getID()), resourceDir));
				}
				else if(portalType.equals("exit")) {
					portals.add(createAssertionExit(new PortalID(name, controller.getID()), resourceDir));
				}
				else {
					parseError = true;
					break;
				}
			}
		}
		else
			parseError = true;
		
		if(parseError) {
			XStream xs = new XStream();
			throw new MUSCLERuntimeException("can not read arguments <"+xs.toXML(args)+">");
		}
		
		for(Portal p : portals) {
			if(p instanceof FileReaderEntrance)
				addEntrance((FileReaderEntrance)p);
			else if(p instanceof AssertionExit)
				addExit((AssertionExit)p);
			else
				throw new MUSCLERuntimeException("unknown portal class");
		}
		
	}


	//
	protected void execute() {
		int globalStepCount = CxADescription.ONLY.getIntProperty(CxADescription.Key.MAX_TIMESTEPS);
		getLogger().info("globalStepCount:"+globalStepCount);
		
		for(int globalStep = 0; globalStep < globalStepCount; globalStep++) {
			getLogger().info("  -----  global time:"+globalStep+"  -----");
			for(Portal p : portals) {
				if(p instanceof FileReaderEntrance) {
					if( globalStep % ((FileReaderEntrance)p).getDt() == 0) {
						((FileReaderEntrance)p).action();
					}
				}
				else if(p instanceof AssertionExit) {
					if( globalStep % ((AssertionExit)p).getDt() == 0) {
						((AssertionExit)p).action();
					}
				}
				else
					throw new MUSCLERuntimeException("unknown portal class");
			}
		}

		getLogger().info(getClass().getName()+"\n finished ----------\n\n");
//		controller.doDelete();
	}
	
	
	//
	private FileReaderEntrance createFileReaderEntrance(PortalID portalID, String resourceDir) {

		// create DataTemplate from resources
		String dataTemplateResource = MiscTool.joinPaths(resourceDir, "DataTemplate");
		assert (new File(dataTemplateResource)).canRead() : "can not read "+dataTemplateResource;
		DataTemplate dataTemplate = DataTemplate.createInstanceFromFile(new File(dataTemplateResource));
		String[] dataPaths = null;
		try {
			dataPaths = MiscTool.getAbsolutePathsFromDir(resourceDir, ".*?\\.DataWrapperDouble");
		}
		catch(java.io.IOException e) {
			e.printStackTrace();
		}
		assert dataPaths != null;
		assert dataPaths.length > 0;

		FileReaderEntrance entrance = new FileReaderEntrance(portalID, controller, dataTemplate, null, dataPaths);
		return entrance;
	}


	//
	private AssertionExit createAssertionExit(PortalID portalID, String resourceDir) {

		// create DataTemplate from resources
		String dataTemplateResource = MiscTool.joinPaths(resourceDir, "DataTemplate");
		assert (new File(dataTemplateResource)).canRead() : "can not read "+dataTemplateResource;
		DataTemplate dataTemplate = DataTemplate.createInstanceFromFile(new File(dataTemplateResource));
		
		String[] dataPaths = null;
		try {
			dataPaths = MiscTool.getAbsolutePathsFromDir(resourceDir, ".*?\\.DataWrapperDouble");
		}
		catch(java.io.IOException e) {
			e.printStackTrace();
		}
		assert dataPaths != null;
		assert dataPaths.length > 0;

		AssertionExit exit = new AssertionExit(portalID, controller, dataTemplate, null, dataPaths);
		return exit;
	}


	//
	private class FileReaderEntrance extends muscle.core.ConduitEntrance {

		private int timestep;
		private final int dt; // use seconds internally
		private String[] filePaths;
		private int index;

		//
		public FileReaderEntrance(PortalID newPortalID, InstanceController newOwnerAgent, DataTemplate newDataTemplate, RemoteOutputStream newTraceOutput, String[] newFilePaths) {
			super(newPortalID, newOwnerAgent, 1, newDataTemplate);
			
			dt = (int)(getDataTemplate().getScale().getDt().longValue(SI.SECOND));
			filePaths = newFilePaths;
		}
		
		public int getDt() {
		
			return dt;
		}

		public void action() {

			if(index >= filePaths.length) {
				getLogger().info("nothing to do, local time:"+timestep);
				return; // we are done
			}
			else {
				File file = new File(filePaths[index]);

				getLogger().finest("sending, local time:"+timestep);
				
				DataWrapper data;
				if(getDataTemplate().getDataClass().equals(double[].class)) {
					send(ArraysTool.getFromFile_double(file));//data = new DataWrapper(ArraysTool.getFromFile_double(file), DecimalMeasureTool.multiply(getScale().getDt(), new BigDecimal(timestep)));
				}
				else if(getDataTemplate().getDataClass().equals(boolean[].class)) {
					send(ArraysTool.getFromFile_boolean(file));//data = new DataWrapper(ArraysTool.getFromFile_boolean(file), DecimalMeasureTool.multiply(getScale().getDt(), new BigDecimal(timestep)));
				}
				else {
					throw new MUSCLERuntimeException("unknown datatype <"+getDataTemplate().getDataClass()+">");
				}
			}
		
			index ++;
			timestep += dt;
		}
		
	}


	//
	private class AssertionExit extends muscle.core.ConduitExit {

		private int timestep;
		private final int dt; // use seconds internally
		private String[] filePaths;
		private int index;

		//
		public AssertionExit(PortalID newPortalID, InstanceController newOwner, DataTemplate newDataTemplate, RemoteOutputStream newTraceOutput, String[] newFilePaths) {
			super(newPortalID, newOwner, 1, newDataTemplate);

			dt = (int)(getDataTemplate().getScale().getDt().longValue(SI.SECOND));
			filePaths = newFilePaths;
		}

		public int getDt() {
		
			return dt;
		}

		//
		public void action() {
			
			if(index >= filePaths.length) {
				getLogger().info("nothing to do, local time:"+timestep);
				return; // we are done
			}
			else {
				File file = new File(filePaths[index]);

				getLogger().finest("receiving, local time:"+timestep);
				
				Object actualData = receive();
				if(getDataTemplate().getDataClass().equals(double[].class)) {
				}
				else if(getDataTemplate().getDataClass().equals(boolean[].class)) {
				}
				else {
					throw new MUSCLERuntimeException("unknown datatype <"+getDataTemplate().getDataClass()+">");
				}
								
//				if(index >= filePaths.length)
//					throw new NullPointerException("Error: no data to compare new received data with "+actualData.toString());

				double[] targetData = ArraysTool.getFromFile_double(file);

				ArraysTool.assertEqualArrays(actualData, targetData, 1e-4);
			}
			
			index ++;
			timestep += dt;
		}

	}

}
