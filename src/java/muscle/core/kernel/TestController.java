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

import javatool.ArraysTool;

import javax.measure.unit.SI;

import muscle.core.CxADescription;
import muscle.core.DataTemplate;
import muscle.core.Portal;
import muscle.core.PortalID;
import muscle.exception.MUSCLERuntimeException;
import muscle.utilities.RemoteOutputStream;
import utilities.MiscTool;

import com.thoughtworks.xstream.XStream;


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
public class TestController extends muscle.core.kernel.CAController {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	// intermix entrances and exits because we need their correct ordering
	private ArrayList<Portal> portals = new ArrayList<Portal>();


	//
	@Override
	public muscle.core.Scale getScale() {
		javax.measure.DecimalMeasure<javax.measure.quantity.Duration> dt = javax.measure.DecimalMeasure.valueOf(new java.math.BigDecimal(1), javax.measure.unit.SI.SECOND);
		javax.measure.DecimalMeasure<javax.measure.quantity.Length> dx = javax.measure.DecimalMeasure.valueOf(new java.math.BigDecimal(1), javax.measure.unit.SI.METER);
		return new muscle.core.Scale(dt,dx);
	}


	//
	@Override
	protected void addPortals() {

		boolean parseError = false;
		Object[] args = this.getArguments();
		if( args != null && args.length %3 == 0 ) {
			for(int i = 0; i < args.length; i+=6) {
				String portalType = (String)args[i];
				String name = (String)args[i+1];
				String resourceDir = (String)args[i+2];
				resourceDir = MiscTool.resolveTilde(resourceDir);

				// if resource path is absolute, simply use it
				// else we assume the resource dir is located in the root dir of a cxa
				if( !(new File(resourceDir)).isAbsolute() ) {
					resourceDir = MiscTool.joinPaths(CAController.getCxAPath(), resourceDir);
				}

				if(portalType.equals("entrance")) {
					this.portals.add(this.createFileReaderEntrance(new PortalID(name, this.getAID()), resourceDir));
				}
				else if(portalType.equals("exit")) {
					this.portals.add(this.createAssertionExit(new PortalID(name, this.getAID()), resourceDir));
				}
				else {
					parseError = true;
					break;
				}
			}
		} else {
			parseError = true;
		}

		if(parseError) {
			XStream xs = new XStream();
			throw new MUSCLERuntimeException("can not read arguments <"+xs.toXML(args)+">");
		}

		for(Portal p : this.portals) {
			if(p instanceof FileReaderEntrance) {
				this.addEntrance((FileReaderEntrance)p);
			} else if(p instanceof AssertionExit) {
				this.addExit((AssertionExit)p);
			} else {
				throw new MUSCLERuntimeException("unknown portal class");
			}
		}

	}


	//
	@Override
	protected void execute() {

		int globalStepCount = CxADescription.ONLY.getIntProperty(CxADescription.Key.MAX_TIMESTEPS);
		this.getLogger().info("globalStepCount:"+globalStepCount);

		for(int globalStep = 0; globalStep < globalStepCount; globalStep++) {
			this.getLogger().info("  -----  global time:"+globalStep+"  -----");
			for(Portal p : this.portals) {
				if(p instanceof FileReaderEntrance) {
					if( globalStep % ((FileReaderEntrance)p).getDt() == 0) {
						((FileReaderEntrance)p).action();
					}
				}
				else if(p instanceof AssertionExit) {
					if( globalStep % ((AssertionExit)p).getDt() == 0) {
						((AssertionExit)p).action();
					}
				} else {
					throw new MUSCLERuntimeException("unknown portal class");
				}
			}
		}

		this.getLogger().info(this.getClass().getName()+"\n finished ----------\n\n");
		this.doDelete();
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

		RemoteOutputStream traceOutput = new RemoteOutputStream(this, CxADescription.ONLY.getSharedLocation(), portalID.getName()+"--f"+dataTemplate.getScale().getDt()+".txt", 1024);

		FileReaderEntrance entrance = new FileReaderEntrance(portalID, this, dataTemplate, traceOutput, dataPaths);
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

		RemoteOutputStream traceOutput = new RemoteOutputStream(this, CxADescription.ONLY.getSharedLocation(), portalID.getName()+"--f"+dataTemplate.getScale().getDt()+".txt", 1024);

		AssertionExit exit = new AssertionExit(portalID, this, dataTemplate, traceOutput, dataPaths);
		return exit;
	}


