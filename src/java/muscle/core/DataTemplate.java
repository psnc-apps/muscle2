/*
Copyright 2008,2009 Complex Automata Simulation Technique (COAST) consortium

GNU Lesser General Public License

This file is part of MUSCLE (Multiscale Coupling Library and Environment).

    MUSCLE is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    MUSCLE is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with MUSCLE.  If not, see <http://www.gnu.org/licenses/>.
*/

package muscle.core;

import com.thoughtworks.xstream.XStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.measure.DecimalMeasure;
import javax.measure.quantity.Duration;
import javax.measure.quantity.Length;
import javax.measure.unit.SI;
import muscle.util.jni.JNITool;


/**
describes data content and accompanies a DataWrapper
@author Jan Hegewald
*/
public class DataTemplate<T> implements Serializable {
	public static final int ANY_SIZE = -1;
	public static final int UNKNOWN_QUANTITY = -2;

	// these fields might be null to indicate: any
	private final Class<?> dataClass; // double.class for double, boolean.class for boolean, int[].class for 1D int array etc.
	private int size = ANY_SIZE;
	private final Scale scale;

	public DataTemplate(Class<T> newDataClass) {
		this(newDataClass, new Scale(new DecimalMeasure<Duration>(BigDecimal.ONE, SI.SECOND), new DecimalMeasure<Length>(BigDecimal.ONE,SI.METER)));
	}
	
	public DataTemplate(Class<T> newDataClass, Scale newScale) {
		dataClass = replacePrimitiveClass(newDataClass);
		this.scale = newScale;
	}
	
	public Scale getScale() {
		return this.scale;
	}
	
	/**
	returns amount of data in bits or negative value if unspecified
	*/
	public long getQuantity() {
		int dataSize = 0;
		if(dataClass.equals(boolean[].class))
			dataSize = 1;
		else if(dataClass.equals(byte[].class))
			dataSize = Byte.SIZE;
		else if(dataClass.equals(char[].class))
			dataSize = Character.SIZE;
		else if(dataClass.equals(short[].class))
			dataSize = Short.SIZE;
		else if(dataClass.equals(int[].class))
			dataSize = Integer.SIZE;
		else if(dataClass.equals(long[].class))
			dataSize = Long.SIZE;
		else if(dataClass.equals(float[].class))
			dataSize = Float.SIZE;
		else if(dataClass.equals(double[].class))
			dataSize = Double.SIZE;
		else {
			Logger.getLogger(DataTemplate.class.getName()).log(Level.INFO, "unknown data class <{0}>", dataClass.getName());
			return UNKNOWN_QUANTITY;
		}
			
		if( getSize() == ANY_SIZE)
			return ANY_SIZE;
			
		return dataSize * getSize();
	}

	public String getJNISignature() {
		return JNITool.toFieldDescriptor(dataClass);
	}

	private int getSize() {
		return size;
	}

	public String toString() {
		XStream xstream = new XStream();
		return xstream.toXML(this);
	}

	// workaround to solve an issue with the JADE Classloader which can not serialize a Class field with a primitive class
	private static Class<?> replacePrimitiveClass(Class<?> clazz) {
		if(clazz.equals(boolean.class)) return PrimitiveBoolean.class;
		else if(clazz.equals(byte.class)) return PrimitiveByte.class;
		else if(clazz.equals(char.class)) return PrimitiveChar.class;
		else if(clazz.equals(short.class)) return PrimitiveShort.class;
		else if(clazz.equals(int.class)) return PrimitiveInt.class;
		else if(clazz.equals(long.class)) return PrimitiveLong.class;
		else if(clazz.equals(float.class)) return PrimitiveFloat.class;
		else if(clazz.equals(double.class)) return PrimitiveDouble.class;
		return clazz;
	}

	// workaround to solve an issue with the JADE Classloader which can not serialize a Class field with a primitive class
	@SuppressWarnings({"unchecked", "unchecked"})
	public Class<T> getDataClass() {
		Class<?> ret = dataClass;
		
		if(dataClass.equals(PrimitiveBoolean.class)) ret = boolean.class;
		else if(dataClass.equals(PrimitiveByte.class)) ret = byte.class;
		else if(dataClass.equals(PrimitiveChar.class)) ret = char.class;
		else if(dataClass.equals(PrimitiveShort.class)) ret = short.class;
		else if(dataClass.equals(PrimitiveInt.class)) ret = int.class;
		else if(dataClass.equals(PrimitiveLong.class)) ret = long.class;
		else if(dataClass.equals(PrimitiveFloat.class)) ret = float.class;
		else if(dataClass.equals(PrimitiveDouble.class)) ret = double.class;
		
		return (Class<T>)ret;
	}
	// workaround to solve an issue with the JADE Classloader which (as of v3.5) can not serialize a Class field with a primitive class
	private static class PrimitiveBoolean {}
	private static class PrimitiveByte {}
	private static class PrimitiveChar {}
	private static class PrimitiveShort {}
	private static class PrimitiveInt {}
	private static class PrimitiveLong {}
	private static class PrimitiveFloat {}
	private static class PrimitiveDouble {}

	//
	public static void main (String args[]) {

		DataTemplate<double[]> template = new DataTemplate<double[]>(double[].class/*, new DataPattern(0,DataPattern.ANY_BLOCKSIZE,1,0)*/);
		System.out.println(template.toString());
	}
}

