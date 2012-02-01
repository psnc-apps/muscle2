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
import java.io.FileInputStream;
import java.io.Serializable;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import utilities.MiscTool;
import muscle.Constant;
import muscle.exception.MUSCLERuntimeException;
import muscle.core.kernel.KernelBootInfo;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import muscle.core.kernel.RawKernel;
import utilities.JVM;
import utilities.data.Env;

/**
singleton which holds information about the current CxA
@author Jan Hegewald
 */
public class CxADescription extends utilities.data.Env implements Serializable {

	private final static transient Logger logger = Logger.getLogger(CxADescription.class.getName());

	public enum Key {

		FINEST_DT("finest_dt"), COARSEST_DT("coarsest_dt"), MAX_TIMESTEPS("max_timesteps"), GUI("gui");
		private String str;

		Key(String str) {
			this.str = str;
		}

		public String toString() {
			return this.str;
		}
	}
	static private final Env DEFAULT_ENV = new Env();
	//

	static {
		// init default values for mandatory flags
		DEFAULT_ENV.put(Key.FINEST_DT.toString(), 1);
		DEFAULT_ENV.put(Key.COARSEST_DT.toString(), 1);
		DEFAULT_ENV.put(Key.MAX_TIMESTEPS.toString(), 1);

		// init default values for optional flags
		DEFAULT_ENV.put(Key.GUI.toString(), false);
	}
	// be careful to init all other static fields we may use here before our singleton
	public final static CxADescription ONLY = new CxADescription(); // handle for the singleton
	private Location sharedLocation;
	private File tmpDir;

	{
		putAll(DEFAULT_ENV);

		// load (mandatory) cxa properties from muscle environment
		Env env = muscle.Env.ONLY.subenv(this.getClass());
		putAll(env);
	}

	// disallow instantiation from everywhere
	protected CxADescription() {
		initEnv();

		// init default shared Location if any
		sharedLocation = new ContainerID((String) get(Constant.Key.TRACE_DATA_TRANSFER, "Main-Container"/*default value*/), null);
	}

	// tmps path where kernels can create individual subdirs
	public String getTmpRootPath() {
		return tmpDir.toString();
	}

	public Location getSharedLocation() {
		return sharedLocation;
	}

	public String getProperty(String key) {
		return get(key).toString();
	}

	public String getPathProperty(String key) {
		return MiscTool.resolveTilde(getEnvAndAssert(key).toString());
	}

	public boolean getBooleanProperty(String key) {
		return (Boolean) getEnvAndAssert(key);
	}

	public int getIntProperty(String key) {
		// json only uses Long, not Integer
		return ((Long) getEnvAndAssert(key)).intValue();
	}

	public double getDoubleProperty(String key) {
		return (Double) getEnvAndAssert(key);
	}

	/**
	returns properties as ASCII
	format is
	key val\n
	 */
	public String getLegacyProperties() {
		StringBuilder text = new StringBuilder();
		for (Iterator e = keySet().iterator(); e.hasNext();) {
			Object key = e.next();
			text.append(key.toString());
			text.append(' ');
			text.append(get(key).toString());
			text.append('\n');
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
		if (tmpDirPath == null) {
			tmpDir = new File(System.getProperty("java.io.tmpdir"));
			logger.log(Level.INFO, "using default tmp directory <{0}>", tmpDir);
		} else {
			tmpDir = new File(MiscTool.resolveTilde(tmpDirPath));
		}
		if (!tmpDir.isDirectory()) {
			File oldTmp = tmpDir;
			tmpDir = new File(System.getProperty("java.io.tmpdir"));
			logger.log(Level.INFO, "omitting invalid tmp directory <{0}>, using default tmp directory <{1}>", new Object[]{oldTmp, tmpDir});
		} else {
			logger.log(Level.INFO, "using tmp directory <{0}>", tmpDir);
		}
	}

	/**
	generates kernel infos from a properties file
	either put
	kernel.example.SomeKernel
	or
	name kernel.example.SomeKernel
	in the file<br>
	commented lines are allowed as described in http://java.sun.com/javase/6/docs/api/java/util/Properties.html#load(java.io.Reader)
	 */
	private static List<KernelBootInfo> kernelBootInfosFromFile(File file) throws java.io.FileNotFoundException {

		// load kernel info properties from file
		Properties props = new Properties();
		try {
			FileInputStream in = new FileInputStream(file);
			props.load(in);
		} catch (java.io.IOException e) {
			throw new MUSCLERuntimeException(e);
		}

		List<KernelBootInfo> kbi = new ArrayList<KernelBootInfo>();
		for (Iterator<String> iter = props.stringPropertyNames().iterator(); iter.hasNext();) {
			String key = iter.next();
			String val = props.getProperty(key);
			// strip any leading and trailing whitespace from class name
			val = val.trim();

			if (val.equals("")) {
				val = key; // only class name given
			}
			Class<? extends RawKernel> cls = null;
			try {
				// do not initialize the kernel class here, else e.g. their static code is already called
				cls = (Class<? extends RawKernel>) Class.forName(val, false, CxADescription.class.getClassLoader());
			} catch (java.lang.ClassNotFoundException e) {
				MUSCLERuntimeException cre = new MUSCLERuntimeException("unknown class <" + cls + ">");
				cre.setStackTrace(e.getStackTrace());
				throw new MUSCLERuntimeException(cre);
			}
			kbi.add(new KernelBootInfo(key, cls));
		}

		return kbi;
	}
}
