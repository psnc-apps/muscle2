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
public class XdrNIODeserializerWrapper implements DeserializerWrapper {
	private final XdrDecodingStream xdrIn;
	private boolean isClean;

	public XdrNIODeserializerWrapper(XdrDecodingStream xdrIn) {
		this.xdrIn = xdrIn;
		this.isClean = true;
	}
	
	@Override
	public void refresh() throws IOException {
		this.cleanUp();
		xdrIn.beginDecoding();
		this.isClean = false;
	}

	@Override
	public void cleanUp() throws IOException {
		if (!isClean) {
			xdrIn.endDecoding();
			this.isClean = true;
		}
	}

	@Override
	public int readInt() throws IOException {
		return xdrIn.xdrDecodeInt();
	}

	@Override
	public boolean readBoolean() throws IOException {
		return xdrIn.xdrDecodeBoolean();
	}

	@Override
	public byte[] readByteArray() throws IOException {
		return (byte[])readValue(SerializableDatatype.BYTE_ARR);
	}
	
	@Override
	public String readString() throws IOException {
		return xdrIn.xdrDecodeString();
	}
	
	@Override
	public double readDouble() throws IOException {
		return xdrIn.xdrDecodeDouble();
	}
	
	public Serializable readValue(SerializableDatatype type) throws IOException {
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
	}

	public XdrDecodingStream getXdrDecodingStream() {
		return this.xdrIn;
	}

	@Override
	public void close() throws IOException {
		this.xdrIn.close();
	}
	
	private Serializable parse(SerializableDatatype type) throws IOException {
		switch (type.typeOf()) {
			case BYTE_ARR:
				return xdrIn.xdrDecodeByteVector();
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
	
	private int parseChunk(Serializable value, SerializableDatatype type, int startAt) throws IOException {
		Serializable arr = parse(type);
		int len = MatrixTool.lengthOfArray(arr, type);
		System.arraycopy(arr, 0, value, startAt, len);
		return len;
	}
}
