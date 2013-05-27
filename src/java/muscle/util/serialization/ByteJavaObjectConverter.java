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
package muscle.util.serialization;

import java.io.*;

/**
 * Deserialize an object from given byte array
 * @author Joris Borgdorff
 */
public class ByteJavaObjectConverter<T> extends AbstractDataConverter<T, byte[]> {

	/**
	serialize an object
	 */
	public byte[] serialize(T object) {
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		ObjectOutputStream out;
		try {
			out = new ObjectOutputStream(byteStream);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		try {
			// write object to the byteStream
			out.writeObject(object);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (RuntimeException e) {
			throw e;
		} finally {
			try {
				out.close();
			} catch (java.io.IOException e) {
				throw new RuntimeException(e);
			}
			try {
				byteStream.close();
			} catch (java.io.IOException e) {
				throw new RuntimeException(e);
			}
		}

		return byteStream.toByteArray();
	}

	@SuppressWarnings("unchecked")
	public T deserialize(byte[] data) {
		ByteArrayInputStream byteStream = new ByteArrayInputStream(data);

		ObjectInputStream in;
		try {
			in = new ObjectInputStream(byteStream);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		// read an object from the byteStream
		try {
			return (T) in.readObject();
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
}
