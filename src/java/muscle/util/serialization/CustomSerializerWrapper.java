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
public class CustomSerializerWrapper implements SerializerWrapper {

	private final CustomSerializer out;
	public final static int DEFAULT_BUFFER_SIZE = 66560; // 65*1024
	
	public CustomSerializerWrapper(CustomSerializer out, int buffer_size) {
		this.out = out;
	}

	@Override
	public void writeInt(int num) throws IOException {
		this.out.encode(num);
	}

	@Override
	public void writeBoolean(boolean bool) throws IOException {
		this.out.encode(bool);
	}

	@Override
	public void writeByteArray(byte[] bytes) throws IOException {
		this.out.encode(bytes);
	}

	@Override
	public void flush() throws IOException {
		this.out.flush();
	}

	@Override
	public void writeString(String str) throws IOException {
		this.out.encode(str);
	}

	@Override
	public void writeDouble(double d) throws IOException {
		this.out.encode(d);
	}

	@Override
	public void close() throws IOException {
	}

	@Override
	public void writeValue(Serializable newValue, SerializableDatatype type) throws IOException {
		if (type.typeOf().isArray()) {
			switch (type.typeOf()) {
				case STRING_ARR:
					out.encode((String[]) newValue);
					break;
				case BOOLEAN_ARR:
					out.encode((boolean[]) newValue);
					break;
				case SHORT_ARR:
					out.encode((short[]) newValue);
					break;
				case INT_ARR:
					out.encode((int[]) newValue);
					break;
				case LONG_ARR:
					out.encode((long[]) newValue);
					break;
				case FLOAT_ARR:
					out.encode((float[]) newValue);
					break;
				case DOUBLE_ARR:
					out.encode((double[]) newValue);
					break;
				case BYTE_ARR:
					out.encode((byte[]) newValue);
					break;
				default:
					throw new IllegalArgumentException("Datatype " + type + " not recognized");
			}
		} else {
			switch (type) {
				case BYTE:
					out.encode(((Number)newValue).byteValue());
					break;
				case SHORT:
					out.encode(((Number)newValue).shortValue());
					break;
				case LONG:
					out.encode(((Number)newValue).longValue());
					break;
				case FLOAT:
					out.encode(((Number)newValue).floatValue());
					break;
				default:
					throw new IllegalArgumentException("Datatype " + type + " not recognized");
			}
		}
	}
}
