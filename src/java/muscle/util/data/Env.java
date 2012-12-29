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

package muscle.util.data;

import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

/**
loads environment for multiple classes from a json file
@author Jan Hegewald
*/
public class Env extends HashMap<Object,Object> {
	private static final long serialVersionUID = 1L;
	public Env() {
		super();
	}	

	@SuppressWarnings("unchecked")
	public Env(Reader in) {
		super();
		// treat input as a org.json.simple.JSONObject and put in our env
		JSONObject jsonHash = (JSONObject)JSONValue.parse(in);
		putAll(jsonHash);
	}

	public Env(String input) {
		this(new StringReader(input));
	}	

	public Env(Map<?, ?> map) {
		super();
		putAll(map);
	}
	
	public Env subenv(Class cls) {

		JSONObject jsonHash = (JSONObject)get(muscle.util.ClassTool.getName(cls));
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
