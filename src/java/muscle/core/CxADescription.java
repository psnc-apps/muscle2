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
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import muscle.util.FileTool;
import muscle.util.JVM;
import muscle.util.data.Env;

/**
singleton which holds information about the current CxA
@author Jan Hegewald
 */
public class CxADescription extends muscle.util.data.Env {
	private final static transient Logger logger = Logger.getLogger(CxADescription.class.getName());
	private static final long serialVersionUID = 1L;

	public enum Key {

		MAX_TIMESTEPS("max_timesteps");
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
		DEFAULT_ENV.put(Key.MAX_TIMESTEPS.toString(), 1);
	}
	// be careful to init all other static fields we may use here before our singleton
	public final static CxADescription ONLY = new CxADescription(); // handle for the singleton
	
	private File tmpDir;
	{
		putAll(DEFAULT_ENV);

		// load (mandatory) cxa properties from muscle environment
		Env env = muscle.Env.ONLY.subenv(this.getClass());
		putAll(env);
	}

	// disallow instantiation from everywhere
	protected CxADescription() {
		tmpDir = JVM.ONLY.tmpDir();
		logger.log(Level.INFO, "Using directory <{0}>", tmpDir);
	}

	// tmps path where kernels can create individual subdirs
	public String getTmpRootPath() {
		return tmpDir.toString();
	}

	public String getProperty(String key) {
		return get(key).toString();
	}

	public Object getRawProperty(String key) {
		return get(key);
	}

	public File getPathProperty(String key) {
		return FileTool.resolveTilde(getEnvAndAssert(key).toString());
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
	
	public boolean hasProperty(String name) {
		return containsKey(name);
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
}
