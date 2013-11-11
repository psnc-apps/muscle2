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
package muscle.util;

import eu.mapperproject.jmml.util.ArraySet;
import java.io.File;
import java.util.Set;

/**
singleton class which provides access to global (JVM wide) settings
@author Jan Hegewald
 */
public class JVM {
	// be careful to init all other static fields we may use here before our singleton
	public final static JVM ONLY = new JVM(); // handle for the singleton
	private final File tmpDir;
	private final Set<String> libraries;

	public static boolean is64bitJVM() {
		return System.getProperty("sun.arch.data.model").indexOf("64") != -1;
	}

	public JVM() {
		tmpDir = mkTmpDir();
		libraries = new ArraySet<String>();
	}

	/**
	tmp dir for this JVM (e.g. /tmp/<JVMNAME>)
        * @return The File containing the temporary directory
	 */
	public File tmpDir() {
		return tmpDir;
	}
	
	public String getTmpDirName() {
		return tmpDir.getName();
	}
	
	public File tmpFile(String filename) {
		return FileTool.joinPaths(tmpDir.toString(), filename);
	}

	private static File mkTmpDir() {
		File td = new File(System.getProperty("java.io.tmpdir"));
		// create our JVM tmp dir if not already there
		if (!td.exists()) {
			td.mkdir();
		}
		if (td.isDirectory()) {
			return td;
		} else {
			File newtd = FileTool.resolveTilde(System.getProperty("java.io.tmpdir"));
			if (!newtd.exists()) {
				newtd.mkdir();
			}
			if (newtd.isDirectory()) {
				return newtd;
			} else {
				throw new RuntimeException("invalid tmp paths <" + td + "> or <" + newtd + "> for JVM");
			}
		}
	}
	
	public synchronized void loadLibrary(String name) {
		if (!libraries.contains(name)) {
			libraries.add(name);
			try {
				System.loadLibrary(name);
			} catch (UnsatisfiedLinkError ex) {
				throw new Error("Can not find library " + name + " in java.library.path: <" + System.getProperty("java.library.path") + ">", ex);
			}
		}
	}
}