	//
	private class FileReaderEntrance extends muscle.core.ConduitEntrance {

		/**
		 *
		 */
		private static final long serialVersionUID = 1L;
		private int timestep;
		private final int dt; // use seconds internally
		private String[] filePaths;
		private int index;

		//
		public FileReaderEntrance(PortalID newPortalID, RawKernel newOwnerAgent, DataTemplate newDataTemplate, RemoteOutputStream newTraceOutput, String[] newFilePaths) {
			super(newPortalID, newOwnerAgent, 1, newDataTemplate);
			this.setTraceOutputStream(newTraceOutput);

			this.dt = (int)(this.getDataTemplate().getScale().getDt().longValue(SI.SECOND));
			this.filePaths = newFilePaths;
		}

		public int getDt() {

			return this.dt;
		}

		public void action() {

			if(this.index >= this.filePaths.length) {
				TestController.this.getLogger().info("nothing to do, local time:"+this.timestep);
				return; // we are done
			}
			else {
				File file = new File(this.filePaths[this.index]);

				TestController.this.getLogger().finest("sending, local time:"+this.timestep);

				if(this.getDataTemplate().getDataClass().equals(double[].class)) {
					this.send(ArraysTool.getFromFile_double(file));//data = new DataWrapper(ArraysTool.getFromFile_double(file), DecimalMeasureTool.multiply(getScale().getDt(), new BigDecimal(timestep)));
				}
				else if(this.getDataTemplate().getDataClass().equals(boolean[].class)) {
					this.send(ArraysTool.getFromFile_boolean(file));//data = new DataWrapper(ArraysTool.getFromFile_boolean(file), DecimalMeasureTool.multiply(getScale().getDt(), new BigDecimal(timestep)));
				}
				else {
					throw new MUSCLERuntimeException("unknown datatype <"+this.getDataTemplate().getDataClass()+">");
				}
			}

			this.index ++;
			this.timestep += this.dt;
		}

	}


	//
	private class AssertionExit extends muscle.core.ConduitExit {

		/**
		 *
		 */
		private static final long serialVersionUID = 1L;
		private int timestep;
		private final int dt; // use seconds internally
		private String[] filePaths;
		private int index;

		//
		public AssertionExit(PortalID newPortalID, RawKernel newOwner, DataTemplate newDataTemplate, RemoteOutputStream newTraceOutput, String[] newFilePaths) {
			super(newPortalID, newOwner, 1, newDataTemplate);
			this.setTraceOutputStream(newTraceOutput);

			this.dt = (int)(this.getDataTemplate().getScale().getDt().longValue(SI.SECOND));
			this.filePaths = newFilePaths;
		}

		public int getDt() {

			return this.dt;
		}

		//
		public void action() {

			if(this.index >= this.filePaths.length) {
				TestController.this.getLogger().info("nothing to do, local time:"+this.timestep);
				return; // we are done
			}
			else {
				File file = new File(this.filePaths[this.index]);

				TestController.this.getLogger().finest("receiving, local time:"+this.timestep);

				Object actualData = this.receive();
				if(this.getDataTemplate().getDataClass().equals(double[].class)) {
				}
				else if(this.getDataTemplate().getDataClass().equals(boolean[].class)) {
				}
				else {
					throw new MUSCLERuntimeException("unknown datatype <"+this.getDataTemplate().getDataClass()+">");
				}

//				if(index >= filePaths.length)
//					throw new NullPointerException("Error: no data to compare new received data with "+actualData.toString());

				double[] targetData = ArraysTool.getFromFile_double(file);

				ArraysTool.assertEqualArrays(actualData, targetData, 1e-4);
			}

			this.index ++;
			this.timestep += this.dt;
		}

	}

}
