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
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

/**
formats log messages
@author Joris Borgdorff
*/
public class MuscleDetailFormatter extends SimpleFormatter {
	private final static int SEVERE = Level.SEVERE.intValue();
	private final static int WARNING = Level.WARNING.intValue();
	private final static int INFO = Level.INFO.intValue();
	
	public String format(LogRecord record) {
		StringBuilder sb = new StringBuilder(200);
		String loggerName = record.getLoggerName();
		Calendar c = new GregorianCalendar();
		sb.append('[');
		MuscleFormatter.date(sb, c);
		sb.append(' ');
		MuscleFormatter.time(sb, c);
		sb.append(' ');
		
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
		MuscleFormatter.rightAlign(sb, pkg, 15);
		sb.append(": ");
		MuscleFormatter.rightAlign(sb, clazz, 25);
		sb.append('.');
		
		String method = record.getSourceMethodName();
		if (method == null) method = "?";
		MuscleFormatter.leftAlign(sb, method, 20);
		sb.append("] ");

		int intLevel = record.getLevel().intValue();
		if (intLevel >= SEVERE) {
			sb.append("ERROR: ");
		} else if (intLevel >= WARNING) {
			sb.append("warning: ");
		} else if (intLevel < INFO) {
			sb.append("debug: ");
		}
		
		MuscleFormatter.formatMessage(sb, record);
		sb.append('\n');
		
		Throwable thrown = record.getThrown();
		if (thrown != null) {
			try {
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				thrown.printStackTrace(pw);
				pw.close();
				
				sb.append("[================== ERROR ===================] ");
				sb.append(sw);
				sb.append("[================ END TRACE =================]\n");
			} catch (Exception ex) {
				// Do nothing
			}
		}

		return sb.toString();
	}
}
