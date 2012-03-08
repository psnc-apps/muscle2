package utilities.data;

import java.util.Collection;
import java.util.Map;

/**
 *
 * @author Joris Borgdorff
 */
public enum SerializableDatatype {
	NULL(null), STRING_MAP(Map.class), COLLECTION(Collection.class),
	STRING(String.class),     BOOLEAN(Boolean.class),     BYTE(Byte.class),     SHORT(Short.class),     INT(Integer.class),     LONG(Long.class),     FLOAT(Float.class),     DOUBLE(Double.class),
	STRING_ARR(String[].class, 1), BOOLEAN_ARR(boolean[].class, 1), BYTE_ARR(byte[].class, 1), SHORT_ARR(short[].class, 1), INT_ARR(int[].class, 1), LONG_ARR(long[].class, 1), FLOAT_ARR(float[].class, 1), DOUBLE_ARR(double[].class, 1),
	BOOLEAN_MATRIX_2D(boolean[][].class, 2), BYTE_MATRIX_2D(byte[][].class, 2), SHORT_MATRIX_2D(short[][].class, 2), INT_MATRIX_2D(int[][].class, 2), LONG_MATRIX_2D(long[][].class, 2), FLOAT_MATRIX_2D(float[][].class, 2), DOUBLE_MATRIX_2D(double[][].class, 2),
	BOOLEAN_MATRIX_3D(boolean[][][].class, 3), BYTE_MATRIX_3D(byte[][][].class, 3), SHORT_MATRIX_3D(short[][][].class, 3), INT_MATRIX_3D(int[][][].class, 3), LONG_MATRIX_3D(long[][][].class, 3), FLOAT_MATRIX_3D(float[][][].class, 3), DOUBLE_MATRIX_3D(double[][][].class, 3),
	BOOLEAN_MATRIX_4D(boolean[][][][].class, 4), BYTE_MATRIX_4D(byte[][][][].class, 4), SHORT_MATRIX_4D(short[][][][].class, 4), INT_MATRIX_4D(int[][][][].class, 4), LONG_MATRIX_4D(long[][][][].class, 4), FLOAT_MATRIX_4D(float[][][][].class, 4), DOUBLE_MATRIX_4D(double[][][][].class, 4),
	JAVA_BYTE_OBJECT(null);
	
	private final int dimensions;
	private final Class<?> clazz;
	
	SerializableDatatype(Class<?> clazz, int dim) {
		this.clazz = clazz;
		this.dimensions = dim;
	}
	SerializableDatatype(Class<?> clazz) {
		this(clazz, 0);
	}
	
	public boolean isArray() {
		return dimensions == 1;
	}
	
	public boolean isMatrix() {
		return dimensions >= 2;
	}
	
	public boolean isMatrix2D() {
		return dimensions == 2;
	}
	
	public boolean isMatrix3D() {
		return dimensions == 3;
	}

	public boolean isMatrix4D() {
		return dimensions == 4;
	}
	
	public int getDimensions() {
		return dimensions;
	}
	
	public Class<?> getDataClass() {
		return clazz;
	}
}
