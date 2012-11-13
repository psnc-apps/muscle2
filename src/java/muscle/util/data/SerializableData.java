/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package muscle.util.data;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import muscle.util.serialization.ByteJavaObjectConverter;
import muscle.util.serialization.DeserializerWrapper;
import muscle.util.serialization.SerializerWrapper;

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
	private final long size;
	private final static SerializableDatatype[] datatypes = SerializableDatatype.values();
	
	/**
	 * Creates a new SerializableData object containing a value of with datatype type.
	 * It will use Java byte serialization if the object does not match any predetermined type.
	 * This will make the message incompatible with other programming languages.
	 * @param value any Serializable object of the right datatype
	 * @param type the type corresponding to the serializable object
	 * @throws IllegalArgumentException if the type provided does not match the data.
	 */
	public SerializableData(Serializable value, SerializableDatatype type, long size) {
		if (type == SerializableDatatype.NULL && value != null) {
			throw new IllegalArgumentException("A NULL datatype should be provided with null data.");
		} else if (type == SerializableDatatype.JAVA_BYTE_OBJECT) {
			if (value instanceof SerializableData) {
				SerializableData sValue = (SerializableData)value;
				this.value = sValue.value;
				this.size = sValue.size;
				this.type = sValue.type;
			} else {
				this.value = serialize(value, type);
				this.size = sizeOf(value, type);
				this.type = type;
			}
		} else if (type.getDataClass() != null && !type.getDataClass().isInstance(value)) {
			throw new IllegalArgumentException("Class of value '" + value.getClass() + "' does not match datatype '" + type + "'");
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
		}
		SerializableDatatype type = inferDatatype(value);
		long size = (type == SerializableDatatype.JAVA_BYTE_OBJECT) ? -1 : sizeOf(value, type);
		return new SerializableData(value, type, size);
	}
	
	/**
	 * Creates a new SerializableData object containing given value and datatype,
	 * determining the size.
	 * @param value any Serializable object
	 * @see SerializableData(Serializable, SerializableDatatype)
	 */
	public static SerializableData valueOf(Serializable value, SerializableDatatype type) {
		if (value == null) {
			return new SerializableData(null, SerializableDatatype.NULL, 0);
		}
		long size = (type == SerializableDatatype.JAVA_BYTE_OBJECT) ? -1 : sizeOf(value, type);
		return new SerializableData(value, type, size);
	}
	
	public static SerializableDatatype inferDatatype(Serializable value) {
		if (value == null) {
			return SerializableDatatype.NULL;
		}
		for (SerializableDatatype type : datatypes) {
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
	
	public long getSize() {
		return size;
	}
	
	public static SerializableData parseData(DeserializerWrapper in) throws IOException {
		int typeNum = in.readInt();
		
		if (typeNum < 0 || typeNum >= datatypes.length) {
			throw new IllegalStateException("Datatype with number " + typeNum + " not recognized");
		}
		
		SerializableDatatype type = datatypes[typeNum];
		Serializable value;
		int len;
		switch (type.typeOf()) {
			case NULL:
				return new SerializableData(null, type, 0);
			case MAP: {
				len = in.readInt();
				HashMap<Serializable,Serializable> map = new HashMap<Serializable,Serializable>(len*3/2);

				for (int i = 0; i < len; i++) {
					map.put(SerializableData.parseData(in).getValue(), SerializableData.parseData(in).getValue());
				}
				value = map;
			}
				break;
			case COLLECTION: {
				len = in.readInt();
				ArrayList<Serializable> list = new ArrayList<Serializable>(len);

				for (int i = 0; i < len; i++) {
					list.add(SerializableData.parseData(in).getValue());
				}
				value = list;
			}
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
			default:
				value = in.readValue(type);
				break;
		}

		long size = sizeOf(value, type);		
		if (type.isMatrix()) {
			int length = MatrixTool.lengthOfMatrix(value, type);
			
			int dimX, dimY, dimZ, dimZZ;
			dimX = in.readInt();
			dimY = type.isMatrix2D() ? length / dimX : in.readInt();
			dimZ = type.isMatrix3D() ? length / (dimX*dimY) : in.readInt();
			dimZZ = length / (dimX*dimY*dimZ);
		
			value = MatrixTool.arrayToMatrix(value, type, dimX, dimY, dimZ, dimZZ);
		}
		
		return new SerializableData(value, type, size);
	}
	
	public void encodeData(SerializerWrapper out) throws IOException {
		out.writeInt(type.ordinal());
		
		Serializable newValue = MatrixTool.matrixToArray(value, type);
		
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
				out.writeValue(newValue, type);
				break;
		}
		if (type.isMatrix()) {
			int dimNum = type.getDimensions();
			int[] dims = new int[dimNum];
			MatrixTool.dimensionsOfMatrix(value, type, dims, 0);
			for (int i = 0; i < dimNum - 1; i++) {
				out.writeInt(dims[i]);
			}
		}
	}
	
	private static long sizeOf(Serializable value, SerializableDatatype type) {
		int size = 0;
		if ((type.isArray() || type.isMatrix()) && type != SerializableDatatype.STRING_ARR) {
			return MatrixTool.deepSizeOf(value, type);
		} else if (type == SerializableDatatype.JAVA_BYTE_OBJECT) {
			byte[] byteValue = (byte[])(value instanceof byte[] ? value : serialize(value, type));
			return byteValue.length;
		} else {
			switch (type.typeOf()) {
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
		}
		
		return size;
	}
	
	public static <T extends Serializable> T createIndependent(T value) {
		return createIndependent(value, inferDatatype(value));
	}
	
	public static <T extends Serializable> T createIndependent(T value, SerializableDatatype type) {
		if (value instanceof SerializableData) {
			SerializableData sValue = (SerializableData)value;
			@SuppressWarnings("unchecked")
			T typedValue = (T)new SerializableData(createIndependent(sValue.getValue()), sValue.type, sValue.size);
			return typedValue;
		}
		Serializable copyValue;
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
			default:
				copyValue = MatrixTool.deepCopy(value, type);
				break;
		}
		@SuppressWarnings("unchecked")
		T typedValue = (T)copyValue;
		return typedValue;
	}
	
	@Override
	public String toString() {
		return "SerializableData[" + type.toString() + ", size=" + size + "]";
	}
}
