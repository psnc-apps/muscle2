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

package muscle;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.net.URI;

import muscle.exception.MUSCLERuntimeException;


/**
loads environment for a muscle platform
@author Jan Hegewald
*/
public class Env extends utilities.Env {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	public static Env ONLY = Env.create();




	private static Env create() {

		// use java property like:
		// -Dmuscle.core.Env="file:/path/to/json_file"
		// or a json formatted string
		// -Dmuscle.core.Env={\"cs_file\":\"/path/to/custom/cs\"}
		String rawEnv = System.getProperty(javatool.ClassTool.getName(Env.class));

		if(rawEnv != null) {
			URI envUri = null;
			try {
				envUri = new URI(rawEnv);
			}
			catch(java.net.URISyntaxException e) {
				// not an uri, now try to create the env from the raw string
				return new Env(rawEnv);
			}
				// create env with file from uri
				try {
					return new Env(new FileReader(new File(envUri)));
				}
				catch(FileNotFoundException e) {
					throw new MUSCLERuntimeException(e);
				}
		}

		return new Env();
	}

	//
	private Env() {
		super();
	}

	//
	private Env(Reader in) {
		super(in);
	}

	//
	private Env(String in) {
		super(in);
	}


}
