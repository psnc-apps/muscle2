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
import org.acplt.oncrpc.OncRpcException;
import org.acplt.oncrpc.XdrDecodingStream;

/**
 *
 * @author Joris Borgdorff
 */
public class XdrDeserializerWrapper implements DeserializerWrapper {
	private final XdrDecodingStream xdrIn;
	private boolean isClean = true;

	public XdrDeserializerWrapper(XdrDecodingStream xdrIn) {
		this.xdrIn = xdrIn;
	}
	
	@Override
	public void refresh() throws IOException {
		this.cleanUp();
		try {
			xdrIn.beginDecoding();
			this.isClean = false;
		} catch (OncRpcException ex) {
			throw new IOException("Sending side hung up", ex);
		}
	}

	@Override
	public void cleanUp() throws IOException {
		if (!isClean) {
			try {
				xdrIn.endDecoding();
				this.isClean = true;
			} catch (OncRpcException ex) {
				throw new IllegalStateException("Can not clean up", ex);
			}
		}
	}

	@Override
	public int readInt() throws IOException {
		try {
			return xdrIn.xdrDecodeInt();
		} catch (OncRpcException ex) {
			throw new IllegalStateException("Message is depleted or next value is not an int", ex);
		}
	}

	@Override
	public boolean readBoolean() throws IOException {
		try {
			return xdrIn.xdrDecodeBoolean();
		} catch (OncRpcException ex) {
			throw new IllegalStateException("Message is depleted or next value is not a boolean", ex);
		}
	}

	@Override
	public byte[] readByteArray() throws IOException {
		return (byte[])readValue(SerializableDatatype.BYTE_ARR);
	}
	
	@Override
	public String readString() throws IOException {
		try {
			return xdrIn.xdrDecodeString();
		} catch (OncRpcException ex) {
			throw new IllegalStateException("Message is depleted or next value is not a String", ex);
		}
	}
	
	@Override
	public double readDouble() throws IOException {
		try {
			return xdrIn.xdrDecodeDouble();
		} catch (OncRpcException ex) {
			throw new IllegalStateException("Message is depleted or next value is not an int", ex);
		}
	}
	
	public Serializable readValue(SerializableDatatype type) throws IOException {
		try {
			if (type.typeOf().isArray()) {
				int chunks = xdrIn.xdrDecodeInt();
				
				if (chunks > 1) {
					int len = xdrIn.xdrDecodeInt();
					Serializable value = MatrixTool.initializeArray(type, len);
					int index = 0;
					index += parseChunk(value, type, index);
					for (int i = 1; i < chunks; i++) {
						xdrIn.endDecoding();
						xdrIn.beginDecoding();
						index += parseChunk(value, type, index);
					}
					return value;
				} else {
					return parse(type);
				}
			} else {
				switch (type) {
					case BYTE:
						return xdrIn.xdrDecodeByte();
					case SHORT:
						return xdrIn.xdrDecodeShort();
					case LONG:
						return xdrIn.xdrDecodeLong();
					case FLOAT:
						return xdrIn.xdrDecodeFloat();
					default:
						throw new IllegalArgumentException("Can not parse datatype " + type);
				}
			}
		} catch (OncRpcException ex) {
			throw new IllegalStateException("Message depleted or next value does not match " + type.typeOf(), ex);
		}
	}

	public XdrDecodingStream getXdrDecodingStream() {
		return this.xdrIn;
	}

	@Override
	public void close() throws IOException {
		try {
			this.xdrIn.close();
		} catch (OncRpcException ex) {
			throw new IllegalStateException("Can not close input", ex);
		}
	}
	
	private Serializable parse(SerializableDatatype type) throws OncRpcException, IOException {
		switch (type.typeOf()) {
			case BYTE_ARR:
				return xdrIn.xdrDecodeDynamicOpaque();
			case STRING_ARR:
				return xdrIn.xdrDecodeStringVector();
			case BOOLEAN_ARR:
				return xdrIn.xdrDecodeBooleanVector();
			case SHORT_ARR:
				return xdrIn.xdrDecodeShortVector();
			case INT_ARR:
				return xdrIn.xdrDecodeIntVector();
			case LONG_ARR:
				return xdrIn.xdrDecodeLongVector();
			case FLOAT_ARR:
				return xdrIn.xdrDecodeFloatVector();
			case DOUBLE_ARR:
				return xdrIn.xdrDecodeDoubleVector();
			default:
				throw new IllegalArgumentException("Can not parse type " + type);
		}
	}
	
	private int parseChunk(Serializable value, SerializableDatatype type, int startAt) throws IOException, OncRpcException {
		Serializable arr = parse(type);
		int len = MatrixTool.lengthOfArray(arr, type);
		System.arraycopy(arr, 0, value, startAt, len);
		return len;
	}
}
