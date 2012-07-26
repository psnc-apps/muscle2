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
