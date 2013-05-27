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

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
  * A kernel that executes a native instance with MPI.
  * 
  * For this to work correctly, the mpiexec_command parameter should be set.
  */
public class MPIKernel extends NativeKernel {
	private final static Logger logger = Logger.getLogger(MPIKernel.class.getName());

	/**
	 *  Default serial versionUID
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected void buildCommand(List<String> command) {
		if (hasProperty("mpiexec_command")) {			
			command.add(getProperty("mpiexec_command"));
		}
		else {
			logger.log(Level.WARNING, "MPI command variable ''mpiexec_command''  for {0} not given. Using mpiexec.", getLocalName());
			command.add("mpiexec");
		}
		
		if (hasProperty("mpiexec_args")) {
			String args[] = getProperty("mpiexec_args").trim().split(" ");
			command.addAll(Arrays.asList(args));
		} else {
			logger.log(Level.WARNING, "MPI arguments variable ''mpiexec_args'' for {0} not given. Not using arguments.", getLocalName());
		}
		
		super.buildCommand(command);
	}

}
