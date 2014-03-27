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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import muscle.util.FileTool;
import muscle.util.JVM;
import org.json.simple.JSONValue;

/**
singleton which holds information about the current CxA
@author Jan Hegewald
 */
public class CxADescription {
	private final static Logger logger =  Logger.getLogger(CxADescription.class.getName());
	private final static String CS_KEY = ConnectionScheme.class.getName();
	private final static String CXA_KEY = CxADescription.class.getName();
	private final static String ENV_PROPERTY = "muscle.Env";
	
	private final Map<String,Object> description;
	private final File tmpDir;
	
	public enum Key {
		MAX_TIMESTEPS("max_timesteps");
		private final String str;

		Key(String str) {
			this.str = str;
		}

		@Override
		public String toString() {
			return this.str;
		}
	}
	
	// be careful to init all other static fields we may use here before our singleton
	public final static CxADescription ONLY = new CxADescription(); // handle for the singleton
	
	// disallow instantiation from everywhere
	private CxADescription() {
		tmpDir = JVM.ONLY.tmpDir();
		logger.log(Level.INFO, "Using directory <{0}>", tmpDir);
		
		description = new HashMap<String,Object>();
		
		final String envFilename = System.getProperty(ENV_PROPERTY);
		if(envFilename == null) {
			logger.severe("Property muscle.Env is not specified; cannot start MUSCLE");
			System.exit(1);
		} else {
			try {
				final File envFile = new File(envFilename);
				Reader reader = new BufferedReader(new FileReader(envFile));
				// treat input as a org.json.simple.JSONObject and put in our env
				@SuppressWarnings("unchecked")
				Map<String,Object> jsonHash = (Map<String,Object>)JSONValue.parse(reader);
				@SuppressWarnings("unchecked")
				Map<String,Object> cxaParams = (Map<String,Object>)jsonHash.get(CXA_KEY);
				if (cxaParams == null) {
					logger.severe("CxA parameters are not passed in the configuration.");
					System.exit(1);
				}
				// load (mandatory) cxa properties from muscle environment
				description.putAll(cxaParams);
			} catch (FileNotFoundException e) {
				logger.log(Level.SEVERE, "Cannot load MUSCLE parameters from <" + envFilename + ">: path does not exist", e);
				System.exit(1);
			}
		}
	}

	// tmps path where kernels can create individual subdirs
	public String getTmpRootPath() {
		return tmpDir.toString();
	}

	public String getProperty(Key key) {
		return getProperty(key.toString());
	}

	public String getProperty(String key) {
		return description.get(key).toString();
	}

	public Object getRawProperty(String key) {
		return description.get(key);
	}

	public File getPathProperty(String key) {
		return FileTool.resolveTilde(getEnvAndAssert(key).toString());
	}
	
	// project-accessible
	File getConnectionSchemeFile() {
		return new File(getProperty(CS_KEY));
	}

	public boolean getBooleanProperty(String key) {
		return (Boolean) getEnvAndAssert(key);
	}

	public int getIntProperty(String key) {
		return ((Number) getEnvAndAssert(key)).intValue();
	}

	public double getDoubleProperty(String key) {
		return ((Number)getEnvAndAssert(key)).doubleValue();
	}
	
	public boolean hasProperty(String name) {
		return description.containsKey(name);
	}

	/**
	returns properties as ASCII
	format is
	key val\n
	 */
	public String getLegacyProperties() {
		StringBuilder text = new StringBuilder(35*description.size());
		for (Map.Entry<String,Object> entry : description.entrySet()) {
			text.append(entry.getKey());
			text.append(' ');
			text.append(entry.getValue());
			text.append('\n');
		}

		return text.toString();
	}
	
	/**
	 throw exception if key does not exist
	 * @param key
	 * @return 
	*/
	public Object getEnvAndAssert(String key) {
		if (!description.containsKey(key)) {
			throw new RuntimeException("property <"+key+"> not set");
		}
		
		return description.get(key);
	}
}
