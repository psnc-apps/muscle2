/*
* Copyright 2008, 2009 Complex Automata Simulation Technique (COAST) consortium
* Copyright 2010-2013 Multiscale Applications on European e-Infrastructures (MAPPER) project
*
* GNU Lesser General Public License
* 
* This file is part of MUSCLE (Multiscale Coupling Library and Environment).
* 
* MUSCLE is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
* 
* MUSCLE is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Lesser General Public License for more details.
* 
* You should have received a copy of the GNU Lesser General Public License
* along with MUSCLE.  If not, see <http://www.gnu.org/licenses/>.
*/
package muscle.core.conduit.terminal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import muscle.core.model.Observation;
import muscle.core.model.Timestamp;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

/**
 *
 * @author Joris Borgdorff
 */
public class JSONSource extends Source<HashMap<String,Serializable>> {
	int iteration = 0;
	URL url;

	@Override
	public void beforeExecute() {
		try {
			// Get data from web
			url = new URL(getProperty("url"));
		} catch (MalformedURLException ex) {
			Logger.getLogger(JSONSource.class.getName()).log(Level.SEVERE, "Could not parse url parameter to use with JSON source", ex);
			throw new RuntimeException(ex);
		}
	}
	
	@Override
	public Observation<HashMap<String, Serializable>> take() throws InterruptedException {
		Logger.getLogger(JSONSource.class.getName()).log(Level.INFO, "Source takes next");

		BufferedReader in = null;
		Observation<HashMap<String,Serializable>> obs = null;
		
		try {
			in = new BufferedReader(new InputStreamReader(url.openStream()));
			JSONObject jsonHash = (JSONObject)JSONValue.parse(in);

			@SuppressWarnings("unchecked")
			HashMap<String,Serializable> map = new HashMap<String,Serializable>(jsonHash);

			obs = new Observation<HashMap<String,Serializable>>(map, new Timestamp(iteration), new Timestamp(++iteration));
		} catch (IOException ex) {
			Logger.getLogger(JSONSource.class.getName()).log(Level.SEVERE, "Can not connect to URL <" + url + "> with JSON source.", ex);
		} finally {
			try {
				if (in != null)
					in.close();
			} catch (IOException ex) {
			}
		}
		
		return obs;
	}

	@Override
	public boolean isEmpty() {
		Logger.getLogger(JSONSource.class.getName()).log(Level.INFO, "Source has next");
		// is not empty: has data
		return false;
	}
}
