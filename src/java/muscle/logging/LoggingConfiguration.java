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

package muscle.logging;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
initializes our logging mechanism, load with<br>
-Djava.util.logging.config.class=muscle.logging.LoggingConfiguration
@author Jan Hegewald
*/
public class LoggingConfiguration {

	public LoggingConfiguration() throws java.io.IOException {
		InputStream loggingConfig = null;

		String fileName = System.getProperty("java.util.logging.config.file");
		if(fileName != null) {
			try {
				 loggingConfig = new FileInputStream(fileName);
			} catch (java.io.FileNotFoundException e) {
				loggingConfig = null;
			}
		}
		
		if( loggingConfig != null ) {
			// load our logging config into the LogManager
			LogManager manager = LogManager.getLogManager();
			manager.readConfiguration(loggingConfig);
		}
		else {
			// init LogManager with JVMs default settings<br>
			// important: unset this class from java.util.logging.config.class,
			// else the LogManager#readConfiguration() would initialize this class in an endless loop
			System.clearProperty("java.util.logging.config.class");

			LogManager.getLogManager().readConfiguration();		
		}
	}
}
