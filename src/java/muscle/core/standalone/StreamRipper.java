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

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StreamRipper extends Thread {
	private BufferedReader br;
	private PrintStream ps;
	private final static Logger logger = Logger.getLogger(StreamRipper.class.getName());
	
	public StreamRipper(String name, PrintStream ps, InputStream is) {
		super(name);
		this.ps = ps;
		br = new BufferedReader(new InputStreamReader(is));
	}
	
	public void run() {
		String line;
		
		try {
			while ((line = br.readLine()) != null) {
				ps.println(line);
			}
		} catch (IOException ex) {
			logger.log(Level.WARNING, this.getName() + " no longer capturing output of process", ex);
		}
	}
}
