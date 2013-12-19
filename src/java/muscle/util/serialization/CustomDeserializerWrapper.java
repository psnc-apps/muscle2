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
public class CustomDeserializerWrapper implements DeserializerWrapper {
	private final CustomDeserializer in;
	private boolean isClean;

	public CustomDeserializerWrapper(CustomDeserializer in) {
		this.in = in;
		this.isClean = true;
	}
	
	@Override
	public void refresh() throws IOException {
		this.cleanUp();
		in.beginDecoding();
		this.isClean = false;
	}

	@Override
	public void cleanUp() throws IOException {
		if (!isClean) {
			in.endDecoding();
			this.isClean = true;
		}
	}

	@Override
	public int readInt() throws IOException {
		return in.decodeInt();
	}

	@Override
	public boolean readBoolean() throws IOException {
		return in.decodeBoolean();
	}

	@Override
	public byte[] readByteArray() throws IOException {
		return in.decodeByteArray();
	}
	
	@Override
	public String readString() throws IOException {
		return in.decodeString();
	}
	
	@Override
	public double readDouble() throws IOException {
		return in.decodeDouble();
	}
	
	@Override
	public Serializable readValue(SerializableDatatype type) throws IOException {
		if (type.typeOf().isArray()) {
			switch (type.typeOf()) {
				case BYTE_ARR:
					return in.decodeByteArray();
				case STRING_ARR:
					return in.decodeStringArray();
				case BOOLEAN_ARR:
					return in.decodeBooleanArray();
				case SHORT_ARR:
					return in.decodeShortArray();
				case INT_ARR:
					return in.decodeIntArray();
				case LONG_ARR:
					return in.decodeLongArray();
				case FLOAT_ARR:
					return in.decodeFloatArray();
				case DOUBLE_ARR:
					return in.decodeDoubleArray();
				default:
					throw new IllegalArgumentException("Can not parse type " + type);
			}
		} else {
			switch (type) {
				case BYTE:
					return in.decodeByte();
				case SHORT:
					return in.decodeShort();
				case LONG:
					return in.decodeLong();
				case FLOAT:
					return in.decodeFloat();
				default:
					throw new IllegalArgumentException("Can not parse datatype " + type);
			}
		}
	}

	@Override
	public void close() throws IOException {
	}
}
