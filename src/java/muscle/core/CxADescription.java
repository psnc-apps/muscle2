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

import jade.core.ContainerID;
import jade.core.Location;

import java.io.File;
import java.util.Iterator;
import java.util.logging.Logger;

import muscle.Constant;
import muscle.exception.MUSCLERuntimeException;
import utilities.Env;
import utilities.JVM;
import utilities.MiscTool;


/**
singleton which holds information about the current CxA
@author Jan Hegewald
*/
public class CxADescription extends utilities.Env implements java.io.Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;


	static public class Key {
		static public final String FINEST_DT = "finest_dt";
		static public final String COARSEST_DT = "coarsest_dt";
		static public final String MAX_TIMESTEPS = "max_timesteps";

		static public final String GUI = "gui";
	}

//	static private final String[] MANDATORY_KEYS = {
////			Constant.Key.CONNECTION_SCHEME,
////			Constant.Key.TRACE_DATA_TRANSFER,
////			Key.CXA_PATH,
//			// provide these in the properties file of a cxa
//			Key.FINEST_DT,
//			Key.COARSEST_DT,
//			Key.MAX_TIMESTEPS,
//	};

	static private final Env DEFAULT_ENV = new Env();
	//
	static {
		// init default values for mandatory flags
		DEFAULT_ENV.put(Key.FINEST_DT, 1);
		DEFAULT_ENV.put(Key.COARSEST_DT, 1);
		DEFAULT_ENV.put(Key.MAX_TIMESTEPS, 1);

		// init default values for optional flags
		DEFAULT_ENV.put(Key.GUI, false);
	}

	// be careful to init all other static fields we may use here before our singleton
	public static CxADescription ONLY = new CxADescription(); // handle for the singleton

   private Location sharedLocation;
	private Logger logger;
	private File tmpDir;

	{
		this.putAll(DEFAULT_ENV);

		// load (mandatory) cxa properties from muscle environment
		Env env = muscle.Env.ONLY.subenv(this.getClass());
		this.putAll(env);
	}

	// disallow instantiation from everywhere
	protected CxADescription() {

		this.logger = muscle.logging.Logger.getLogger(this.getClass());

		this.initEnv();

		// init default shared Location if any
		this.sharedLocation = new ContainerID((String)this.get(Constant.Key.TRACE_DATA_TRANSFER, "Main-Container"/*default value*/), null);
   }


	// tmps path where kernels can create individual subdirs
	public String getTmpRootPath() {

		return this.tmpDir.toString();
	}


	/**
	this is called from the plumber to know with which class to load the communication graph
	*/
	@SuppressWarnings("unchecked")
	public Class<? extends ConnectionScheme> getConnectionSchemeClass() {

		String className = (String)this.getEnvAndAssert(Constant.Key.CONNECTION_SCHEME_CLASS);
		Class<? extends ConnectionScheme> loaderClass = null;
		try {
			loaderClass = (Class<? extends ConnectionScheme>) Class.forName(className);
		}
		catch(java.lang.ClassNotFoundException e) {
			throw new MUSCLERuntimeException(e);
		}

		return loaderClass;
	}


	//
	public Location getSharedLocation() {

		return this.sharedLocation;
	}


	//
	public String getProperty(String key) {

		return this.get(key).toString();
	}


	//
	public String getPathProperty(String key) {

		return MiscTool.resolveTilde(this.getEnvAndAssert(key).toString());
	}


	//
	public boolean getBooleanProperty(String key) {

		return (Boolean)this.getEnvAndAssert(key);
	}


	//
	public int getIntProperty(String key) {

		// json only uses Long, not Integer
		return ((Long)this.getEnvAndAssert(key)).intValue();
	}


	//
	public double getDoubleProperty(String key) {

		return (Double)this.getEnvAndAssert(key);
	}


	/**
	returns properties as ASCII
	format is
	key val\n
	*/
	public String getLegacyProperties() {

		StringBuilder text = new StringBuilder();
		for(Iterator<Object> e = this.keySet().iterator(); e.hasNext();) {
			Object key = e.next();
			text.append(key.toString());
			text.append(" ");
			text.append(this.get(key).toString());
			text.append("\n");
		}

		return text.toString();
	}


	// do not create multiple instances when deserializing multiple times
	private Object readResolve() {
		return ONLY;
	}


	/**
	configure this CxA from the muscle environment
	*/
	private void initEnv() {

		// init tmp dir
		String tmpDirPath = JVM.ONLY.tmpDir().toString();
		if(tmpDirPath == null) {
			this.tmpDir = new File(System.getProperty("java.io.tmpdir"));
			this.logger.info("using default tmp directory <"+this.tmpDir+">");
		}
		else {
			this.tmpDir = new File(MiscTool.resolveTilde(tmpDirPath));
		}
		if(!this.tmpDir.isDirectory()) {
			File oldTmp = this.tmpDir;
			this.tmpDir = new File(System.getProperty("java.io.tmpdir"));
			this.logger.info("omitting invalid tmp directory <"+oldTmp+">, using default tmp directory <"+this.tmpDir+">");
		}
		else {
			this.logger.info("using tmp directory <"+this.tmpDir+">");
		}

//		if( !PropertiesTool.hasKeys(props, MANDATORY_KEYS) )
//			throw new InvalidCxADescription("required keys: <"+MiscTool.joinItems(MANDATORY_KEYS, ", ")+">");
	}

}
