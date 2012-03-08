/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package utilities.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import muscle.core.messaging.serialization.ByteJavaObjectConverter;
import org.acplt.oncrpc.OncRpcException;
import org.acplt.oncrpc.XdrDecodingStream;
import org.acplt.oncrpc.XdrEncodingStream;

/**
 *
 * @author Joris Borgdorff
 */
public class SerializableData {
	private final SerializableDatatype type;
	private final Object value;
	private final static SerializableDatatype[] datatypes = SerializableDatatype.values();
	
	public SerializableData(Object value, SerializableDatatype type) {
		this.type = type;
		this.value = value;
	}
	
	public static SerializableData valueOf(Object value) {
		if (value == null) {
			return new SerializableData(null, SerializableDatatype.NULL);
		}
		for (SerializableDatatype type : SerializableDatatype.values()) {
			if (type.getDataClass() != null && type.getDataClass().isInstance(value)) {
				return new SerializableData(value, type);
			}
		}
		return new SerializableData(new ByteJavaObjectConverter().serialize(value),SerializableDatatype.JAVA_BYTE_OBJECT);
	}
	
	public Object getValue() {
		if (type == SerializableDatatype.JAVA_BYTE_OBJECT) {
			return new ByteJavaObjectConverter().deserialize((byte[])value);
		}
		else {
			return value;
		}
	}

	public SerializableDatatype getType() {
		return type;
	}
	
