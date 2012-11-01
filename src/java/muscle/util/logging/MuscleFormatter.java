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

package muscle.util.logging;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

/**
formats log messages
@author Joris Borgdorff
*/
public class MuscleFormatter extends SimpleFormatter {
	private final static String format = "[%tT %6.6s] %s%s\n%s";
	private final static int SEVERE = Level.SEVERE.intValue();
	private final static int WARNING = Level.WARNING.intValue();
	private final static int INFO = Level.INFO.intValue();
	
	public synchronized String format(LogRecord record) {
		String loggerName = record.getLoggerName();
		if (loggerName == null) {
			loggerName = "?";
		} else if (!loggerName.startsWith("muscle")) {
			// Use class name
			int	classIndex = loggerName.lastIndexOf('.');
			if (classIndex >= 0) {
				loggerName = loggerName.substring(classIndex + 1);
			}
		}
		
		int intLevel = record.getLevel().intValue();
		String level;
		if (intLevel >= SEVERE) {
			level = "ERROR: ";
		} else if (intLevel >= WARNING) {
			level = "warning: ";
		} else if (intLevel >= INFO) {
			level = "";
		} else {
			level = "debug: ";
		}
		
		String msg = formatMessage(record);
		
		Throwable thrown = record.getThrown();
		String err;
		if(thrown == null) {
			err = "";
		} else {
			err = "                         (" + thrown.getClass().getName() + ": " + thrown.getMessage() + ")\n";
		}
		
		return String.format(format, System.currentTimeMillis(), loggerName, level, msg, err);
	}
}
