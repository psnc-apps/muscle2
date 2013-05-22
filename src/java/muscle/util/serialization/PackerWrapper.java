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
import java.io.OutputStream;
import java.io.Serializable;
import muscle.util.data.MatrixTool;
import muscle.util.data.SerializableDatatype;
import org.msgpack.packer.Packer;

/**
 *
 * @author Joris Borgdorff
 */
public class PackerWrapper implements SerializerWrapper {
	private final Packer packer;
	private final OutputStream stream;
	private final OutputStream socketStream;

	public PackerWrapper(Packer packer, OutputStream stream, OutputStream socketStream) {
		this.packer = packer;
		this.stream = stream;
		this.socketStream = socketStream;
	}

	@Override
	public void writeInt(int num) throws IOException {
		packer.write(num);
	}

	@Override
	public void writeBoolean(boolean bool) throws IOException {
		packer.write(bool);
	}

	@Override
	public void writeByteArray(byte[] bytes) throws IOException {
		packer.write(bytes);
	}

	@Override
	public void writeString(String str) throws IOException {
		packer.write(str);
	}

	@Override
	public void writeDouble(double d) throws IOException {
		packer.write(d);
	}

	@Override
	public void flush() throws IOException {
		packer.flush();
		stream.flush();
		socketStream.flush();
	}

	@Override
	public void close() throws IOException {
		packer.close();
	}
	
	public Packer getPacker() {
		return this.packer;
	}

	@Override
	public void writeValue(Serializable value, SerializableDatatype type) throws IOException {
		if (type.typeOf().isArray()) {
			int len = MatrixTool.lengthOfMatrix(value, type);
			packer.writeArrayBegin(len);
			switch (type.typeOf()) {
				case STRING_ARR: {
					String[] arr = (String[])value;
					for (int i = 0; i < len; i++) {
						packer.write(arr[i]);
					}
				}	break;
				case BOOLEAN_ARR: {
					boolean[] arr = (boolean[]) value;
					for (int i = 0; i < len; i++) {
						packer.write(arr[i]);
					}
				}	break;
				case SHORT_ARR: {
					short[] arr = (short[])value;
					for (int i = 0; i < len; i++) {
						packer.write(arr[i]);
					}
				}	break;
				case INT_ARR: {
					int[] arr = (int[])value;
					for (int i = 0; i < len; i++) {
						packer.write(arr[i]);
					}
				}	break;
				case LONG_ARR: {
					long[] arr = (long[])value;
					for (int i = 0; i < len; i++) {
						packer.write(arr[i]);
					}
				}	break;
				case FLOAT_ARR: {
					float[] arr = (float[])value;
					for (int i = 0; i < len; i++) {
						packer.write(arr[i]);
					}
				}	break;
				case DOUBLE_ARR: {
					double[] arr = (double[])value;
					for (int i = 0; i < len; i++) {
						packer.write(arr[i]);
					}
				}	break;
			}
			packer.writeArrayEnd();
		} else {
			switch (type) {
				case BYTE:
					packer.write((Byte)value);
					break;
				case SHORT:
					packer.write((Short)value);
					break;
				case FLOAT:
					packer.write((Float)value);
					break;
				case LONG:
					packer.write((Long)value);
					break;
			}
		}
	}
}
