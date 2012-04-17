/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package utilities.data;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import muscle.core.messaging.serialization.*;
import org.acplt.oncrpc.OncRpcException;
import org.acplt.oncrpc.XdrDecodingStream;
import org.acplt.oncrpc.XdrEncodingStream;
import org.msgpack.packer.Packer;
import org.msgpack.unpacker.Unpacker;

/**
 * Stores data in a way that is serializable to other platforms.
 *
 * Aim is to support the datatypes listed in SerializableDatatype. Also serializable
 * Java objects may be added, which will be encoded as byte strings. This is not
 * interchangable with other languages.
 * 
 * At the moment, a direct interface with Xdr exists, other serialization methods
 * could also be implemented.
 * 
 * @author Joris Borgdorff
 */
public class SerializableData implements Serializable {
	private final SerializableDatatype type;
	private final Serializable value;
	private final int size;
	private final static SerializableDatatype[] datatypes = SerializableDatatype.values();
	
	/**
	 * Creates a new SerializableData object containing a value of with datatype type.
	 * It will use Java byte serialization if the object does not match any predetermined type.
	 * This will make the message incompatible with other programming languages.
	 * @param value any Serializable object of the right datatype
	 * @param type the type corresponding to the serializable object
	 * @throws IllegalArgumentException if the type provided does not match the data.
	 */
	public SerializableData(Serializable value, SerializableDatatype type, int size) {
		if (type == SerializableDatatype.NULL && value != null) {
			throw new IllegalArgumentException("A NULL datatype should be provided with null data.");
		} else if (type == SerializableDatatype.JAVA_BYTE_OBJECT) {
			this.value = serialize(value, type);
			this.size = sizeOf(value, type);
			this.type = type;
		} else if (type.getDataClass() != null && !type.getDataClass().isInstance(value)) {
			throw new IllegalArgumentException("Class of value '" + value.getClass() + "' does not match datatype '" + type + "'");
		} else if (value instanceof SerializableData) {
			SerializableData sValue = (SerializableData)value;
			this.value = sValue.value;
			this.size = sValue.size;
			this.type = sValue.type;
		} else {
			this.value = value;
			this.size = size;
			this.type = type;
		}
	}
	
	private static Serializable serialize(Serializable data, SerializableDatatype type) {
		if (type == SerializableDatatype.JAVA_BYTE_OBJECT && !(data instanceof byte[])) {
			return new ByteJavaObjectConverter<Serializable>().serialize(data);
		}
		return data;
	}
	
	/**
	 * Creates a new SerializableData object containing given value, guessing
	 * the right datatype.
	 * @param value any Serializable object
	 * @see SerializableData(Serializable, SerializableDatatype)
	 */
	public static SerializableData valueOf(Serializable value) {
		if (value == null) {
			return new SerializableData(null, SerializableDatatype.NULL, 0);
		} else if (value instanceof SerializableData) {
			return (SerializableData)value;
		}
		SerializableDatatype type = inferDatatype(value);
		int size = (type == SerializableDatatype.JAVA_BYTE_OBJECT) ? -1 : sizeOf(value, type);
		return new SerializableData(value, type, size);
	}
	
	private static SerializableDatatype inferDatatype(Serializable value) {
		if (value == null) {
			return SerializableDatatype.NULL;
		}
		for (SerializableDatatype type : SerializableDatatype.values()) {
			if (type.getDataClass() != null && type.getDataClass().isInstance(value)) {
				return type;
			}
		}
		return SerializableDatatype.JAVA_BYTE_OBJECT;
	}
	
	/**
	 * Get the data, unserialized. 
	 */
	public Serializable getValue() {
		if (type == SerializableDatatype.JAVA_BYTE_OBJECT) {
			return (Serializable)new ByteJavaObjectConverter().deserialize((byte[])value);
		}
		else {
			return value;
		}
	}

	/**
	 * Get the datatype.
	 */
	public SerializableDatatype getType() {
		return type;
	}
	
	public int getSize() {
		return size;
	}
	
	public static SerializableData parseData(DeserializerWrapper in) throws IOException {
		int typeNum = in.readInt();
		
		if (typeNum < 0 || typeNum >= datatypes.length) {
			throw new IllegalStateException("Datatype with number " + typeNum + " not recognized");
		}
		SerializableDatatype type = datatypes[typeNum];

		Serializable value = null;
		int len;

		switch (type.typeOf()) {
			case NULL:
				return new SerializableData(null, type, 0);
			case MAP:
				len = in.readInt();
				HashMap<Serializable,Serializable> xdrMap = new HashMap<Serializable,Serializable>(len*3/2);
				
				for (int i = 0; i < len; i++) {
					xdrMap.put(SerializableData.parseData(in).getValue(), SerializableData.parseData(in).getValue());
				}
				value = xdrMap;
				break;
			case COLLECTION:
				len = in.readInt();
				ArrayList<Serializable> xdrList = new ArrayList<Serializable>(len);
				
				for (int i = 0; i < len; i++) {
					xdrList.add(SerializableData.parseData(in).getValue());
				}
				value = xdrList;
				break;
			case STRING:
				value = in.readString();
				break;
			case BOOLEAN:
				value = in.readBoolean();
				break;
			case BYTE_ARR:
				value = in.readByteArray();
				break;
			case INT:
				value = in.readInt();
				break;
			case DOUBLE:
				value = in.readDouble();
				break;
		}
				
		if (value == null) {
			if (in instanceof XdrDeserializerWrapper) {
				try {
					XdrDecodingStream xdrIn = ((XdrDeserializerWrapper)in).getXdrDecodingStream();
					value = parseXdrData(xdrIn, type);
				} catch (OncRpcException ex) {
					throw new IllegalStateException("Could not parse data", ex);
				}
			} else {
				throw new IllegalArgumentException("Can not parse data from wrapper " + in.getClass().getName());
			}
		}
		
		int size = sizeOf(value, type);		
		if (type.isMatrix()) {
			int length = lengthOfMatrix(value, type);
			
			int dimX, dimY, dimZ, dimZZ;
			dimX = in.readInt();
			dimY = type.isMatrix2D() ? length / dimX : in.readInt();
			dimZ = type.isMatrix4D() ? in.readInt() : length / (dimX*dimY);
			dimZZ = type.isMatrix4D() ? length / (dimX*dimY*dimZ) : 1;
		
			value = arrayToMatrix(value, type, dimX, dimY, dimZ, dimZZ);
		}
		
		return new SerializableData(value, type, size);
	}
	
