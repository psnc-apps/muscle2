/*
* Copyright 2008, 2009 Complex Automata Simulation Technique (COAST) consortium
* Copyright 2010-2013 Multiscale Applications on European e-Infrastructures (MAPPER) project
*
* GNU Lesser General Public License
* 
* This file is part of MUSCLE (Multiscale Coupling Library and Environment).
* 
* MUSCLE is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
* 
* MUSCLE is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Lesser General Public License for more details.
* 
* You should have received a copy of the GNU Lesser General Public License
* along with MUSCLE.  If not, see <http://www.gnu.org/licenses/>.
*/
package muscle.core.standalone;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
  * A kernel that executes a native instance with MPI.
  * 
  * For this to work correctly, the mpiexec_command parameter should be set.
  */
public class MatlabKernel extends NativeKernel {
	private final static Logger logger = Logger.getLogger(MPIKernel.class.getName());

	/**
	 *  Default serial versionUID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Builds a command for a matlab script using the following recipe:
	 * :matlab_command :matlab_args -nosplash -nodisplay -r $wrapper :args
	 * where $wrapper = "addpath(':script', '$MUSCLE_HOME/share/muscle/matlab/modules/'); try, :script, catch Ex, Error = Ex, ErrorReport = getReport(Ex, 'extended'), ErrorStack = Ex.stack, exit(4), end; exit"
	 * @param command List that the command will be added to
	 * @throws IllegalStateException if the MUSCLE_HOME environment variables is not set
	 * @throws IllegalArgumentException if the "script" property for this kernel is not set
	 * @throws NullPointerException if command is null
	 */
	@Override
	protected void buildCommand(List<String> command) {
		if (hasProperty("matlab_command")) {			
			command.add(getProperty("matlab_command"));
		} else {
			logger.log(Level.INFO, "MATLAB command variable ''matlab_command''  for {0} not given. Using matlab.", getLocalName());
			command.add("matlab");
		}
		
		if (hasProperty("matlab_args")) {
			String args[] = getProperty("matlab_args").trim().split(" ");
			command.addAll(Arrays.asList(args));
		} else {
			logger.log(Level.INFO, "MATLAB arguments variable ''matlab_args'' for {0} not given. Not giving additional arguments.", getLocalName());
		}
		command.add("-nosplash");
		command.add("-nodisplay");

		String muscleHome = System.getenv("MUSCLE_HOME");

		if (muscleHome == null) {
			throw new IllegalStateException("MUSCLE_HOME not set");
		}

		if (hasInstanceProperty("script")) {
			File script = new File(getProperty("script"));
			String scriptDir = script.getParent();
			String mainFunction = script.getName().split("\\.")[0];
			command.add("-r");
			command.add(
					"addpath('" + scriptDir + "','" + muscleHome + "/share/muscle/matlab/modules/'); "
					+ "try, " + mainFunction + ", catch Ex, Error = Ex, "
					+ "ErrorReport = getReport(Ex, 'extended'), "
					+ "ErrorStack = Ex.stack, exit(4), end; exit");
		} else {
			throw new IllegalArgumentException("Missing property: " + getLocalName() + ":script" );
		}
		
		if (hasInstanceProperty("args")) {
			String args[] = getProperty("args").split(" ");
			command.addAll(Arrays.asList(args));
		}
	}

}