	public static SerializableData parseXdrData(XdrDecodingStream xdrIn) throws OncRpcException, IOException {
		int typeNum = xdrIn.xdrDecodeInt();
		
		if (typeNum < 0 || typeNum >= datatypes.length) {
			throw new OncRpcException("Datatype with number " + typeNum + " not recognized");
		}
		
		SerializableDatatype type = datatypes[typeNum];
		Object value;
		int size = 0;
		
		switch (type) {
			case NULL:
				value = null;
				break;
			case STRING_MAP:
				size = xdrIn.xdrDecodeInt();
				Map<String,SerializableData> xdrMap = new HashMap<String,SerializableData>(size*3/2);
				
				for (int i = 0; i < size; i++) {
					xdrMap.put(xdrIn.xdrDecodeString(), SerializableData.parseXdrData(xdrIn));
				}
				value = xdrMap;
				break;
			case COLLECTION:
				size = xdrIn.xdrDecodeInt();
				List<SerializableData> xdrList = new ArrayList<SerializableData>(size);
				
				for (int i = 0; i < size; i++) {
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
			case BOOLEAN_MATRIX_2D:
			case BOOLEAN_MATRIX_3D:
			case BOOLEAN_MATRIX_4D:
				value = xdrIn.xdrDecodeBooleanVector();
				size = ((boolean[])value).length;
				break;
			case BYTE:
				value = xdrIn.xdrDecodeByte();
				break;
			case JAVA_BYTE_OBJECT:
			case BYTE_ARR:
			case BYTE_MATRIX_2D:
			case BYTE_MATRIX_3D:
			case BYTE_MATRIX_4D:
				value = xdrIn.xdrDecodeByteVector();
				size = ((byte[])value).length;
				break;
			case SHORT:
				value = xdrIn.xdrDecodeShort();
				break;
			case SHORT_ARR:
			case SHORT_MATRIX_2D:
			case SHORT_MATRIX_3D:
			case SHORT_MATRIX_4D:
				value = xdrIn.xdrDecodeShortVector();
				size = ((short[])value).length;
				break;
			case INT:
				value = xdrIn.xdrDecodeInt();
				break;
			case INT_ARR:
			case INT_MATRIX_2D:
			case INT_MATRIX_3D:
			case INT_MATRIX_4D:
				value = xdrIn.xdrDecodeIntVector();
				size = ((int[])value).length;
				break;
			case LONG:
				value = xdrIn.xdrDecodeLong();
				break;
			case LONG_ARR:
			case LONG_MATRIX_2D:
			case LONG_MATRIX_3D:
			case LONG_MATRIX_4D:
				value = xdrIn.xdrDecodeLongVector();
				size = ((long[])value).length;
				break;
			case FLOAT:
				value = xdrIn.xdrDecodeFloat();
				break;
			case FLOAT_ARR:
			case FLOAT_MATRIX_2D:
			case FLOAT_MATRIX_3D:
			case FLOAT_MATRIX_4D:
				value = xdrIn.xdrDecodeFloatVector();
				size = ((float[])value).length;
				break;
			case DOUBLE:
				value = xdrIn.xdrDecodeDouble();
				break;
			case DOUBLE_ARR:
			case DOUBLE_MATRIX_2D:
			case DOUBLE_MATRIX_3D:
			case DOUBLE_MATRIX_4D:
				value = xdrIn.xdrDecodeDoubleVector();
				size = ((double[])value).length;
				break;
			default:
				throw new OncRpcException("Datatype " + type + " not recognized");
		}
		
		if (type.isMatrix()) {
			int dimX, dimY, dimZ, dimZZ;
			dimX = xdrIn.xdrDecodeInt();
			dimY = type.isMatrix2D() ? size / dimX : xdrIn.xdrDecodeInt();
			dimZ = type.isMatrix4D() ? xdrIn.xdrDecodeInt() : size / (dimX*dimY);
			dimZZ = type.isMatrix4D() ? size / (dimX*dimY*dimZ) : 1;
		
			value = arrayToMatrix(value, type, dimX, dimY, dimZ, dimZZ);
		}
		
		return new SerializableData(value, type);
	}
	
	public void encodeXdrData(XdrEncodingStream xdrOut) throws OncRpcException, IOException {
		xdrOut.xdrEncodeInt(type.ordinal());
		
		Object newValue = matrixToArray();
		
		switch (type) {
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
			case BOOLEAN_MATRIX_2D:
			case BOOLEAN_MATRIX_3D:
			case BOOLEAN_MATRIX_4D:
				xdrOut.xdrEncodeBooleanVector((boolean[])newValue);
				break;
			case BYTE:
				xdrOut.xdrEncodeByte((Byte)newValue);
				break;
			case BYTE_ARR:
			case BYTE_MATRIX_2D:
			case BYTE_MATRIX_3D:
			case BYTE_MATRIX_4D:
			case JAVA_BYTE_OBJECT:
				xdrOut.xdrEncodeByteVector((byte[])newValue);
				break;
			case SHORT:
				xdrOut.xdrEncodeShort((Short)newValue);
				break;
			case SHORT_ARR:
			case SHORT_MATRIX_2D:
			case SHORT_MATRIX_3D:
			case SHORT_MATRIX_4D:
				xdrOut.xdrEncodeShortVector((short[])newValue);
				break;
			case INT:
				xdrOut.xdrEncodeInt((Integer)newValue);
				break;
			case INT_ARR:
			case INT_MATRIX_2D:
			case INT_MATRIX_3D:
			case INT_MATRIX_4D:
				xdrOut.xdrEncodeIntVector((int[])newValue);
				break;
			case LONG:
				xdrOut.xdrEncodeLong((Long)newValue);
				break;
			case LONG_ARR:
			case LONG_MATRIX_2D:
			case LONG_MATRIX_3D:
			case LONG_MATRIX_4D:
				xdrOut.xdrEncodeLongVector((long[])newValue);
				break;
			case FLOAT:
				xdrOut.xdrEncodeFloat((Float)newValue);
				break;
			case FLOAT_ARR:
			case FLOAT_MATRIX_2D:
			case FLOAT_MATRIX_3D:
			case FLOAT_MATRIX_4D:
				xdrOut.xdrEncodeFloatVector((float[])newValue);
				break;
			case DOUBLE:
				xdrOut.xdrEncodeDouble((Double)newValue);
				break;
			case DOUBLE_ARR:
			case DOUBLE_MATRIX_2D:
			case DOUBLE_MATRIX_3D:
			case DOUBLE_MATRIX_4D:
				xdrOut.xdrEncodeDoubleVector((double[])newValue);
				break;
			default:
				throw new OncRpcException("Datatype " + type + " not recognized");
		}
	}
	
	private static Object arrayToMatrix(Object value, SerializableDatatype type, int dimX, int dimY, int dimZ, int dimZZ) {
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
						System.arraycopy(value, i*dimY*dimZ+j*dimZ, newValue[i][j], 0, dimZ);
					}
				}
				value = newValue; }
				break;
			case BYTE_MATRIX_3D: {
				byte[][][] newValue = new byte[dimX][dimY][dimZ];
				for (int i = 0; i < dimX; i++) {
					for (int j = 0; j < dimY; j++) {
						System.arraycopy(value, i*dimY*dimZ+j*dimZ, newValue[i][j], 0, dimZ);
					}
				}
				value = newValue; }
				break;
			case SHORT_MATRIX_3D: {
				short[][][] newValue = new short[dimX][dimY][dimZ];
				for (int i = 0; i < dimX; i++) {
					for (int j = 0; j < dimY; j++) {
						System.arraycopy(value, i*dimY*dimZ+j*dimZ, newValue[i][j], 0, dimZ);
					}
				}
				value = newValue; }
				break;
			case INT_MATRIX_3D: {
				int[][][] newValue = new int[dimX][dimY][dimZ];
				for (int i = 0; i < dimX; i++) {
					for (int j = 0; j < dimY; j++) {
						System.arraycopy(value, i*dimY*dimZ+j*dimZ, newValue[i][j], 0, dimZ);
					}
				}
				value = newValue; }
				break;
			case LONG_MATRIX_3D: {
				long[][][] newValue = new long[dimX][dimY][dimZ];
				for (int i = 0; i < dimX; i++) {
					for (int j = 0; j < dimY; j++) {
						System.arraycopy(value, i*dimY*dimZ+j*dimZ, newValue[i][j], 0, dimZ);
					}
				}
				value = newValue; }
				break;
			case FLOAT_MATRIX_3D: {
				float[][][] newValue = new float[dimX][dimY][dimZ];
				for (int i = 0; i < dimX; i++) {
					for (int j = 0; j < dimY; j++) {
						System.arraycopy(value, i*dimY*dimZ+j*dimZ, newValue[i][j], 0, dimZ);
					}
				}
				value = newValue; }
				break;
			case DOUBLE_MATRIX_3D: {
				double[][][] newValue = new double[dimX][dimY][dimZ];
				for (int i = 0; i < dimX; i++) {
					for (int j = 0; j < dimY; j++) {
						System.arraycopy(value, i*dimY*dimZ+j*dimZ, newValue[i][j], 0, dimZ);
					}
				}
				value = newValue; }
				break;
			case BOOLEAN_MATRIX_4D: {
				boolean[][][][] newValue = new boolean[dimX][dimY][dimZ][dimZZ];
				for (int i = 0; i < dimX; i++) {
					for (int j = 0; j < dimY; j++) {
						for (int k = 0; k < dimZ; k++) {
							System.arraycopy(value, i*dimY*dimZ*dimZZ+j*dimZ*dimZZ+k*dimZZ, newValue[i][j][k], 0, dimZZ);
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
							System.arraycopy(value, i*dimY*dimZ*dimZZ+j*dimZ*dimZZ+k*dimZZ, newValue[i][j][k], 0, dimZZ);
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
							System.arraycopy(value, i*dimY*dimZ*dimZZ+j*dimZ*dimZZ+k*dimZZ, newValue[i][j][k], 0, dimZZ);
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
							System.arraycopy(value, i*dimY*dimZ*dimZZ+j*dimZ*dimZZ+k*dimZZ, newValue[i][j][k], 0, dimZZ);
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
							System.arraycopy(value, i*dimY*dimZ*dimZZ+j*dimZ*dimZZ+k*dimZZ, newValue[i][j][k], 0, dimZZ);
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
							System.arraycopy(value, i*dimY*dimZ*dimZZ+j*dimZ*dimZZ+k*dimZZ, newValue[i][j][k], 0, dimZZ);
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
							System.arraycopy(value, i*dimY*dimZ*dimZZ+j*dimZ*dimZZ+k*dimZZ, newValue[i][j][k], 0, dimZZ);
						}
					}
				}
				value = newValue; }
				break;
		}
		return value;
	}
	
	private Object matrixToArray() {
		Object newValue = value;
		
		if (type.isMatrix()) {
			int dimX, dimY, dimZ, dimZZ;
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
							System.arraycopy(oldValue[i][j], 0, newValue, i*dimY*dimZ+j*dimZ, dimZ);
						}
					}
					} break;
				case BYTE_MATRIX_3D: {
					byte[][][] oldValue = (byte[][][])value;
					dimX = oldValue.length; dimY = oldValue[0].length; dimZ = oldValue[0][0].length;
					newValue = new byte[dimX*dimY*dimZ];
					for (int i = 0; i < dimX; i++) {
						for (int j = 0; j < dimY; j++) {
							System.arraycopy(oldValue[i][j], 0, newValue, i*dimY*dimZ+j*dimZ, dimZ);
						}
					}
					} break;
				case SHORT_MATRIX_3D: {
					short[][][] oldValue = (short[][][])value;
					dimX = oldValue.length; dimY = oldValue[0].length; dimZ = oldValue[0][0].length;
					newValue = new short[dimX*dimY*dimZ];
					for (int i = 0; i < dimX; i++) {
						for (int j = 0; j < dimY; j++) {
							System.arraycopy(oldValue[i][j], 0, newValue, i*dimY*dimZ+j*dimZ, dimZ);
						}
					}
					} break;
				case INT_MATRIX_3D: {
					int[][][] oldValue = (int[][][])value;
					dimX = oldValue.length; dimY = oldValue[0].length; dimZ = oldValue[0][0].length;
					newValue = new int[dimX*dimY*dimZ];
					for (int i = 0; i < dimX; i++) {
						for (int j = 0; j < dimY; j++) {
							System.arraycopy(oldValue[i][j], 0, newValue, i*dimY*dimZ+j*dimZ, dimZ);
						}
					}
					} break;
				case LONG_MATRIX_3D: {
					long[][][] oldValue = (long[][][])value;
					dimX = oldValue.length; dimY = oldValue[0].length; dimZ = oldValue[0][0].length;
					newValue = new long[dimX*dimY*dimZ];
					for (int i = 0; i < dimX; i++) {
						for (int j = 0; j < dimY; j++) {
							System.arraycopy(oldValue[i][j], 0, newValue, i*dimY*dimZ+j*dimZ, dimZ);
						}
					}
					} break;
				case FLOAT_MATRIX_3D: {
					float[][][] oldValue = (float[][][])value;
					dimX = oldValue.length; dimY = oldValue[0].length; dimZ = oldValue[0][0].length;
					newValue = new float[dimX*dimY*dimZ];
					for (int i = 0; i < dimX; i++) {
						for (int j = 0; j < dimY; j++) {
							System.arraycopy(oldValue[i][j], 0, newValue, i*dimY*dimZ+j*dimZ, dimZ);
						}
					}
					} break;
				case DOUBLE_MATRIX_3D: {
					double[][][] oldValue = (double[][][])value;
					dimX = oldValue.length; dimY = oldValue[0].length; dimZ = oldValue[0][0].length;
					newValue = new double[dimX*dimY*dimZ];
					for (int i = 0; i < dimX; i++) {
						for (int j = 0; j < dimY; j++) {
							System.arraycopy(oldValue[i][j], 0, newValue, i*dimY*dimZ+j*dimZ, dimZ);
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
								System.arraycopy(oldValue[i][j][k], 0, newValue, i*dimY*dimZ*dimZZ+j*dimZ*dimZZ+k*dimZZ, dimZZ);
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
								System.arraycopy(oldValue[i][j][k], 0, newValue, i*dimY*dimZ*dimZZ+j*dimZ*dimZZ+k*dimZZ, dimZZ);
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
								System.arraycopy(oldValue[i][j][k], 0, newValue, i*dimY*dimZ*dimZZ+j*dimZ*dimZZ+k*dimZZ, dimZZ);
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
								System.arraycopy(oldValue[i][j][k], 0, newValue, i*dimY*dimZ*dimZZ+j*dimZ*dimZZ+k*dimZZ, dimZZ);
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
								System.arraycopy(oldValue[i][j][k], 0, newValue, i*dimY*dimZ*dimZZ+j*dimZ*dimZZ+k*dimZZ, dimZZ);
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
								System.arraycopy(oldValue[i][j][k], 0, newValue, i*dimY*dimZ*dimZZ+j*dimZ*dimZZ+k*dimZZ, dimZZ);
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
								System.arraycopy(oldValue[i][j][k], 0, newValue, i*dimY*dimZ*dimZZ+j*dimZ*dimZZ+k*dimZZ, dimZZ);
							}
						}
					}
					} break;
			}
		}
		
		return newValue;
	}
}