	public static Serializable parseMsgPackData(Unpacker unpacker, SerializableDatatype type) throws IOException {
		Serializable value = null;
		int len = 0;
		if (type.typeOf().isArray()) {
			len = unpacker.readArrayBegin();
		}
		
		switch (type.typeOf()) {
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
			case STRING_ARR:
				value = new String[len];
				for (int i = 0; i < len; i++) {
					((String[])value)[i] = unpacker.readString();
				}
				break;
			case BOOLEAN_ARR:
				value = new boolean[len];
				for (int i = 0; i < len; i++) {
					((boolean[])value)[i] = unpacker.readBoolean();
				}
				break;
			case SHORT_ARR:
				value = new short[len];
				for (int i = 0; i < len; i++) {
					((short[])value)[i] = unpacker.readShort();
				}
				break;
			case INT_ARR:
				value = new int[len];
				for (int i = 0; i < len; i++) {
					((int[])value)[i] = unpacker.readInt();
				}
				break;
			case LONG_ARR:
				value = new long[len];
				for (int i = 0; i < len; i++) {
					((long[])value)[i] = unpacker.readLong();
				}
				break;
			case FLOAT_ARR:
				value = new float[len];
				for (int i = 0; i < len; i++) {
					((float[])value)[i] = unpacker.readFloat();
				}
				break;
			case DOUBLE_ARR:
				value = new double[len];
				for (int i = 0; i < len; i++) {
					((double[])value)[i] = unpacker.readDouble();
				}
				break;
		}
		if (type.typeOf().isArray()) {
			unpacker.readArrayEnd();
		}
				
		return value;
	}
	
	/**
	 * Parses a SerializableData from an XdrDecodingStream.
	 * @param xdrIn an initialized XdrDecodingStream
	 * @return a new SerializableData
	 * @throws OncRpcException if the supplied data is not correct
	 * @throws IOException if the connection fails
	 */
	private static Serializable parseXdrData(XdrDecodingStream xdrIn, SerializableDatatype type) throws OncRpcException, IOException {		
		Serializable value = null;
		
		switch (type.typeOf()) {
			case STRING_ARR:
				value = xdrIn.xdrDecodeStringVector();
				break;
			case BOOLEAN_ARR:
				value = xdrIn.xdrDecodeBooleanVector();
				break;
			case BYTE:
				value = xdrIn.xdrDecodeByte();
				break;
			case SHORT:
				value = xdrIn.xdrDecodeShort();
				break;
			case SHORT_ARR:
				value = xdrIn.xdrDecodeShortVector();
				break;
			case INT_ARR:
				value = xdrIn.xdrDecodeIntVector();
				break;
			case LONG:
				value = xdrIn.xdrDecodeLong();
				break;
			case LONG_ARR:
				value = xdrIn.xdrDecodeLongVector();
				break;
			case FLOAT:
				value = xdrIn.xdrDecodeFloat();
				break;
			case FLOAT_ARR:
				value = xdrIn.xdrDecodeFloatVector();
				break;
			case DOUBLE_ARR:
				value = xdrIn.xdrDecodeDoubleVector();
				break;
		}
		
		return value;
	}
	
	public void encodeData(SerializerWrapper out) throws IOException {
		out.writeInt(type.ordinal());
		
		Object newValue = matrixToArray();
		
		switch (type.typeOf()) {
			case NULL:
				break;
			case MAP:
				Map xdrMap = (Map)newValue;
				out.writeInt(xdrMap.size());
				for (Iterator it = xdrMap.entrySet().iterator(); it.hasNext();) {
					Map.Entry entry = (Map.Entry)it.next();
					valueOf((Serializable)entry.getKey()).encodeData(out);
					valueOf((Serializable)entry.getValue()).encodeData(out);
				}
				break;
			case COLLECTION:
				Collection xdrList = (Collection)newValue;
				out.writeInt(xdrList.size());
				
				for (Object data : xdrList) {
					valueOf((Serializable)data).encodeData(out);
				}
				break;
			case STRING:
				out.writeString((String)newValue);
				break;
			case BOOLEAN:
				out.writeBoolean((Boolean)newValue);
				break;
			case BYTE_ARR:
				out.writeByteArray((byte[])newValue);
				break;
			case INT:
				out.writeInt((Integer)newValue);
				break;
			case DOUBLE:
				out.writeDouble((Double)newValue);
				break;
			default:
				if (out instanceof XdrSerializerWrapper) {
					try {
						encodeXdrData(((XdrSerializerWrapper)out).getXdrEncodingStream(), newValue);
					} catch (OncRpcException ex) {
						throw new IllegalStateException("Could not parse data", ex);
					}
				} else if (out instanceof PackerWrapper) {
					encodePackerData(((PackerWrapper)out).getPacker(), newValue);
				} else {
					throw new IllegalArgumentException("Can not parse data from wrapper " + out.getClass().getName());
				}
		}
		if (type.isMatrix()) {
			int dimNum = type.getDimensions();
			int[] dims = new int[dimNum];
			dimensionsOfMatrix(value, type, dims, 0);
			for (int i = 0; i < dimNum - 1; i++) {
				out.writeInt(dims[i]);
			}
		}
	}
	
	private void encodePackerData(Packer packer, Object newValue) throws IOException {
		int len = 0;
		if (type.typeOf().isArray()) {
			len = lengthOfMatrix(newValue, type);
			packer.writeArrayBegin(len);
		}
		switch (type.typeOf()) {
			case BYTE:
				packer.write((Byte)newValue);
				break;
			case SHORT:
				packer.write((Short)newValue);
				break;
			case FLOAT:
				packer.write((Float)newValue);
				break;
			case LONG:
				packer.write((Long)newValue);
				break;
			default:
				// No more packer-defined-types
				packer.write(newValue);
		}
		
		if (type.typeOf().isArray()) {
			packer.writeArrayEnd();
		}
	}
	
	/**
	 * Encodes the data over an XdrEncodingStream.
	 * @param xdrOut an initialized XdrEncodingStream, it is not flushed.
	 * @throws OncRpcException if the supplied data is not correct
	 * @throws IOException if the connection fails
	 */
	private void encodeXdrData(XdrEncodingStream xdrOut, Object newValue) throws OncRpcException, IOException {
		switch (type.typeOf()) {
			case STRING_ARR:
				xdrOut.xdrEncodeStringVector((String[])newValue);
				break;
			case BOOLEAN_ARR:
				xdrOut.xdrEncodeBooleanVector((boolean[])newValue);
				break;
			case BYTE:
				xdrOut.xdrEncodeByte((Byte)newValue);
				break;
			case SHORT:
				xdrOut.xdrEncodeShort((Short)newValue);
				break;
			case SHORT_ARR:
				xdrOut.xdrEncodeShortVector((short[])newValue);
				break;
			case INT_ARR:
				xdrOut.xdrEncodeIntVector((int[])newValue);
				break;
			case LONG:
				xdrOut.xdrEncodeLong((Long)newValue);
				break;
			case LONG_ARR:
				xdrOut.xdrEncodeLongVector((long[])newValue);
				break;
			case FLOAT:
				xdrOut.xdrEncodeFloat((Float)newValue);
				break;
			case FLOAT_ARR:
				xdrOut.xdrEncodeFloatVector((float[])newValue);
				break;
			case DOUBLE_ARR:
				xdrOut.xdrEncodeDoubleVector((double[])newValue);
				break;
			default:
				throw new OncRpcException("Datatype " + type + " not recognized");
		}
	}
	
