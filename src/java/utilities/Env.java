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

package utilities;

import java.util.HashMap;
import org.json.simple.JSONValue;
import org.json.simple.JSONObject;
import java.io.File;
import java.net.URI;
import java.io.Reader;
import java.util.Map;
import java.io.StringReader;

/**
loads environment for multiple classes from a json file
@author Jan Hegewald
*/
public class Env extends HashMap {


	public Env() {
		super();
	}	

	public Env(Reader in) {

		super();
		// treat input as a org.json.simple.JSONObject and put in our env
		JSONObject jsonHash = (JSONObject)JSONValue.parse(in);
		putAll(jsonHash);
	}

	public Env(String input) {
		this(new StringReader(input));
	}	

	public Env(Map map) {
		super();
		putAll(map);
	}
	
	//
	public Env subenv(Class cls) {

		JSONObject jsonHash = (JSONObject)get(javatool.ClassTool.getName(cls));
		if(jsonHash != null) {
			return new Env(jsonHash);
		}
				
		return new Env();
	}


	/**
	throw exception if key does not exist
	*/
	public Object getEnvAndAssert(Object key) {

		if(!containsKey(key)) {
			throw new RuntimeException("no env for key <"+key+">");			
		}
		
		return get(key);
	}

	/**
	return given default value if key does not exist
	*/
	public Object get(Object key, Object defaultVal) {

		if(containsKey(key))
			return get(key);
		
		return defaultVal;			
	}

}
