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
import muscle.util.data.MatrixTool;
import muscle.util.data.SerializableDatatype;

/**
 *
 * @author Joris Borgdorff
 */
public class XdrSerializerWrapper implements SerializerWrapper {

	private final XdrEncodingStream xdrOut;
	public final static int DEFAULT_BUFFER_SIZE = 66560; // 65*1024
	private final static float CHUNK_SIZE_MODIFIER = .95f;
	private final float max_chunk_size;

	public XdrSerializerWrapper(XdrEncodingStream xdrOut, int buffer_size) {
		this.xdrOut = xdrOut;
		this.max_chunk_size = CHUNK_SIZE_MODIFIER*buffer_size;
		// Get ready to write
		this.xdrOut.beginEncoding();
	}

	@Override
	public void writeInt(int num) throws IOException {
		this.xdrOut.xdrEncodeInt(num);
	}

	@Override
	public void writeBoolean(boolean bool) throws IOException {
		this.xdrOut.xdrEncodeBoolean(bool);
	}

	@Override
	public void writeByteArray(byte[] bytes) throws IOException {
		this.writeValue(bytes, SerializableDatatype.BYTE_ARR);
	}

	@Override
	public void flush() throws IOException {
		this.xdrOut.endEncoding();
		// get ready for the next round!
		this.xdrOut.beginEncoding();
	}

	@Override
	public void writeString(String str) throws IOException {
		this.xdrOut.xdrEncodeString(str);
	}

	@Override
	public void writeDouble(double d) throws IOException {
		this.xdrOut.xdrEncodeDouble(d);
	}

	@Override
	public void close() throws IOException {
		this.xdrOut.close();
	}

	public XdrEncodingStream getXdrEncodingStream() {
		return this.xdrOut;
	}

	@Override
	public void writeValue(Serializable newValue, SerializableDatatype type) throws IOException {
		if (type.typeOf().isArray()) {
			int len = MatrixTool.lengthOfMatrix(newValue, type);

			// Take into account that char* is transmitted worse than expected (4 byte per char) in XDR
			long size = MatrixTool.deepSizeOf(newValue, type);
			int chunks = (int) Math.ceil(size / max_chunk_size);
			xdrOut.xdrEncodeInt(chunks);

			if (chunks > 1) {
				xdrOut.xdrEncodeInt(len);

				int chunk_len = (int) Math.ceil(len / (float) chunks);
				int first_chunk_len = (int)(len - (long)((chunks - 1) * chunk_len));

				Serializable arr = MatrixTool.initializeArray(type, first_chunk_len);
				System.arraycopy(newValue, 0, arr, 0, first_chunk_len);
				write(arr, type);
				int index = first_chunk_len;

				if (chunk_len != first_chunk_len) {
					arr = MatrixTool.initializeArray(type, chunk_len);
				}
				for (int i = 1; i < chunks; i++) {
					flush();
					System.arraycopy(newValue, index, arr, 0, chunk_len);
					write(arr, type);
					index += chunk_len;
				}
			} else {
				write(newValue, type);
			}
		} else {
			switch (type) {
				case BYTE:
					xdrOut.xdrEncodeByte((Byte) newValue);
					break;
				case SHORT:
					xdrOut.xdrEncodeShort((Short) newValue);
					break;
				case LONG:
					xdrOut.xdrEncodeLong((Long) newValue);
					break;
				case FLOAT:
					xdrOut.xdrEncodeFloat((Float) newValue);
					break;
				default:
					throw new IllegalArgumentException("Datatype " + type + " not recognized");
			}
		}
	}

	private void write(Serializable newValue, SerializableDatatype type) throws IOException {
		switch (type.typeOf()) {
			case STRING_ARR:
				xdrOut.xdrEncodeStringVector((String[]) newValue);
				break;
			case BOOLEAN_ARR:
				xdrOut.xdrEncodeBooleanVector((boolean[]) newValue);
				break;
			case SHORT_ARR:
				xdrOut.xdrEncodeShortVector((short[]) newValue);
				break;
			case INT_ARR:
				xdrOut.xdrEncodeIntVector((int[]) newValue);
				break;
			case LONG_ARR:
				xdrOut.xdrEncodeLongVector((long[]) newValue);
				break;
			case FLOAT_ARR:
				xdrOut.xdrEncodeFloatVector((float[]) newValue);
				break;
			case DOUBLE_ARR:
				xdrOut.xdrEncodeDoubleVector((double[]) newValue);
				break;
			case BYTE_ARR:
				xdrOut.xdrEncodeDynamicOpaque((byte[]) newValue);
				break;
			default:
				throw new IllegalArgumentException("Datatype " + type + " not recognized");
		}
	}
}