	private static int sizeOf(Serializable value, SerializableDatatype type) {
		int size = 0;
		if ((type.isArray() || type.isMatrix()) && type != SerializableDatatype.STRING_ARR) {
			return deepSizeOf(value, type);
		} else if (type == SerializableDatatype.JAVA_BYTE_OBJECT) {
			return deepSizeOf(serialize(value, type), type);
		} else switch (type.typeOf()) {
			case NULL:
				break;
			case MAP:				
				Map xdrMap = (Map)value;
				size = 4;
				for (Iterator it = xdrMap.entrySet().iterator(); it.hasNext();) {
					Map.Entry entry = (Map.Entry)it.next();
					size += ((String)entry.getKey()).length()*2+4;
					size += 4;
					size += sizeOf((Serializable)entry.getValue(), inferDatatype((Serializable)entry.getValue()));
				}
				break;
			case COLLECTION:
				Collection xdrList = (Collection)value;
				size = 4;
				for (Object data : xdrList) {
					size += sizeOf((Serializable)data, inferDatatype((Serializable)data));
				}
				break;
			case STRING:
				size = 4 + ((String)value).length()*2;
				break;
			case STRING_ARR:
				size = 4;
				for (String s : (String[])value) {
					size += 4 + s.length()*2;
				}
				break;
			case BOOLEAN:
				size = 1;
				break;
			case BYTE:
				size = 1;
				break;
			case SHORT:
				size = 2;
				break;
			case INT:
				size = 4;
				break;
			case LONG:
				size = 8;
				break;
			case FLOAT:
				size = 4;
				break;
			case DOUBLE:
				size = 8;
				break;
			default:
				throw new IllegalArgumentException("Datatype " + type + " not recognized");
		}
		
		return size;
	}
	
	private static int deepSizeOf(Object value, SerializableDatatype type) {
		int size = type.getDimensions()*4;

		switch (type.typeOf()) {
			case BOOLEAN_ARR:
				size += lengthOfMatrix(value, type)/8;
				break;
			case BYTE_ARR:
				size += lengthOfMatrix(value, type);
				break;
			case SHORT_ARR:
				size += lengthOfMatrix(value, type)*2;
				break;
			case INT_ARR: case FLOAT_ARR:
				size += lengthOfMatrix(value, type)*4;
				break;
			case LONG_ARR: case DOUBLE_ARR:
				size += lengthOfMatrix(value, type)*8;
				break;
		}
		return size;
	}
	
	private static int lengthOfMatrix(Object value, SerializableDatatype type) {
		int[] dims = {1, 1, 1, 1};
		dimensionsOfMatrix(value, type, dims, 0);
		return dims[0]*dims[1]*dims[2]*dims[3];
	}
	
	private static void dimensionsOfMatrix(Object value, SerializableDatatype type, int[] dims, int depth) {
		if (value instanceof Object[]) {
			dims[depth] = ((Object[])value).length;
			dimensionsOfMatrix(value, type, dims, depth + 1);
		}
		else {
			switch (type.typeOf()) {
				case BOOLEAN_ARR:
					dims[depth] = ((boolean[])value).length;
					break;
				case BYTE_ARR:
					dims[depth] = ((byte[])value).length;
					break;
				case SHORT_ARR:
					dims[depth] = ((short[])value).length;
					break;
				case INT_ARR:
					dims[depth] = ((int[])value).length;
					break;
				case LONG_ARR:
					dims[depth] = ((long[])value).length;
					break;
				case FLOAT_ARR:
					dims[depth] = ((float[])value).length;
					break;
				case DOUBLE_ARR:
					dims[depth] = ((double[])value).length;
					break;
				default:
					throw new IllegalArgumentException("Can only compute the length of arrays");
			}
		}
	}
	
