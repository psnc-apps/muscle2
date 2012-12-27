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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

/**
formats log messages
@author Joris Borgdorff
*/
public class MuscleDetailFormatter extends SimpleFormatter {
	private final static String format = "[%tF %tT %15.15s: %25.25s.%-20.20s] %s%s%s\n";
	private final static int SEVERE = Level.SEVERE.intValue();
	private final static int WARNING = Level.WARNING.intValue();
	private final static int INFO = Level.INFO.intValue();
	
	public String format(LogRecord record) {
		String loggerName = record.getLoggerName();
		String pkg, clazz;
		if (loggerName == null) {
			clazz = pkg = "?";
		} else {
			int classIndex = loggerName.lastIndexOf('.');
			if (classIndex == -1) {
				clazz = loggerName;
				pkg = ":";
			} else {
				 clazz = loggerName.substring(classIndex + 1);
				 pkg = loggerName.substring(0, Math.min(15, classIndex));
			}
		}
		
		String method = record.getSourceMethodName();
		if (method == null) method = "?";

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
			try {
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				pw.println("\n[================== ERROR ===================] " + thrown.getClass().getName() + ": " + thrown.getMessage());
				thrown.printStackTrace(pw);
				pw.print("[================ END TRACE =================]");
				pw.close();
				err = sw.toString();
			} catch (Exception ex) {
				err = "";
			}
		}
		Long time = System.currentTimeMillis();
		return String.format(format, time, time, pkg, clazz, method, level, msg, err);
	}
}
