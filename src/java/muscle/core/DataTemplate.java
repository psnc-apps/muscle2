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

import java.io.Serializable;
import muscle.core.model.Distance;

/**
describes data content and accompanies a DataWrapper
@author Jan Hegewald
*/
public class DataTemplate<T> implements Serializable {
	private static final long serialVersionUID = 1L;
	// these fields might be null to indicate: any
	private final Class<?> dataClass; // double.class for double, boolean.class for boolean, int[].class for 1D int array etc.
	private final Scale scale;

	public DataTemplate(Class<T> newDataClass) {
		this(newDataClass, new Scale(new Distance(1d), new Distance(1d)));
	}
	
	public DataTemplate(Class<T> newDataClass, Scale newScale) {
		dataClass = replacePrimitiveClass(newDataClass);
		this.scale = newScale;
	}
	
	public Scale getScale() {
		return this.scale;
	}
	
	public String toString() {
		return "<" + dataClass.getSimpleName() + ":" + scale.toString() + ">";
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
}

