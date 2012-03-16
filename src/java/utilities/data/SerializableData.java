/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package utilities.data;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import muscle.core.messaging.serialization.ByteJavaObjectConverter;
import org.acplt.oncrpc.OncRpcException;
import org.acplt.oncrpc.XdrDecodingStream;
import org.acplt.oncrpc.XdrEncodingStream;

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
		} else if (type == SerializableDatatype.JAVA_BYTE_OBJECT && !(value instanceof byte[])) {
			value = new ByteJavaObjectConverter().serialize(value);
			size = sizeOf(value, type);
		} else if (type.getDataClass() != null && !type.getDataClass().isInstance(value)) {
			throw new IllegalArgumentException("Class of value '" + value.getClass() + "' does not match datatype '" + type + "'");
		}
		this.type = type;
		this.value = value;
		this.size = size;
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
	
	/**
	 * Parses a SerializableData from an XdrDecodingStream.
	 * @param xdrIn an initialized XdrDecodingStream
	 * @return a new SerializableData
	 * @throws OncRpcException if the supplied data is not correct
	 * @throws IOException if the connection fails
	 */
	public static SerializableData parseXdrData(XdrDecodingStream xdrIn) throws OncRpcException, IOException {
		int typeNum = xdrIn.xdrDecodeInt();
		
		if (typeNum < 0 || typeNum >= datatypes.length) {
			throw new OncRpcException("Datatype with number " + typeNum + " not recognized");
		}
		
		SerializableDatatype type = datatypes[typeNum];
		Serializable value;
		int length = 0;
		
		switch (type.typeOf()) {
			case NULL:
				value = null;
				break;
			case STRING_MAP:
				length = xdrIn.xdrDecodeInt();
				HashMap<String,SerializableData> xdrMap = new HashMap<String,SerializableData>(length*3/2);
				
				for (int i = 0; i < length; i++) {
					xdrMap.put(xdrIn.xdrDecodeString(), SerializableData.parseXdrData(xdrIn));
				}
				value = xdrMap;
				break;
			case COLLECTION:
				length = xdrIn.xdrDecodeInt();
				ArrayList<SerializableData> xdrList = new ArrayList<SerializableData>(length);
				
				for (int i = 0; i < length; i++) {
					xdrList.add(SerializableData.parseXdrData(xdrIn));
				}
				value = xdrList;
				break;
			case STRING:
				value = xdrIn.xdrDecodeString();
				break;
			case STRING_ARR:
				value = xdrIn.xdrDecodeStringVector();
				break;
			case BOOLEAN:
				value = xdrIn.xdrDecodeBoolean();
				break;
			case BOOLEAN_ARR:
				value = xdrIn.xdrDecodeBooleanVector();
				length = ((boolean[])value).length;
				break;
			case BYTE:
				value = xdrIn.xdrDecodeByte();
				break;
			case BYTE_ARR:
				value = xdrIn.xdrDecodeByteVector();
				length = ((byte[])value).length;
				break;
			case SHORT:
				value = xdrIn.xdrDecodeShort();
				break;
			case SHORT_ARR:
				value = xdrIn.xdrDecodeShortVector();
				length = ((short[])value).length;
				break;
			case INT:
				value = xdrIn.xdrDecodeInt();
				break;
			case INT_ARR:
				value = xdrIn.xdrDecodeIntVector();
				length = ((int[])value).length;
				break;
			case LONG:
				value = xdrIn.xdrDecodeLong();
				break;
			case LONG_ARR:
				value = xdrIn.xdrDecodeLongVector();
				length = ((long[])value).length;
				break;
			case FLOAT:
				value = xdrIn.xdrDecodeFloat();
				break;
			case FLOAT_ARR:
				value = xdrIn.xdrDecodeFloatVector();
				length = ((float[])value).length;
				break;
			case DOUBLE:
				value = xdrIn.xdrDecodeDouble();
				break;
			case DOUBLE_ARR:
				value = xdrIn.xdrDecodeDoubleVector();
				length = ((double[])value).length;
				break;
			default:
				throw new OncRpcException("Datatype " + type + " not recognized");
		}
		
		int size = sizeOf(value, type);
		
		if (type.isMatrix()) {
			int dimX, dimY, dimZ, dimZZ;
			dimX = xdrIn.xdrDecodeInt();
			dimY = type.isMatrix2D() ? length / dimX : xdrIn.xdrDecodeInt();
			dimZ = type.isMatrix4D() ? xdrIn.xdrDecodeInt() : length / (dimX*dimY);
			dimZZ = type.isMatrix4D() ? length / (dimX*dimY*dimZ) : 1;
		
			value = arrayToMatrix(value, type, dimX, dimY, dimZ, dimZZ);
		}
		
		return new SerializableData(value, type, size);
	}
	
	/**
	 * Encodes the data over an XdrEncodingStream.
	 * @param xdrOut an initialized XdrEncodingStream, it is not flushed.
	 * @throws OncRpcException if the supplied data is not correct
	 * @throws IOException if the connection fails
	 */
	public void encodeXdrData(XdrEncodingStream xdrOut) throws OncRpcException, IOException {
		xdrOut.xdrEncodeInt(type.ordinal());
		
		Object newValue = matrixToArray();
		
		switch (type.typeOf()) {
			case NULL:
				break;
			case STRING_MAP:				
				Map<String,SerializableData> xdrMap = (Map<String,SerializableData>)newValue;
				xdrOut.xdrEncodeInt(xdrMap.size());

				for (Map.Entry<String,SerializableData> entry : xdrMap.entrySet()) {
					xdrOut.xdrEncodeString(entry.getKey());
					entry.getValue().encodeXdrData(xdrOut);
				}
				break;
			case COLLECTION:
				List<SerializableData> xdrList = (List<SerializableData>)newValue;
				xdrOut.xdrEncodeInt(xdrList.size());
				
				for (SerializableData data : xdrList) {
					data.encodeXdrData(xdrOut);
				}
				break;
			case STRING:
				xdrOut.xdrEncodeString((String)newValue);
				break;
			case STRING_ARR:
				xdrOut.xdrEncodeStringVector((String[])newValue);
				break;
			case BOOLEAN:
				xdrOut.xdrEncodeBoolean((Boolean)newValue);
				break;
			case BOOLEAN_ARR:
				xdrOut.xdrEncodeBooleanVector((boolean[])newValue);
				break;
			case BYTE:
				xdrOut.xdrEncodeByte((Byte)newValue);
				break;
			case BYTE_ARR:
				xdrOut.xdrEncodeByteVector((byte[])newValue);
				break;
			case SHORT:
				xdrOut.xdrEncodeShort((Short)newValue);
				break;
			case SHORT_ARR:
				xdrOut.xdrEncodeShortVector((short[])newValue);
				break;
			case INT:
				xdrOut.xdrEncodeInt((Integer)newValue);
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
			case DOUBLE:
				xdrOut.xdrEncodeDouble((Double)newValue);
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
		if (type.isArray() || type.isMatrix() && type != SerializableDatatype.STRING_ARR) {
			return deepSizeOf(value, type);
		}
		else switch (type.typeOf()) {
			case NULL:
				break;
			case STRING_MAP:				
				Map<String,SerializableData> xdrMap = (Map<String,SerializableData>)value;
				
				for (Map.Entry<String,SerializableData> entry : xdrMap.entrySet()) {
					size += entry.getKey().length()*2+4;
					size += 4;
					size += sizeOf(entry.getValue().value, entry.getValue().type);
				}
				break;
			case COLLECTION:
				List<SerializableData> xdrList = (List<SerializableData>)value;
				
				for (SerializableData data : xdrList) {
					size += sizeOf(data.value, data.type);
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
		if (value instanceof Object[]) {
			return ((Object[])value).length * lengthOfMatrix(((Object[])value)[0], type);
		}
		else {
			switch (type.typeOf()) {
				case BOOLEAN_ARR:
					return ((boolean[])value).length;
				case BYTE_ARR:
					return ((byte[])value).length;
				case SHORT_ARR:
					return ((short[])value).length;
				case INT_ARR:
					return ((int[])value).length;
				case LONG_ARR:
					return ((long[])value).length;
				case FLOAT_ARR:
					return ((float[])value).length;
				case DOUBLE_ARR:
					return ((double[])value).length;
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
		switch (type) {
			case BOOLEAN_MATRIX_2D: {
				boolean[][] newValue = new boolean[dimX][dimY];
				for (int i = 0; i < dimX; i++) {
					System.arraycopy(value, i*dimY, newValue[i], 0, dimY);
				}
				value = newValue; }
				break;
			case BYTE_MATRIX_2D: {
				byte[][] newValue = new byte[dimX][dimY];
				for (int i = 0; i < dimX; i++) {
					System.arraycopy(value, i*dimY, newValue[i], 0, dimY);
				}
				value = newValue; }
				break;
			case SHORT_MATRIX_2D: {
				short[][] newValue = new short[dimX][dimY];
				for (int i = 0; i < dimX; i++) {
					System.arraycopy(value, i*dimY, newValue[i], 0, dimY);
				}
				value = newValue; }
				break;
			case INT_MATRIX_2D: {
				int[][] newValue = new int[dimX][dimY];
				for (int i = 0; i < dimX; i++) {
					System.arraycopy(value, i*dimY, newValue[i], 0, dimY);
				}
				value = newValue; }
				break;
			case LONG_MATRIX_2D: {
				long[][] newValue = new long[dimX][dimY];
				for (int i = 0; i < dimX; i++) {
					System.arraycopy(value, i*dimY, newValue[i], 0, dimY);
				}
				value = newValue; }
				break;
			case FLOAT_MATRIX_2D: {
				float[][] newValue = new float[dimX][dimY];
				for (int i = 0; i < dimX; i++) {
					System.arraycopy(value, i*dimY, newValue[i], 0, dimY);
				}
				value = newValue; }
				break;
			case DOUBLE_MATRIX_2D: {
				double[][] newValue = new double[dimX][dimY];
				for (int i = 0; i < dimX; i++) {
					System.arraycopy(value, i*dimY, newValue[i], 0, dimY);
				}
				value = newValue; }
				break;
			case BOOLEAN_MATRIX_3D: {
				boolean[][][] newValue = new boolean[dimX][dimY][dimZ];
				for (int i = 0; i < dimX; i++) {
					for (int j = 0; j < dimY; j++) {
						System.arraycopy(value, count, newValue[i][j], 0, dimZ);
						count += dimZ;
					}
				}
				value = newValue; }
				break;
			case BYTE_MATRIX_3D: {
				byte[][][] newValue = new byte[dimX][dimY][dimZ];
				for (int i = 0; i < dimX; i++) {
					for (int j = 0; j < dimY; j++) {
						System.arraycopy(value, count, newValue[i][j], 0, dimZ);
						count += dimZ;
					}
				}
				value = newValue; }
				break;
			case SHORT_MATRIX_3D: {
				short[][][] newValue = new short[dimX][dimY][dimZ];
				for (int i = 0; i < dimX; i++) {
					for (int j = 0; j < dimY; j++) {
						System.arraycopy(value, count, newValue[i][j], 0, dimZ);
						count += dimZ;
					}
				}
				value = newValue; }
				break;
			case INT_MATRIX_3D: {
				int[][][] newValue = new int[dimX][dimY][dimZ];
				for (int i = 0; i < dimX; i++) {
					for (int j = 0; j < dimY; j++) {
						System.arraycopy(value, count, newValue[i][j], 0, dimZ);
						count += dimZ;
					}
				}
				value = newValue; }
				break;
			case LONG_MATRIX_3D: {
				long[][][] newValue = new long[dimX][dimY][dimZ];
				for (int i = 0; i < dimX; i++) {
					for (int j = 0; j < dimY; j++) {
						System.arraycopy(value, count, newValue[i][j], 0, dimZ);
						count += dimZ;
					}
				}
				value = newValue; }
				break;
			case FLOAT_MATRIX_3D: {
				float[][][] newValue = new float[dimX][dimY][dimZ];
				for (int i = 0; i < dimX; i++) {
					for (int j = 0; j < dimY; j++) {
						System.arraycopy(value, count, newValue[i][j], 0, dimZ);
						count += dimZ;
					}
				}
				value = newValue; }
				break;
			case DOUBLE_MATRIX_3D: {
				double[][][] newValue = new double[dimX][dimY][dimZ];
				for (int i = 0; i < dimX; i++) {
					for (int j = 0; j < dimY; j++) {
						System.arraycopy(value, count, newValue[i][j], 0, dimZ);
						count += dimZ;
					}
				}
				value = newValue; }
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
				value = newValue; }
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
				value = newValue; }
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
				value = newValue; }
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
				value = newValue; }
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
				value = newValue; }
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
				value = newValue; }
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
				value = newValue; }
				break;
		}
		return value;
	}
	
	/**
	 * Gets the current data. If the datatype is a matrix, it converts it to an array first.
	 * @throws ClassCastException if the data is not a matrix of the correct type.
	 */
	private Serializable matrixToArray() {
		Serializable newValue = value;
		
		if (type.isMatrix()) {
			int dimX, dimY, dimZ, dimZZ, count = 0;
			switch (type) {
				case BOOLEAN_MATRIX_2D: {
					boolean[][] oldValue = (boolean[][])value;
					dimX = oldValue.length; dimY = oldValue[0].length;
					newValue = new boolean[dimX*dimY];
					for (int i = 0; i < dimX; i++) {
						System.arraycopy(oldValue[i], 0, newValue, i*dimY, dimY);
					}
					} break;
				case BYTE_MATRIX_2D: {
					byte[][] oldValue = (byte[][])value;
					dimX = oldValue.length; dimY = oldValue[0].length;
					newValue = new byte[dimX*dimY];
					for (int i = 0; i < dimX; i++) {
						System.arraycopy(oldValue[i], 0, newValue, i*dimY, dimY);
					}
					} break;
				case SHORT_MATRIX_2D: {
					short[][] oldValue = (short[][])value;
					dimX = oldValue.length; dimY = oldValue[0].length;
					newValue = new short[dimX*dimY];
					for (int i = 0; i < dimX; i++) {
						System.arraycopy(oldValue[i], 0, newValue, i*dimY, dimY);
					}
					} break;
				case INT_MATRIX_2D: {
					int[][] oldValue = (int[][])value;
					dimX = oldValue.length; dimY = oldValue[0].length;
					newValue = new int[dimX*dimY];
					for (int i = 0; i < dimX; i++) {
						System.arraycopy(oldValue[i], 0, newValue, i*dimY, dimY);
					}
					} break;
				case LONG_MATRIX_2D: {
					long[][] oldValue = (long[][])value;
					dimX = oldValue.length; dimY = oldValue[0].length;
					newValue = new long[dimX*dimY];
					for (int i = 0; i < dimX; i++) {
						System.arraycopy(oldValue[i], 0, newValue, i*dimY, dimY);
					}
					} break;
				case FLOAT_MATRIX_2D: {
					float[][] oldValue = (float[][])value;
					dimX = oldValue.length; dimY = oldValue[0].length;
					newValue = new float[dimX*dimY];
					for (int i = 0; i < dimX; i++) {
						System.arraycopy(oldValue[i], 0, newValue, i*dimY, dimY);
					}
					} break;
				case DOUBLE_MATRIX_2D: {
					double[][] oldValue = (double[][])value;
					dimX = oldValue.length; dimY = oldValue[0].length;
					newValue = new double[dimX*dimY];
					for (int i = 0; i < dimX; i++) {
						System.arraycopy(oldValue[i], 0, newValue, i*dimY, dimY);
					}
					} break;
				case BOOLEAN_MATRIX_3D: {
					boolean[][][] oldValue = (boolean[][][])value;
					dimX = oldValue.length; dimY = oldValue[0].length; dimZ = oldValue[0][0].length;
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
					dimX = oldValue.length; dimY = oldValue[0].length; dimZ = oldValue[0][0].length;
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
					dimX = oldValue.length; dimY = oldValue[0].length; dimZ = oldValue[0][0].length;
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
					dimX = oldValue.length; dimY = oldValue[0].length; dimZ = oldValue[0][0].length;
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
					dimX = oldValue.length; dimY = oldValue[0].length; dimZ = oldValue[0][0].length;
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
					dimX = oldValue.length; dimY = oldValue[0].length; dimZ = oldValue[0][0].length;
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
					dimX = oldValue.length; dimY = oldValue[0].length; dimZ = oldValue[0][0].length;
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
					dimX = oldValue.length; dimY = oldValue[0].length; dimZ = oldValue[0][0].length; dimZZ = oldValue[0][0][0].length;
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
					dimX = oldValue.length; dimY = oldValue[0].length; dimZ = oldValue[0][0].length; dimZZ = oldValue[0][0][0].length;
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
					dimX = oldValue.length; dimY = oldValue[0].length; dimZ = oldValue[0][0].length; dimZZ = oldValue[0][0][0].length;
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
					dimX = oldValue.length; dimY = oldValue[0].length; dimZ = oldValue[0][0].length; dimZZ = oldValue[0][0][0].length;
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
					dimX = oldValue.length; dimY = oldValue[0].length; dimZ = oldValue[0][0].length; dimZZ = oldValue[0][0][0].length;
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
					dimX = oldValue.length; dimY = oldValue[0].length; dimZ = oldValue[0][0].length; dimZZ = oldValue[0][0][0].length;
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
					dimX = oldValue.length; dimY = oldValue[0].length; dimZ = oldValue[0][0].length; dimZZ = oldValue[0][0][0].length;
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
	
	public static Serializable createIndependent(Serializable value) {
		Serializable copyValue;
		int dimX, dimY, dimZ, dimZZ;
		SerializableDatatype type = inferDatatype(value);
		switch (type) {
			// Immutable
			case NULL: case STRING: case BOOLEAN: case BYTE: case SHORT: case INT: case LONG: case FLOAT: case DOUBLE:
				copyValue = value;
				break;
			case STRING_MAP: {
				int len = ((Map)value).size();
				copyValue = new HashMap<String,Serializable>(len*3/2);
				for (Map.Entry<String,?> entry : ((Map<String,?>)value).entrySet()) {
					((Map<String,Serializable>)copyValue).put(entry.getKey(),createIndependent((Serializable)entry.getValue()));
				}
			} break;
			case COLLECTION: {
				int len = ((Collection)value).size();
				copyValue = new ArrayList(len);
				for (Object o : ((Collection)value)) {
					((Collection)copyValue).add(createIndependent((Serializable)o));
				}
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
				int len = ((boolean[])value).length;
				copyValue = new boolean[len];
				System.arraycopy(value, 0, copyValue, 0, len);
			} break;
			case SHORT_ARR: {
				int len = ((boolean[])value).length;
				copyValue = new boolean[len];
				System.arraycopy(value, 0, copyValue, 0, len);
			} break;
			case INT_ARR: {
				int len = ((boolean[])value).length;
				copyValue = new boolean[len];
				System.arraycopy(value, 0, copyValue, 0, len);
			} break;
			case LONG_ARR: {
				int len = ((boolean[])value).length;
				copyValue = new boolean[len];
				System.arraycopy(value, 0, copyValue, 0, len);
			} break;
			case FLOAT_ARR: {
				int len = ((boolean[])value).length;
				copyValue = new boolean[len];
				System.arraycopy(value, 0, copyValue, 0, len);
			} break;
			case DOUBLE_ARR: {
				int len = ((boolean[])value).length;
				copyValue = new boolean[len];
				System.arraycopy(value, 0, copyValue, 0, len);
			} break;
			case BOOLEAN_MATRIX_2D: {
				boolean[][] oldValue = (boolean[][])value;
				dimX = oldValue.length; dimY = oldValue[0].length;
				boolean[][] newValue = new boolean[dimX][dimY];
				for (int i = 0; i < dimX; i++) {
					System.arraycopy(oldValue[i], 0, newValue[i], 0, dimY);
				}
				copyValue = newValue;
				} break;
			case BYTE_MATRIX_2D: {
				byte[][] oldValue = (byte[][])value;
				dimX = oldValue.length; dimY = oldValue[0].length;
				byte[][] newValue = new byte[dimX][dimY];
				for (int i = 0; i < dimX; i++) {
					System.arraycopy(oldValue[i], 0, newValue[i], 0, dimY);
				}
				copyValue = newValue;
				} break;
			case SHORT_MATRIX_2D: {
				short[][] oldValue = (short[][])value;
				dimX = oldValue.length; dimY = oldValue[0].length;
				short[][] newValue = new short[dimX][dimY];
				for (int i = 0; i < dimX; i++) {
					System.arraycopy(oldValue[i], 0, newValue[i], 0, dimY);
				}
				copyValue = newValue;
				} break;
			case INT_MATRIX_2D: {
				int[][] oldValue = (int[][])value;
				dimX = oldValue.length; dimY = oldValue[0].length;
				int[][] newValue = new int[dimX][dimY];
				for (int i = 0; i < dimX; i++) {
					System.arraycopy(oldValue[i], 0, newValue[i], 0, dimY);
				}
				copyValue = newValue;
				} break;
			case LONG_MATRIX_2D: {
				long[][] oldValue = (long[][])value;
				dimX = oldValue.length; dimY = oldValue[0].length;
				long[][] newValue = new long[dimX][dimY];
				for (int i = 0; i < dimX; i++) {
					System.arraycopy(oldValue[i], 0, newValue[i], 0, dimY);
				}
				copyValue = newValue;
				} break;
			case FLOAT_MATRIX_2D: {
				float[][] oldValue = (float[][])value;
				dimX = oldValue.length; dimY = oldValue[0].length;
				float[][] newValue = new float[dimX][dimY];
				for (int i = 0; i < dimX; i++) {
					System.arraycopy(oldValue[i], 0, newValue[i], 0, dimY);
				}
				copyValue = newValue;
				} break;
			case DOUBLE_MATRIX_2D: {
				double[][] oldValue = (double[][])value;
				dimX = oldValue.length; dimY = oldValue[0].length;
				double[][] newValue = new double[dimX][dimY];
				for (int i = 0; i < dimX; i++) {
					System.arraycopy(oldValue[i], 0, newValue[i], 0, dimY);
				}
				copyValue = newValue;
				} break;
			case BOOLEAN_MATRIX_3D: {
				boolean[][][] oldValue = (boolean[][][])value;
				dimX = oldValue.length; dimY = oldValue[0].length; dimZ = oldValue[0][0].length;
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
				dimX = oldValue.length; dimY = oldValue[0].length; dimZ = oldValue[0][0].length;
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
				dimX = oldValue.length; dimY = oldValue[0].length; dimZ = oldValue[0][0].length;
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
				dimX = oldValue.length; dimY = oldValue[0].length; dimZ = oldValue[0][0].length;
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
				dimX = oldValue.length; dimY = oldValue[0].length; dimZ = oldValue[0][0].length;
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
				dimX = oldValue.length; dimY = oldValue[0].length; dimZ = oldValue[0][0].length;
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
				dimX = oldValue.length; dimY = oldValue[0].length; dimZ = oldValue[0][0].length;
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
				dimX = oldValue.length; dimY = oldValue[0].length; dimZ = oldValue[0][0].length; dimZZ = oldValue[0][0][0].length;
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
				dimX = oldValue.length; dimY = oldValue[0].length; dimZ = oldValue[0][0].length; dimZZ = oldValue[0][0][0].length;
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
				dimX = oldValue.length; dimY = oldValue[0].length; dimZ = oldValue[0][0].length; dimZZ = oldValue[0][0][0].length;
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
				dimX = oldValue.length; dimY = oldValue[0].length; dimZ = oldValue[0][0].length; dimZZ = oldValue[0][0][0].length;
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
				dimX = oldValue.length; dimY = oldValue[0].length; dimZ = oldValue[0][0].length; dimZZ = oldValue[0][0][0].length;
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
				dimX = oldValue.length; dimY = oldValue[0].length; dimZ = oldValue[0][0].length; dimZZ = oldValue[0][0][0].length;
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
				dimX = oldValue.length; dimY = oldValue[0].length; dimZ = oldValue[0][0].length; dimZZ = oldValue[0][0][0].length;
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
		return copyValue;
	}
}
