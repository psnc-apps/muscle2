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
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package muscle.util.serialization;

import java.io.IOException;
import java.io.Serializable;
import muscle.util.data.SerializableDatatype;

/**
 *
 * @author Joris Borgdorff
 */
public interface DeserializerWrapper {
	/** Call refresh before reading any values. Call it once for each time the opposite side calls flush. It implies cleanup. */
	public void refresh() throws IOException;
	/** Cleanup any resources associated to the previous message. */
	public void cleanUp() throws IOException;
	/** Read a single int; may only be called after refresh has been called, one refresh per flush of the sending side. */
	public int readInt() throws IOException;
	/** Read a single boolean may only be called after refresh has been called, one refresh per flush of the sending side. */
	public boolean readBoolean() throws IOException;
	/** Read an array of bytes; may only be called after refresh has been called, one refresh per flush of the sending side. */
	public byte[] readByteArray() throws IOException;
	/** Read a string; may only be called after refresh has been called, one refresh per flush of the sending side. */
	public String readString() throws IOException;
	/** Read a double; may only be called after refresh has been called, one refresh per flush of the sending side. */
	public double readDouble() throws IOException;
	
	public Serializable readValue(SerializableDatatype type) throws IOException;
	/** Close the deserializer, it can not be used again. */
	public void close() throws IOException;
}
