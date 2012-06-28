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

import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.Set;
import muscle.util.data.ArraySet;

/**
singleton class which provides access to global (JVM wide) settings
@author Jan Hegewald
 */
public class JVM implements java.io.Serializable {
	// be careful to init all other static fields we may use here before our singleton
	public final static JVM ONLY = new JVM(); // handle for the singleton
	private File tmpDir;
	private Set<String> libraries;

	public static boolean is64bitJVM() {
		return System.getProperty("sun.arch.data.model").indexOf("64") != -1;
	}

	public JVM() {
		tmpDir = mkTmpDir();
		libraries = new ArraySet<String>();
	}

	/**
	tmp dir for this JVM (e.g. /tmp/<JVMNAME>)
	 */
	public File tmpDir() {
		return tmpDir;
	}
	
	public File tmpFile(String filename) {
		return new File(MiscTool.joinPaths(tmpDir.toString(), filename));
	}

	/**
	name of this JVM
	 */
	public String name() {
		return ManagementFactory.getRuntimeMXBean().getName();
	}

	private static File mkTmpDir() {
		// use name of this JVM as name for tmp dir
		String tmpDirName = OSTool.portableFileName(ManagementFactory.getRuntimeMXBean().getName(), "");
		File td = new File(MiscTool.resolveTilde(MiscTool.joinPaths(System.getProperty("java.io.tmpdir"), tmpDirName)));
		// create our JVM tmp dir if not already there
		td.mkdir();
		if (!td.isDirectory()) {
			throw new RuntimeException("invalid tmp path <" + td + "> for JVM");
		}

		return td;
	}
	
	public synchronized void loadLibrary(String name) {
		if (!libraries.contains(name)) {
			libraries.add(name);
			System.loadLibrary(name);
		}
	}
}
