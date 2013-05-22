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
/*
 * 
 */

package muscle.core.conduit.terminal;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;
import muscle.core.model.Observation;
import muscle.util.FileTool;

/**
 * A sink that writes all data to file.
 * 
 * It is governed by the CxA options "file", "relative"
 * @author Joris Borgdorff
 */
public abstract class FileSink<T extends Serializable> extends Sink<T> {
	private int iteration = 0;
	
	/** Provides an infix to the filename to be written to: the iteration number as a String. 
	 * @see Terminal.getLocalFile() for how the filename is determined
	 */
	protected String getInfix() {
		return String.valueOf(iteration);
	}
	
	/**
	 * Writes an observation to file using a given Writer.
	 * Override to write specific datatypes.
	 * @see Terminal.getLocalFile() for how the filename is determined
	 * 
	 * @param out to write to file with. It should not be closed.
	 * @param obs observation to write
	 */
	protected abstract void write(Writer out, Observation<T> obs) throws IOException;
	
	/** Delegates the send operation to the write method. */
	@Override
	public final void send(Observation<T> obs) {
		Writer out = null;
		try {
			File output = getLocalFile(getInfix());
			out = new BufferedWriter(new FileWriter(output));
			write(out, obs);
		} catch (IOException ex) {
			Logger.getLogger(FileSink.class.getName()).log(Level.SEVERE, getLocalName() + " could not write to file", ex);
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException ex) {
					Logger.getLogger(FileSink.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
			iteration++;
		}
	}
	
	protected boolean fileIsRelative() {
		return !hasProperty("relative") || getBooleanProperty("relative");
	}
}
