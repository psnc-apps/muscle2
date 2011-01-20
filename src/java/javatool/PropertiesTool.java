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

package javatool;

import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import utilities.CArrayList;
import utilities.Invoker;


/**
additional functionality for java.util.Properties
@author Jan Hegewald
*/
public class PropertiesTool {


	//
	public static boolean hasKeys(Properties props, String[] keys) {

		assert props != null;
		assert keys != null;
		return PropertiesTool.hasKeys(props, new CArrayList<String>(keys));
	}


	//
	public static boolean hasKeys(Properties props, List<String> keys) {

		assert props != null;
		assert keys != null;

		return props.keySet().containsAll(keys);
	}


	/**
	as Property.getProperty, but tries to force the return value to be of given type
	*/
	public static <T> T getPropertyWithType(Properties props, String key, Class<T> type) throws java.lang.reflect.InvocationTargetException {

		String textValue = props.getProperty(key);
		if(textValue == null) {
			return null;
		}

		// see if we can convert textValue to the requested type
		// we use the valueOf method of the given class
		T value = null;
		try {
			value = (T)Invoker.invokeMethod(type, "valueOf", textValue);
		}
		catch (NoSuchMethodException e) {
			RuntimeException re = new RuntimeException("can not determine how to convert <"+textValue+"> to a <"+type+">");
			re.setStackTrace(e.getStackTrace());
			throw re;
		}
		catch (IllegalAccessException e) {
			RuntimeException re = new RuntimeException("can not determine how to convert <"+textValue+"> to a <"+type+">");
			re.setStackTrace(e.getStackTrace());
			throw re;
		}

		return value;
	}


	//
	public static boolean hasKeysWithTypes(Properties props, HashMap<String, Class<?>> types) {

		for (String key : types.keySet()) {
			if( !PropertiesTool.hasKeyWithType(props, key, types.get(key)) ) {
				return false;
			}
		}

		return true;
	}


	//
	public static boolean hasKeyWithType(Properties props, String key, Class type) {

		String textValue = props.getProperty(key);
		if(textValue == null) {
			return false;
		}

		// see if we can convert textValue to the requested type
		// we use the valueOf method of the given class
		try {
			Invoker.invokeMethod(type, "valueOf", textValue);
		}
		catch (NoSuchMethodException e) {
			throw new RuntimeException("can not determine how to convert <"+textValue+"> to a <"+type+">");
		}
		catch (IllegalAccessException e) {
			throw new RuntimeException("can not determine how to convert <"+textValue+"> to a <"+type+">");
		}
		catch (java.lang.reflect.InvocationTargetException e) {
			// the target class has thrown an exception while converting textValue to its type
			return false;
		}

		return true;
	}
}



