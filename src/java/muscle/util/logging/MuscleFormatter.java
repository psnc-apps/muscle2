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
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

/**
formats log messages
@author Joris Borgdorff
*/
public class MuscleFormatter extends SimpleFormatter {
	private final static int SEVERE = Level.SEVERE.intValue();
	private final static int WARNING = Level.WARNING.intValue();
	private final static int INFO = Level.INFO.intValue();
	
	public String format(LogRecord record) {
		StringBuilder sb = new StringBuilder(200);
		sb.append('[');
		time(sb, new GregorianCalendar());
		sb.append(' ');
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
		rightAlign(sb, loggerName, 6);
		sb.append("] ");
		
		formatMessage(sb, record);
		sb.append('\n');
		Throwable thrown = record.getThrown();
		if(thrown != null) {
			sb.append("                         (")
				.append(thrown.getClass().getName())
				.append(": ")
				.append(thrown.getMessage())
				.append(")\n");
		}
		
		return sb.toString();
	}
	
	public static void formatMessage(StringBuilder sb, LogRecord record) {
		final int intLevel = record.getLevel().intValue();
		if (intLevel >= SEVERE) {
			sb.append("ERROR: ");
		} else if (intLevel >= WARNING) {
			sb.append("warning: ");
		} else if (intLevel < INFO) {
			sb.append("debug: ");
		}
		
		String msg = record.getMessage();
		try {
            Object parameters[] = record.getParameters();
            if (parameters != null && parameters.length > 0 && 
					(msg.indexOf("{0") >= 0 || msg.indexOf("{1") >=0 ||
                        msg.indexOf("{2") >=0|| msg.indexOf("{3") >=0)) {
		            sb.append(MessageFormat.format(msg, parameters));
			} else {
				sb.append(msg);
            }
        } catch (Exception ex) {
            // Formatting failed: use localized format string.
            sb.append(msg);
        }
	}
	
	static void leftAlign(StringBuilder sb, String s, int len) {
		int oldLen = s.length();
		if (oldLen > len) {
			sb.append(s.substring(0, len));
		} else {
			sb.append(s);
			if (oldLen < len) {
				for (int i = oldLen; i < len; i++) {
					sb.append(' ');
				}
			}
		}
	}
	static void rightAlign(StringBuilder sb, String s, int len) {
		int oldLen = s.length();
		if (oldLen > len) {
			sb.append(s.substring(0, len));
		} else {
			if (oldLen < len) {
				for (int i = oldLen; i < len; i++) {
					sb.append(' ');
				}
			}
			sb.append(s);
		}
	}
	static void time(StringBuilder sb, Calendar c) {
		int t = c.get(Calendar.HOUR_OF_DAY);
		if (t < 10)	{
			sb.append('0');
		}
		sb.append(t).append(':');
		t = c.get(Calendar.MINUTE);
		if (t < 10) {
			sb.append('0');
		}
		sb.append(t).append(':');
		t = c.get(Calendar.SECOND);
		if (t < 10) {
			sb.append('0');
		}
		sb.append(t);
	}
	static void date(StringBuilder sb, Calendar c) {
		int t = c.get(Calendar.YEAR);
		sb.append(t).append('-');
		t = c.get(Calendar.MONTH);
		if (t < 10) {
			sb.append('0');
		}
		sb.append(t).append('-');
		t = c.get(Calendar.DATE);
		if (t < 10) {
			sb.append('0');
		}
		sb.append(t);
	}
	
	static void addTrace(StringBuilder sb, LogRecord record, boolean addName) {
		Throwable thrown = record.getThrown();
		if (thrown != null) {
			try {
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				thrown.printStackTrace(pw);
				pw.close();
				
				sb.append("[================== ERROR ===================] ");
				if (addName) {
					sb.append(thrown.getClass().getName())
						.append(": ")
						.append(thrown.getMessage());
				}
				sb.append(sw);
				sb.append("[================ END TRACE =================]\n");
			} catch (Exception ex) {
				// Do nothing
			}
		}
	}
}
