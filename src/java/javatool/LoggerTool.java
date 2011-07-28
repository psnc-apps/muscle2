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


import java.util.logging.Logger;
import java.util.logging.Level;

/**
provides additional functionality to deal with Loggers
@author Jan Hegewald
*/
public class LoggerTool {


	/**
	returns the coarsest loggable level for this logger<br>
	considers only the standard log levels defined in java.util.logging.Level
	*/
	public static Level loggableLevel(Logger logger) {

		Level[] levels = {Level.ALL, Level.FINEST, Level.FINER, Level.FINE, Level.CONFIG, Level.INFO, Level.WARNING, Level.SEVERE};
		for(Level l : levels) {
			if( logger.isLoggable(l) ) {
				return l;
			}
		}
		
		return Level.OFF;
	}
}
