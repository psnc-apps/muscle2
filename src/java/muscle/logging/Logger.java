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

import java.util.logging.Level;
import java.util.logging.LogManager;

	
/**
utility class which helps to initialze loggers within the muscle
@author Jan Hegewald
*/
public class Logger {


//	Logger(String name, String resourceBundleName) {
//		super(name, resourceBundleName);
//	}
	
//	//
//	public static Logger getLogger(Object owner) {
//	
//		return getLogger(owner.getClass());
//	}

	//
	public static java.util.logging.Logger getLogger(Class cls) {
			
		return java.util.logging.Logger.getLogger(cls.getName());
	}


//	//
//	private static muscle.logging.Logger getLogger(String name) {
//	
//		return java.util.logging.Logger.getLogger(name);
//	}
}
