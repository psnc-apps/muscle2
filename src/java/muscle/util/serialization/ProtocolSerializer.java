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
package muscle.util.serialization;

import java.io.IOException;

/**
 * Manage the serialization and deserialization of a protocol
 * @author joris
 */
public class ProtocolSerializer<T extends Enum<T>&Protocol> {
	private final T[] values;
	private final int[] nums;
	
	public ProtocolSerializer(T[] enumBase) {
		values = enumBase;
		nums = new int[values.length];

		for (int i = 0; i < values.length; i++) {
			nums[i] = values[i].intValue();
		}
	}
	
	public T read(DeserializerWrapper in) throws IOException {
		return valueOf(in.readInt());
	}
	public void write(SerializerWrapper out, T value) throws IOException {
		out.writeInt(value.intValue());
	}
	
	public T valueOf(int proto) {
		for (int i = 0; i < nums.length; i++) {
			if (nums[i] == proto) {
				return values[i];
			}
		}
		throw new IllegalArgumentException("Protocol " + proto + " not registered");
	}
}