	/**
	 * If the given type is a matrix, it converts given array to a matrix
	 * @throws ClassCastException if the given data is not an array of the correct type.
	 */
	private static Serializable arrayToMatrix(Serializable value, SerializableDatatype type, int dimX, int dimY, int dimZ, int dimZZ) {
		int count = 0;
		Serializable matrixValue = value;
		switch (type) {
			case BOOLEAN_MATRIX_2D: {
				boolean[][] newValue = new boolean[dimX][dimY];
				for (int i = 0; i < dimX; i++) {
					System.arraycopy(value, i*dimY, newValue[i], 0, dimY);
				}
				matrixValue = newValue; }
				break;
			case BYTE_MATRIX_2D: {
				byte[][] newValue = new byte[dimX][dimY];
				for (int i = 0; i < dimX; i++) {
					System.arraycopy(value, i*dimY, newValue[i], 0, dimY);
				}
				matrixValue = newValue; }
				break;
			case SHORT_MATRIX_2D: {
				short[][] newValue = new short[dimX][dimY];
				for (int i = 0; i < dimX; i++) {
					System.arraycopy(value, i*dimY, newValue[i], 0, dimY);
				}
				matrixValue = newValue; }
				break;
			case INT_MATRIX_2D: {
				int[][] newValue = new int[dimX][dimY];
				for (int i = 0; i < dimX; i++) {
					System.arraycopy(value, i*dimY, newValue[i], 0, dimY);
				}
				matrixValue = newValue; }
				break;
			case LONG_MATRIX_2D: {
				long[][] newValue = new long[dimX][dimY];
				for (int i = 0; i < dimX; i++) {
					System.arraycopy(value, i*dimY, newValue[i], 0, dimY);
				}
				matrixValue = newValue; }
				break;
			case FLOAT_MATRIX_2D: {
				float[][] newValue = new float[dimX][dimY];
				for (int i = 0; i < dimX; i++) {
					System.arraycopy(value, i*dimY, newValue[i], 0, dimY);
				}
				matrixValue = newValue; }
				break;
			case DOUBLE_MATRIX_2D: {
				double[][] newValue = new double[dimX][dimY];
				for (int i = 0; i < dimX; i++) {
					System.arraycopy(value, i*dimY, newValue[i], 0, dimY);
				}
				matrixValue = newValue; }
				break;
			case BOOLEAN_MATRIX_3D: {
				boolean[][][] newValue = new boolean[dimX][dimY][dimZ];
				for (int i = 0; i < dimX; i++) {
					for (int j = 0; j < dimY; j++) {
						System.arraycopy(value, count, newValue[i][j], 0, dimZ);
						count += dimZ;
					}
				}
				matrixValue = newValue; }
				break;
			case BYTE_MATRIX_3D: {
				byte[][][] newValue = new byte[dimX][dimY][dimZ];
				for (int i = 0; i < dimX; i++) {
					for (int j = 0; j < dimY; j++) {
						System.arraycopy(value, count, newValue[i][j], 0, dimZ);
						count += dimZ;
					}
				}
				matrixValue = newValue; }
				break;
			case SHORT_MATRIX_3D: {
				short[][][] newValue = new short[dimX][dimY][dimZ];
				for (int i = 0; i < dimX; i++) {
					for (int j = 0; j < dimY; j++) {
						System.arraycopy(value, count, newValue[i][j], 0, dimZ);
						count += dimZ;
					}
				}
				matrixValue = newValue; }
				break;
			case INT_MATRIX_3D: {
				int[][][] newValue = new int[dimX][dimY][dimZ];
				for (int i = 0; i < dimX; i++) {
					for (int j = 0; j < dimY; j++) {
						System.arraycopy(value, count, newValue[i][j], 0, dimZ);
						count += dimZ;
					}
				}
				matrixValue = newValue; }
				break;
			case LONG_MATRIX_3D: {
				long[][][] newValue = new long[dimX][dimY][dimZ];
				for (int i = 0; i < dimX; i++) {
					for (int j = 0; j < dimY; j++) {
						System.arraycopy(value, count, newValue[i][j], 0, dimZ);
						count += dimZ;
					}
				}
				matrixValue = newValue; }
				break;
			case FLOAT_MATRIX_3D: {
				float[][][] newValue = new float[dimX][dimY][dimZ];
				for (int i = 0; i < dimX; i++) {
					for (int j = 0; j < dimY; j++) {
						System.arraycopy(value, count, newValue[i][j], 0, dimZ);
						count += dimZ;
					}
				}
				matrixValue = newValue; }
				break;
			case DOUBLE_MATRIX_3D: {
				double[][][] newValue = new double[dimX][dimY][dimZ];
				for (int i = 0; i < dimX; i++) {
					for (int j = 0; j < dimY; j++) {
						System.arraycopy(value, count, newValue[i][j], 0, dimZ);
						count += dimZ;
					}
				}
				matrixValue = newValue; }
				break;
			case BOOLEAN_MATRIX_4D: {
				boolean[][][][] newValue = new boolean[dimX][dimY][dimZ][dimZZ];
				for (int i = 0; i < dimX; i++) {
					for (int j = 0; j < dimY; j++) {
						for (int k = 0; k < dimZ; k++) {
							System.arraycopy(value, count, newValue[i][j][k], 0, dimZZ);
							count += dimZZ;
						}
					}
				}
				matrixValue = newValue; }
				break;
			case BYTE_MATRIX_4D: {
				byte[][][][] newValue = new byte[dimX][dimY][dimZ][dimZZ];
				for (int i = 0; i < dimX; i++) {
					for (int j = 0; j < dimY; j++) {
						for (int k = 0; k < dimZ; k++) {
							System.arraycopy(value, count, newValue[i][j][k], 0, dimZZ);
							count += dimZZ;
						}
					}
				}
				matrixValue = newValue; }
				break;
			case SHORT_MATRIX_4D: {
				short[][][][] newValue = new short[dimX][dimY][dimZ][dimZZ];
				for (int i = 0; i < dimX; i++) {
					for (int j = 0; j < dimY; j++) {
						for (int k = 0; k < dimZ; k++) {
							System.arraycopy(value, count, newValue[i][j][k], 0, dimZZ);
							count += dimZZ;
						}
					}
				}
				matrixValue = newValue; }
				break;
			case INT_MATRIX_4D: {
				int[][][][] newValue = new int[dimX][dimY][dimZ][dimZZ];
				for (int i = 0; i < dimX; i++) {
					for (int j = 0; j < dimY; j++) {
						for (int k = 0; k < dimZ; k++) {
							System.arraycopy(value, count, newValue[i][j][k], 0, dimZZ);
							count += dimZZ;
						}
					}
				}
				matrixValue = newValue; }
				break;
			case LONG_MATRIX_4D: {
				long[][][][] newValue = new long[dimX][dimY][dimZ][dimZZ];
				for (int i = 0; i < dimX; i++) {
					for (int j = 0; j < dimY; j++) {
						for (int k = 0; k < dimZ; k++) {
							System.arraycopy(value, count, newValue[i][j][k], 0, dimZZ);
							count += dimZZ;
						}
					}
				}
				matrixValue = newValue; }
				break;
			case FLOAT_MATRIX_4D: {
				float[][][][] newValue = new float[dimX][dimY][dimZ][dimZZ];
				for (int i = 0; i < dimX; i++) {
					for (int j = 0; j < dimY; j++) {
						for (int k = 0; k < dimZ; k++) {
							System.arraycopy(value, count, newValue[i][j][k], 0, dimZZ);
							count += dimZZ;
						}
					}
				}
				matrixValue = newValue; }
				break;
			case DOUBLE_MATRIX_4D: {
				double[][][][] newValue = new double[dimX][dimY][dimZ][dimZZ];
				for (int i = 0; i < dimX; i++) {
					for (int j = 0; j < dimY; j++) {
						for (int k = 0; k < dimZ; k++) {
							System.arraycopy(value, count, newValue[i][j][k], 0, dimZZ);
							count += dimZZ;
						}
					}
				}
				matrixValue = newValue; }
				break;
		}
		return matrixValue;
	}
	
