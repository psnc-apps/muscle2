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
import org.msgpack.unpacker.Unpacker;

/**
 *
 * @author Joris Borgdorff
 */
public class UnpackerWrapper implements DeserializerWrapper {
	private final Unpacker unpacker;

	public UnpackerWrapper(Unpacker unpack) {
		this.unpacker = unpack;
	}
	
	@Override
	public void refresh() throws IOException {
		// nop
	}
	
	@Override
	public void cleanUp() throws IOException {
		// nop
	}

	@Override
	public int readInt() throws IOException {
		return this.unpacker.readInt();
	}

	@Override
	public boolean readBoolean() throws IOException {
		return this.unpacker.readBoolean();
	}

	@Override
	public byte[] readByteArray() throws IOException {
		return this.unpacker.readByteArray();
	}

	@Override
	public String readString() throws IOException {
		return this.unpacker.readString();
	}

	@Override
	public double readDouble() throws IOException {
		return this.unpacker.readDouble();
	}

	@Override
	public void close() throws IOException {
		this.unpacker.close();
	}
	
	public Unpacker getUnpacker() {
		return this.unpacker;
	}

	@Override
	public Serializable readValue(SerializableDatatype type) throws IOException {
		Serializable value;
		if (type.typeOf().isArray()) {
			int len = unpacker.readArrayBegin();
			switch (type.typeOf()) {
				case STRING_ARR: {
					String[] arr = new String[len];
					value = arr;
					for (int i = 0; i < len; i++) {
						arr[i] = unpacker.readString();
					}}
					break;
				case BOOLEAN_ARR: {
					boolean[] arr = new boolean[len];
					value = arr;
					for (int i = 0; i < len; i++) {
						arr[i] = unpacker.readBoolean();
					}}
					break;
				case SHORT_ARR: {
					short[] arr = new short[len];
					value = arr;
					for (int i = 0; i < len; i++) {
						arr[i] = unpacker.readShort();
					}}
					break;
				case INT_ARR: {
					int[] arr = new int[len];
					value = arr;
					for (int i = 0; i < len; i++) {
						arr[i] = unpacker.readInt();
					}}
					break;
				case LONG_ARR: {
					long[] arr = new long[len];
					value = arr;
					for (int i = 0; i < len; i++) {
						arr[i] = unpacker.readLong();
					}}
					break;
				case FLOAT_ARR: {
					float[] arr = new float[len];
					value = arr;
					for (int i = 0; i < len; i++) {
						arr[i] = unpacker.readFloat();
					}}
					break;
				case DOUBLE_ARR: {
					double[] arr = new double[len];
					value = arr;
					for (int i = 0; i < len; i++) {
						arr[i] = unpacker.readDouble();
					}}
					break;
				default:
					throw new IllegalArgumentException("Can only decode array");
			}
			unpacker.readArrayEnd();
		} else {
			switch (type) {
				case BYTE:
					value = unpacker.readByte();
					break;
				case SHORT:
					value = unpacker.readShort();
					break;
				case LONG:
					value = unpacker.readLong();
					break;
				case FLOAT:
					value = unpacker.readFloat();
					break;
				default:
					throw new IllegalArgumentException("Can only decode byte, short, long, or float");
			}
		}
		return value;
	}
}
