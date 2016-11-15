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

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
just the version of this MUSCLE
@author Jan Hegewald
*/
public class Version {
	private final static String VERSION_NUM = "2.1";
	private final static String gittag;
	private final static Properties config;

	static {
		Properties fallback = new Properties();
		fallback.put("gittag", "unknown");
		config = new Properties(fallback);
		try {
			InputStream stream = Version.class.getResourceAsStream("gittag.properties");
			if (stream != null) {
				try {
					config.load(stream);
				}
				finally {
					stream.close();
				}
			}
		}
		catch (IOException ex) {
			/* Handle exception. */
		}
		gittag = config.getProperty("gittag", "unknown");
	}

	private final static String INFO_TEXT = "Multiscale Coupling Library and Environment (MUSCLE) version " + VERSION_NUM + "\n\tGit tag: " + gittag;

	public static void main(String[] args) {
		System.out.println(INFO_TEXT);
	}
}