	/**
	 * Gets the current data. If the datatype is a matrix, it converts it to an array first.
	 * @throws ClassCastException if the data is not a matrix of the correct type.
	 */
	private Serializable matrixToArray() {
		Serializable newValue = value;
		
		if (type.isMatrix()) {
			int dimX = 1, dimY = 1, dimZ = 1, dimZZ = 1, count = 0;
			switch (type) {
				case BOOLEAN_MATRIX_2D: {
					boolean[][] oldValue = (boolean[][])value;
					dimX = oldValue.length; dimY = dimX > 0 ? oldValue[0].length : 0;
					newValue = new boolean[dimX*dimY];
					for (int i = 0; i < dimX; i++) {
						System.arraycopy(oldValue[i], 0, newValue, i*dimY, dimY);
					}
					} break;
				case BYTE_MATRIX_2D: {
					byte[][] oldValue = (byte[][])value;
					dimX = oldValue.length; dimY = dimX > 0 ? oldValue[0].length : 0;
					newValue = new byte[dimX*dimY];
					for (int i = 0; i < dimX; i++) {
						System.arraycopy(oldValue[i], 0, newValue, i*dimY, dimY);
					}
					} break;
				case SHORT_MATRIX_2D: {
					short[][] oldValue = (short[][])value;
					dimX = oldValue.length; dimY = dimX > 0 ? oldValue[0].length : 0;
					newValue = new short[dimX*dimY];
					for (int i = 0; i < dimX; i++) {
						System.arraycopy(oldValue[i], 0, newValue, i*dimY, dimY);
					}
					} break;
				case INT_MATRIX_2D: {
					int[][] oldValue = (int[][])value;
					dimX = oldValue.length; dimY = dimX > 0 ? oldValue[0].length : 0;
					newValue = new int[dimX*dimY];
					for (int i = 0; i < dimX; i++) {
						System.arraycopy(oldValue[i], 0, newValue, i*dimY, dimY);
					}
					} break;
				case LONG_MATRIX_2D: {
					long[][] oldValue = (long[][])value;
					dimX = oldValue.length; dimY = dimX > 0 ? oldValue[0].length : 0;
					newValue = new long[dimX*dimY];
					for (int i = 0; i < dimX; i++) {
						System.arraycopy(oldValue[i], 0, newValue, i*dimY, dimY);
					}
					} break;
				case FLOAT_MATRIX_2D: {
					float[][] oldValue = (float[][])value;
					dimX = oldValue.length; dimY = dimX > 0 ? oldValue[0].length : 0;
					newValue = new float[dimX*dimY];
					for (int i = 0; i < dimX; i++) {
						System.arraycopy(oldValue[i], 0, newValue, i*dimY, dimY);
					}
					} break;
				case DOUBLE_MATRIX_2D: {
					double[][] oldValue = (double[][])value;
					dimX = oldValue.length; dimY = dimX > 0 ? oldValue[0].length : 0;
					newValue = new double[dimX*dimY];
					for (int i = 0; i < dimX; i++) {
						System.arraycopy(oldValue[i], 0, newValue, i*dimY, dimY);
					}
					} break;
				case BOOLEAN_MATRIX_3D: {
					boolean[][][] oldValue = (boolean[][][])value;
					dimX = oldValue.length; dimY = dimX > 0 ? oldValue[0].length : 0; dimZ = dimY > 0 ? oldValue[0][0].length : 0;
					newValue = new boolean[dimX*dimY*dimZ];
					for (int i = 0; i < dimX; i++) {
						for (int j = 0; j < dimY; j++) {
							System.arraycopy(oldValue[i][j], 0, newValue, count, dimZ);
							count += dimZ;
						}
					}
					} break;
				case BYTE_MATRIX_3D: {
					byte[][][] oldValue = (byte[][][])value;
					dimX = oldValue.length; dimY = dimX > 0 ? oldValue[0].length : 0; dimZ = dimY > 0 ? oldValue[0][0].length : 0;
					newValue = new byte[dimX*dimY*dimZ];
					for (int i = 0; i < dimX; i++) {
						for (int j = 0; j < dimY; j++) {
							System.arraycopy(oldValue[i][j], 0, newValue, count, dimZ);
							count += dimZ;
						}
					}
					} break;
				case SHORT_MATRIX_3D: {
					short[][][] oldValue = (short[][][])value;
					dimX = oldValue.length; dimY = dimX > 0 ? oldValue[0].length : 0; dimZ = dimY > 0 ? oldValue[0][0].length : 0;
					newValue = new short[dimX*dimY*dimZ];
					for (int i = 0; i < dimX; i++) {
						for (int j = 0; j < dimY; j++) {
							System.arraycopy(oldValue[i][j], 0, newValue, count, dimZ);
							count += dimZ;
						}
					}
					} break;
				case INT_MATRIX_3D: {
					int[][][] oldValue = (int[][][])value;
					dimX = oldValue.length; dimY = dimX > 0 ? oldValue[0].length : 0; dimZ = dimY > 0 ? oldValue[0][0].length : 0;
					newValue = new int[dimX*dimY*dimZ];
					for (int i = 0; i < dimX; i++) {
						for (int j = 0; j < dimY; j++) {
							System.arraycopy(oldValue[i][j], 0, newValue, count, dimZ);
							count += dimZ;
						}
					}
					} break;
				case LONG_MATRIX_3D: {
					long[][][] oldValue = (long[][][])value;
					dimX = oldValue.length; dimY = dimX > 0 ? oldValue[0].length : 0; dimZ = dimY > 0 ? oldValue[0][0].length : 0;
					newValue = new long[dimX*dimY*dimZ];
					for (int i = 0; i < dimX; i++) {
						for (int j = 0; j < dimY; j++) {
							System.arraycopy(oldValue[i][j], 0, newValue, count, dimZ);
							count += dimZ;
						}
					}
					} break;
				case FLOAT_MATRIX_3D: {
					float[][][] oldValue = (float[][][])value;
					dimX = oldValue.length; dimY = dimX > 0 ? oldValue[0].length : 0; dimZ = dimY > 0 ? oldValue[0][0].length : 0;
					newValue = new float[dimX*dimY*dimZ];
					for (int i = 0; i < dimX; i++) {
						for (int j = 0; j < dimY; j++) {
							System.arraycopy(oldValue[i][j], 0, newValue, count, dimZ);
							count += dimZ;
						}
					}
					} break;
				case DOUBLE_MATRIX_3D: {
					double[][][] oldValue = (double[][][])value;
					dimX = oldValue.length; dimY = dimX > 0 ? oldValue[0].length : 0; dimZ = dimY > 0 ? oldValue[0][0].length : 0;
					newValue = new double[dimX*dimY*dimZ];
					for (int i = 0; i < dimX; i++) {
						for (int j = 0; j < dimY; j++) {
							System.arraycopy(oldValue[i][j], 0, newValue, count, dimZ);
							count += dimZ;
						}
					}
					} break;
				case BOOLEAN_MATRIX_4D: {
					boolean[][][][] oldValue = (boolean[][][][])value;
					dimX = oldValue.length; dimY = dimX > 0 ? oldValue[0].length : 0; dimZ = dimY > 0 ? oldValue[0][0].length : 0; dimZZ = dimZ > 0 ? oldValue[0][0][0].length : 0;
					newValue = new boolean[dimX*dimY*dimZ*dimZZ];
					for (int i = 0; i < dimX; i++) {
						for (int j = 0; j < dimY; j++) {
							for (int k = 0; k < dimZ; k++) {
								System.arraycopy(oldValue[i][j][k], 0, newValue, count, dimZZ);
								count += dimZZ;
							}
						}
					}
					} break;
				case BYTE_MATRIX_4D: {
					byte[][][][] oldValue = (byte[][][][])value;
					dimX = oldValue.length; dimY = dimX > 0 ? oldValue[0].length : 0; dimZ = dimY > 0 ? oldValue[0][0].length : 0; dimZZ = dimZ > 0 ? oldValue[0][0][0].length : 0;
					newValue = new byte[dimX*dimY*dimZ*dimZZ];
					for (int i = 0; i < dimX; i++) {
						for (int j = 0; j < dimY; j++) {
							for (int k = 0; k < dimZ; k++) {
								System.arraycopy(oldValue[i][j][k], 0, newValue, count, dimZZ);
								count += dimZZ;
							}
						}
					}
					} break;
				case SHORT_MATRIX_4D: {
					short[][][][] oldValue = (short[][][][])value;
					dimX = oldValue.length; dimY = dimX > 0 ? oldValue[0].length : 0; dimZ = dimY > 0 ? oldValue[0][0].length : 0; dimZZ = dimZ > 0 ? oldValue[0][0][0].length : 0;
					newValue = new short[dimX*dimY*dimZ*dimZZ];
					for (int i = 0; i < dimX; i++) {
						for (int j = 0; j < dimY; j++) {
							for (int k = 0; k < dimZ; k++) {
								System.arraycopy(oldValue[i][j][k], 0, newValue, count, dimZZ);
								count += dimZZ;
							}
						}
					}
					} break;
				case INT_MATRIX_4D: {
					int[][][][] oldValue = (int[][][][])value;
					dimX = oldValue.length; dimY = dimX > 0 ? oldValue[0].length : 0; dimZ = dimY > 0 ? oldValue[0][0].length : 0; dimZZ = dimZ > 0 ? oldValue[0][0][0].length : 0;
					newValue = new int[dimX*dimY*dimZ*dimZZ];
					for (int i = 0; i < dimX; i++) {
						for (int j = 0; j < dimY; j++) {
							for (int k = 0; k < dimZ; k++) {
								System.arraycopy(oldValue[i][j][k], 0, newValue, count, dimZZ);
								count += dimZZ;
							}
						}
					}
					} break;
				case LONG_MATRIX_4D: {
					long[][][][] oldValue = (long[][][][])value;
					dimX = oldValue.length; dimY = dimX > 0 ? oldValue[0].length : 0; dimZ = dimY > 0 ? oldValue[0][0].length : 0; dimZZ = dimZ > 0 ? oldValue[0][0][0].length : 0;
					newValue = new long[dimX*dimY*dimZ*dimZZ];
					for (int i = 0; i < dimX; i++) {
						for (int j = 0; j < dimY; j++) {
							for (int k = 0; k < dimZ; k++) {
								System.arraycopy(oldValue[i][j][k], 0, newValue, count, dimZZ);
								count += dimZZ;
							}
						}
					}
					} break;
				case FLOAT_MATRIX_4D: {
					float[][][][] oldValue = (float[][][][])value;
					dimX = oldValue.length; dimY = dimX > 0 ? oldValue[0].length : 0; dimZ = dimY > 0 ? oldValue[0][0].length : 0; dimZZ = dimZ > 0 ? oldValue[0][0][0].length : 0;
					newValue = new float[dimX*dimY*dimZ*dimZZ];
					for (int i = 0; i < dimX; i++) {
						for (int j = 0; j < dimY; j++) {
							for (int k = 0; k < dimZ; k++) {
								System.arraycopy(oldValue[i][j][k], 0, newValue, count, dimZZ);
								count += dimZZ;
							}
						}
					}
					} break;
				case DOUBLE_MATRIX_4D: {
					double[][][][] oldValue = (double[][][][])value;
					dimX = oldValue.length; dimY = dimX > 0 ? oldValue[0].length : 0; dimZ = dimY > 0 ? oldValue[0][0].length : 0; dimZZ = dimZ > 0 ? oldValue[0][0][0].length : 0;
					newValue = new double[dimX*dimY*dimZ*dimZZ];
					for (int i = 0; i < dimX; i++) {
						for (int j = 0; j < dimY; j++) {
							for (int k = 0; k < dimZ; k++) {
								System.arraycopy(oldValue[i][j][k], 0, newValue, count, dimZZ);
								count += dimZZ;
							}
						}
					}
					} break;
			}
		}
		
		return newValue;
	}
	
