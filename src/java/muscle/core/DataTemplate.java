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

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import com.thoughtworks.xstream.XStream;
import utilities.MiscTool;
import utilities.jni.JNITool;
import java.io.ObjectStreamException;
import muscle.exception.MUSCLERuntimeException;
import java.io.File;
import java.util.logging.Logger;


/**
describes data content and accompanies a DataWrapper
@author Jan Hegewald
*/
public class DataTemplate<T> implements java.io.Serializable {
	
	public static final int ANY_SIZE = -1;
	public static final int UNKNOWN_QUANTITY = -2;

	// these fields might be null to indicate: any
	private Class<?> dataClass; // double.class for double, boolean.class for boolean, int[].class for 1D int array etc.
	private Scale scale;			// time and space scale according to scale map
//	private DataPattern dataPattern; // describes the layout of the data in the 1D array of the DataWrapper
	private int size = ANY_SIZE;
	
	//
	public static boolean match(DataTemplate templateA, DataTemplate templateB) {
		
		assert !MiscTool.anyNull(templateA, templateB) : "can not compare null";

		// test datatype
		if( !MiscTool.anyOf(AnyClass.class, templateA.getDataClass(), templateB.getDataClass()) )
			if( !templateA.getDataClass().equals(templateB.getDataClass()) )
				return false;

//		// test pattern
//		if( !MiscTool.anyNull(templateA.getPattern(), templateB.getPattern()) )
//			if( !templateA.getPattern().equals(templateB.getPattern()) )
//				return false;

		// test scale
		if( !MiscTool.anyNull(templateA.getScale(), templateB.getScale()) )
			if( !Scale.match(templateA.getScale(), templateB.getScale()) )
				return false;

		// test dimensions (dimensions is an int and can not be null)
		if( !MiscTool.anyNull(templateA.getScale(), templateB.getScale()) )
			if( templateA.getScale().getDimensions() != templateB.getScale().getDimensions() )
				return false;
		
		return true;
	}


//	//
//	public static DataTemplate createFromResourceForEntrance(String name) {
//	
//		return createInstanceFromFile(MiscTool.joinPaths(CxADescription.ONLY.getPathProperty("entrance_resources_path"), name+".DataTemplate"));
//	}


	//
	public static DataTemplate createFromResourceForExit(String name) {
	
		return createInstanceFromFile(new File(MiscTool.joinPaths(CxADescription.ONLY.getPathProperty("exit_resources_path"), name+".DataTemplate")));
	}


	//
	public static DataTemplate createInstanceFromFile(File file) {
	
		String text = null;
		try {
			text = MiscTool.fileToString(file);
		} catch (IOException e) {
			throw new MUSCLERuntimeException(e);
		}
		
		XStream xstream = new XStream();
		DataTemplate dt = (DataTemplate)xstream.fromXML(text);
				
		return new DataTemplate(dt.getDataClass(), dt.getScale()/*, dt.getPattern()*/);
	}
	
	
	//
	public DataTemplate(Class<T> newDataClass, Scale newScale) {
//		this(newDataClass, newScale, new DataPattern(0, DataPattern.ANY_BLOCKSIZE, 1, 0));
//	}
//
//	//
//	public DataTemplate(Class<T> newDataClass, Scale newScale, DataPattern newDataPattern) {
			
		if(newDataClass == null)
			dataClass = AnyClass.class;
		else
			dataClass = newDataClass;

		if(newScale == null)
			throw new MUSCLERuntimeException("scale can not be null");
		scale = newScale;

		//dataPattern = newDataPattern;
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
			muscle.logging.Logger.getLogger(getClass()).info("unknown data class <"+dataClass.getName()+">");
			return UNKNOWN_QUANTITY;
		}
			
		if( getSize() == ANY_SIZE)
			return ANY_SIZE;
			
		return dataSize * getSize();
	}


	//
	public Class<?> getDataClass() {
	
		return dataClass;
	}


	//
	public String getJNISignature() {

		return JNITool.toFieldDescriptor(dataClass);
	}


	//
	private int getSize() {

		return size;
	}
	
	
	//
	public Scale getScale() {
	
		return scale;
	}
	

//	//
//	public DataPattern getPattern() {
//	
//		return dataPattern;
//	}
	

	//
	public String toString() {

		XStream xstream = new XStream();
		return xstream.toXML(this);
	}


	// workaround to solve an issue with the JADE Classloader which can not serialize a Class field with a primitive class
	Object writeReplace() throws ObjectStreamException {

		// do not modify the original because it might not be deserialized at all (e.g if we call xstream.toXML on this object)

		Class<?> modifiedClass = dataClass;
		
		if(modifiedClass.equals(boolean.class)) modifiedClass = PrimitiveBoolean.class;
		else if(modifiedClass.equals(byte.class)) modifiedClass = PrimitiveByte.class;
		else if(modifiedClass.equals(char.class)) modifiedClass = PrimitiveChar.class;
		else if(modifiedClass.equals(short.class)) modifiedClass = PrimitiveShort.class;
		else if(modifiedClass.equals(int.class)) modifiedClass = PrimitiveInt.class;
		else if(modifiedClass.equals(long.class)) modifiedClass = PrimitiveLong.class;
		else if(modifiedClass.equals(float.class)) modifiedClass = PrimitiveFloat.class;
		else if(modifiedClass.equals(double.class)) modifiedClass = PrimitiveDouble.class;

		DataTemplate copy = new DataTemplate(modifiedClass, scale/*, dataPattern*/);
		return copy;
	}
	// workaround to solve an issue with the JADE Classloader which can not serialize a Class field with a primitive class
	private Object readResolve() throws ObjectStreamException {
		
		if(dataClass.equals(PrimitiveBoolean.class)) dataClass = boolean.class;
		else if(dataClass.equals(PrimitiveByte.class)) dataClass = byte.class;
		else if(dataClass.equals(PrimitiveChar.class)) dataClass = char.class;
		else if(dataClass.equals(PrimitiveShort.class)) dataClass = short.class;
		else if(dataClass.equals(PrimitiveInt.class)) dataClass = int.class;
		else if(dataClass.equals(PrimitiveLong.class)) dataClass = long.class;
		else if(dataClass.equals(PrimitiveFloat.class)) dataClass = float.class;
		else if(dataClass.equals(PrimitiveDouble.class)) dataClass = double.class;
		
		return this;
	}
	// workaround to solve an issue with the JADE Classloader which (as of v3.5) can not serialize a Class field with a primitive class
	private class PrimitiveBoolean {}
	private class PrimitiveByte {}
	private class PrimitiveChar {}
	private class PrimitiveShort {}
	private class PrimitiveInt {}
	private class PrimitiveLong {}
	private class PrimitiveFloat {}
	private class PrimitiveDouble {}


	//
	private static class AnyClass {
	
	}


	//
	public static void main (String args[]) {

		javax.measure.DecimalMeasure<javax.measure.quantity.Duration> dt = javax.measure.DecimalMeasure.valueOf(new java.math.BigDecimal(2), javax.measure.unit.SI.SECOND);
		javax.measure.DecimalMeasure<javax.measure.quantity.Length> dx1 = javax.measure.DecimalMeasure.valueOf(new java.math.BigDecimal(2), javax.measure.unit.SI.METER);
		DataTemplate template = new DataTemplate(double[].class, new Scale(dt, dx1)/*, new DataPattern(0,DataPattern.ANY_BLOCKSIZE,1,0)*/);
		System.out.println(template.toString());
	}
}