	public static <T extends Serializable> T createIndependent(T value) {
		if (value instanceof SerializableData) {
			SerializableData sValue = (SerializableData)value;
			return (T)new SerializableData(createIndependent(sValue.getValue()), sValue.type, sValue.size);
		}
		Serializable copyValue;
		int dimX, dimY, dimZ, dimZZ;
		SerializableDatatype type = inferDatatype(value);
		switch (type) {
			// Immutable
			case NULL: case STRING: case BOOLEAN: case BYTE: case SHORT: case INT: case LONG: case FLOAT: case DOUBLE:
				copyValue = value;
				break;
			case MAP: {
				int len = ((Map)value).size();
				HashMap<String,Serializable> map = new HashMap<String,Serializable>(len*3/2);
				for (Iterator it = ((Map)value).entrySet().iterator(); it.hasNext();) {
					Map.Entry entry = (Map.Entry)it.next();
					map.put((String)entry.getKey(),createIndependent((Serializable)entry.getValue()));
				}
				copyValue = map;
			} break;
			case COLLECTION: {
				int len = ((Collection)value).size();
				ArrayList<Serializable> list = new ArrayList<Serializable>(len);
				for (Object o : ((Collection)value)) {
					list.add(createIndependent((Serializable)o));
				}
				copyValue = list;
			} break;
			case JAVA_BYTE_OBJECT:
				copyValue = new SerializableData(value, type, -1).getValue();
				break;
			case BOOLEAN_ARR: {
				int len = ((boolean[])value).length;
				copyValue = new boolean[len];
				System.arraycopy(value, 0, copyValue, 0, len);
			} break;
			case BYTE_ARR: {
				int len = ((byte[])value).length;
				copyValue = new byte[len];
				System.arraycopy(value, 0, copyValue, 0, len);
			} break;
			case SHORT_ARR: {
				int len = ((short[])value).length;
				copyValue = new short[len];
				System.arraycopy(value, 0, copyValue, 0, len);
			} break;
			case INT_ARR: {
				int len = ((int[])value).length;
				copyValue = new int[len];
				System.arraycopy(value, 0, copyValue, 0, len);
			} break;
			case LONG_ARR: {
				int len = ((long[])value).length;
				copyValue = new long[len];
				System.arraycopy(value, 0, copyValue, 0, len);
			} break;
			case FLOAT_ARR: {
				int len = ((float[])value).length;
				copyValue = new float[len];
				System.arraycopy(value, 0, copyValue, 0, len);
			} break;
			case DOUBLE_ARR: {
				int len = ((double[])value).length;
				copyValue = new double[len];
				System.arraycopy(value, 0, copyValue, 0, len);
			} break;
			case BOOLEAN_MATRIX_2D: {
				boolean[][] oldValue = (boolean[][])value;
				dimX = oldValue.length; dimY = dimX > 0 ? oldValue[0].length : 0;
				boolean[][] newValue = new boolean[dimX][dimY];
				for (int i = 0; i < dimX; i++) {
					System.arraycopy(oldValue[i], 0, newValue[i], 0, dimY);
				}
				copyValue = newValue;
				} break;
			case BYTE_MATRIX_2D: {
				byte[][] oldValue = (byte[][])value;
				dimX = oldValue.length; dimY = dimX > 0 ? oldValue[0].length : 0;
				byte[][] newValue = new byte[dimX][dimY];
				for (int i = 0; i < dimX; i++) {
					System.arraycopy(oldValue[i], 0, newValue[i], 0, dimY);
				}
				copyValue = newValue;
				} break;
			case SHORT_MATRIX_2D: {
				short[][] oldValue = (short[][])value;
				dimX = oldValue.length; dimY = dimX > 0 ? oldValue[0].length : 0;
				short[][] newValue = new short[dimX][dimY];
				for (int i = 0; i < dimX; i++) {
					System.arraycopy(oldValue[i], 0, newValue[i], 0, dimY);
				}
				copyValue = newValue;
				} break;
			case INT_MATRIX_2D: {
				int[][] oldValue = (int[][])value;
				dimX = oldValue.length; dimY = dimX > 0 ? oldValue[0].length : 0;
				int[][] newValue = new int[dimX][dimY];
				for (int i = 0; i < dimX; i++) {
					System.arraycopy(oldValue[i], 0, newValue[i], 0, dimY);
				}
				copyValue = newValue;
				} break;
			case LONG_MATRIX_2D: {
				long[][] oldValue = (long[][])value;
				dimX = oldValue.length; dimY = dimX > 0 ? oldValue[0].length : 0;
				long[][] newValue = new long[dimX][dimY];
				for (int i = 0; i < dimX; i++) {
					System.arraycopy(oldValue[i], 0, newValue[i], 0, dimY);
				}
				copyValue = newValue;
				} break;
			case FLOAT_MATRIX_2D: {
				float[][] oldValue = (float[][])value;
				dimX = oldValue.length; dimY = dimX > 0 ? oldValue[0].length : 0;
				float[][] newValue = new float[dimX][dimY];
				for (int i = 0; i < dimX; i++) {
					System.arraycopy(oldValue[i], 0, newValue[i], 0, dimY);
				}
				copyValue = newValue;
				} break;
			case DOUBLE_MATRIX_2D: {
				double[][] oldValue = (double[][])value;
				dimX = oldValue.length; dimY = dimX > 0 ? oldValue[0].length : 0;
				double[][] newValue = new double[dimX][dimY];
				for (int i = 0; i < dimX; i++) {
					System.arraycopy(oldValue[i], 0, newValue[i], 0, dimY);
				}
				copyValue = newValue;
				} break;
			case BOOLEAN_MATRIX_3D: {
				boolean[][][] oldValue = (boolean[][][])value;
				dimX = oldValue.length; dimY = dimX > 0 ? oldValue[0].length : 0; dimZ = dimY > 0 ? oldValue[0][0].length : 0;
				boolean[][][] newValue = new boolean[dimX][dimY][dimZ];
				for (int i = 0; i < dimX; i++) {
					for (int j = 0; j < dimY; j++) {
						System.arraycopy(oldValue[i][j], 0, newValue[i][j], 0, dimZ);
					}
				}
				copyValue = newValue;
				} break;
			case BYTE_MATRIX_3D: {
				byte[][][] oldValue = (byte[][][])value;
				dimX = oldValue.length; dimY = dimX > 0 ? oldValue[0].length : 0; dimZ = dimY > 0 ? oldValue[0][0].length : 0;
				byte[][][] newValue = new byte[dimX][dimY][dimZ];
				for (int i = 0; i < dimX; i++) {
					for (int j = 0; j < dimY; j++) {
						System.arraycopy(oldValue[i][j], 0, newValue[i][j], 0, dimZ);
					}
				}
				copyValue = newValue;
				} break;
			case SHORT_MATRIX_3D: {
				short[][][] oldValue = (short[][][])value;
				dimX = oldValue.length; dimY = dimX > 0 ? oldValue[0].length : 0; dimZ = dimY > 0 ? oldValue[0][0].length : 0;
				short[][][] newValue = new short[dimX][dimY][dimZ];
				for (int i = 0; i < dimX; i++) {
					for (int j = 0; j < dimY; j++) {
						System.arraycopy(oldValue[i][j], 0, newValue[i][j], 0, dimZ);
					}
				}
				copyValue = newValue;
				} break;
			case INT_MATRIX_3D: {
				int[][][] oldValue = (int[][][])value;
				dimX = oldValue.length; dimY = dimX > 0 ? oldValue[0].length : 0; dimZ = dimY > 0 ? oldValue[0][0].length : 0;
				int[][][] newValue = new int[dimX][dimY][dimZ];
				for (int i = 0; i < dimX; i++) {
					for (int j = 0; j < dimY; j++) {
						System.arraycopy(oldValue[i][j], 0, newValue[i][j], 0, dimZ);
					}
				}
				copyValue = newValue;
				} break;
			case LONG_MATRIX_3D: {
				long[][][] oldValue = (long[][][])value;
				dimX = oldValue.length; dimY = dimX > 0 ? oldValue[0].length : 0; dimZ = dimY > 0 ? oldValue[0][0].length : 0;
				long[][][] newValue = new long[dimX][dimY][dimZ];
				for (int i = 0; i < dimX; i++) {
					for (int j = 0; j < dimY; j++) {
						System.arraycopy(oldValue[i][j], 0, newValue[i][j], 0, dimZ);
					}
				}
				copyValue = newValue;
				} break;
			case FLOAT_MATRIX_3D: {
				float[][][] oldValue = (float[][][])value;
				dimX = oldValue.length; dimY = dimX > 0 ? oldValue[0].length : 0; dimZ = dimY > 0 ? oldValue[0][0].length : 0;
				float[][][] newValue = new float[dimX][dimY][dimZ];
				for (int i = 0; i < dimX; i++) {
					for (int j = 0; j < dimY; j++) {
						System.arraycopy(oldValue[i][j], 0, newValue[i][j], 0, dimZ);
					}
				}
				copyValue = newValue;
				} break;
			case DOUBLE_MATRIX_3D: {
				double[][][] oldValue = (double[][][])value;
				dimX = oldValue.length; dimY = dimX > 0 ? oldValue[0].length : 0; dimZ = dimY > 0 ? oldValue[0][0].length : 0;
				double[][][] newValue = new double[dimX][dimY][dimZ];
				for (int i = 0; i < dimX; i++) {
					for (int j = 0; j < dimY; j++) {
						System.arraycopy(oldValue[i][j], 0, newValue[i][j], 0, dimZ);
					}
				}
				copyValue = newValue;
				} break;
			case BOOLEAN_MATRIX_4D: {
				boolean[][][][] oldValue = (boolean[][][][])value;
				dimX = oldValue.length; dimY = dimX > 0 ? oldValue[0].length : 0; dimZ = dimY > 0 ? oldValue[0][0].length : 0; dimZZ = dimZ > 0 ? oldValue[0][0][0].length : 0;
				boolean[][][][] newValue = new boolean[dimX][dimY][dimZ][dimZZ];
				for (int i = 0; i < dimX; i++) {
					for (int j = 0; j < dimY; j++) {
						for (int k = 0; k < dimZ; k++) {
							System.arraycopy(oldValue[i][j][k], 0, newValue[i][j][k], 0, dimZZ);
						}
					}
				}
				copyValue = newValue;
				} break;
			case BYTE_MATRIX_4D: {
				byte[][][][] oldValue = (byte[][][][])value;
				dimX = oldValue.length; dimY = dimX > 0 ? oldValue[0].length : 0; dimZ = dimY > 0 ? oldValue[0][0].length : 0; dimZZ = dimZ > 0 ? oldValue[0][0][0].length : 0;
				byte[][][][] newValue = new byte[dimX][dimY][dimZ][dimZZ];
				for (int i = 0; i < dimX; i++) {
					for (int j = 0; j < dimY; j++) {
						for (int k = 0; k < dimZ; k++) {
							System.arraycopy(oldValue[i][j][k], 0, newValue[i][j][k], 0, dimZZ);
						}
					}
				}
				copyValue = newValue;
				} break;
			case SHORT_MATRIX_4D: {
				short[][][][] oldValue = (short[][][][])value;
				dimX = oldValue.length; dimY = dimX > 0 ? oldValue[0].length : 0; dimZ = dimY > 0 ? oldValue[0][0].length : 0; dimZZ = dimZ > 0 ? oldValue[0][0][0].length : 0;
				short[][][][] newValue = new short[dimX][dimY][dimZ][dimZZ];
				for (int i = 0; i < dimX; i++) {
					for (int j = 0; j < dimY; j++) {
						for (int k = 0; k < dimZ; k++) {
							System.arraycopy(oldValue[i][j][k], 0, newValue[i][j][k], 0, dimZZ);
						}
					}
				}
				copyValue = newValue;
				} break;
			case INT_MATRIX_4D: {
				int[][][][] oldValue = (int[][][][])value;
				dimX = oldValue.length; dimY = dimX > 0 ? oldValue[0].length : 0; dimZ = dimY > 0 ? oldValue[0][0].length : 0; dimZZ = dimZ > 0 ? oldValue[0][0][0].length : 0;
				int[][][][] newValue = new int[dimX][dimY][dimZ][dimZZ];
				for (int i = 0; i < dimX; i++) {
					for (int j = 0; j < dimY; j++) {
						for (int k = 0; k < dimZ; k++) {
							System.arraycopy(oldValue[i][j][k], 0, newValue[i][j][k], 0, dimZZ);
						}
					}
				}
				copyValue = newValue;
				} break;
			case LONG_MATRIX_4D: {
				long[][][][] oldValue = (long[][][][])value;
				dimX = oldValue.length; dimY = dimX > 0 ? oldValue[0].length : 0; dimZ = dimY > 0 ? oldValue[0][0].length : 0; dimZZ = dimZ > 0 ? oldValue[0][0][0].length : 0;
				long[][][][] newValue = new long[dimX][dimY][dimZ][dimZZ];
				for (int i = 0; i < dimX; i++) {
					for (int j = 0; j < dimY; j++) {
						for (int k = 0; k < dimZ; k++) {
							System.arraycopy(oldValue[i][j][k], 0, newValue[i][j][k], 0, dimZZ);
						}
					}
				}
				copyValue = newValue;
				} break;
			case FLOAT_MATRIX_4D: {
				float[][][][] oldValue = (float[][][][])value;
				dimX = oldValue.length; dimY = dimX > 0 ? oldValue[0].length : 0; dimZ = dimY > 0 ? oldValue[0][0].length : 0; dimZZ = dimZ > 0 ? oldValue[0][0][0].length : 0;
				float[][][][] newValue = new float[dimX][dimY][dimZ][dimZZ];
				for (int i = 0; i < dimX; i++) {
					for (int j = 0; j < dimY; j++) {
						for (int k = 0; k < dimZ; k++) {
							System.arraycopy(oldValue[i][j][k], 0, newValue[i][j][k], 0, dimZZ);
						}
					}
				}
				copyValue = newValue;
				} break;
			case DOUBLE_MATRIX_4D: {
				double[][][][] oldValue = (double[][][][])value;
				dimX = oldValue.length; dimY = dimX > 0 ? oldValue[0].length : 0; dimZ = dimY > 0 ? oldValue[0][0].length : 0; dimZZ = dimZ > 0 ? oldValue[0][0][0].length : 0;
				double[][][][] newValue = new double[dimX][dimY][dimZ][dimZZ];
				for (int i = 0; i < dimX; i++) {
					for (int j = 0; j < dimY; j++) {
						for (int k = 0; k < dimZ; k++) {
							System.arraycopy(oldValue[i][j][k], 0, newValue[i][j][k], 0, dimZZ);
						}
					}
				}
				copyValue = newValue;
				} break;
			default:
				throw new IllegalArgumentException("Serializable type not recognized");
		}
		return (T)copyValue;
	}
}
