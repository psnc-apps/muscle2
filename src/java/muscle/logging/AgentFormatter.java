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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.Date;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;


/**
formats log messages
@author Jan Hegewald
*/
public class AgentFormatter extends SimpleFormatter {

	Date dat = new Date();
	private final static String format = "{0,date} {0,time}";
	private MessageFormat formatter;

	private Object args[] = new Object[1];

	private String lineSeparator = "\n";

	@Override
	public synchronized String format(LogRecord record) {

		StringBuffer sb = new StringBuffer();
		// Minimize memory allocations here.
		this.dat.setTime(record.getMillis());
		this.args[0] = this.dat;
		StringBuffer text = new StringBuffer();
		if (this.formatter == null) {
			 this.formatter = new MessageFormat(format);
		}
		this.formatter.format(this.args, text, null);
		sb.append(text);
		sb.append(" ");
//		if (record.getSourceClassName() != null) {
//			 sb.append(record.getSourceClassName());
//		} else {
//			 sb.append(record.getLoggerName());
//		}
		sb.append(record.getLoggerName());
		sb.append("(");
		sb.append(record.getSourceClassName());
		sb.append(")");


		if (record.getSourceMethodName() != null) {
			sb.append(" ");
			sb.append(record.getSourceMethodName());
		}
		sb.append(this.lineSeparator);
		String message = this.formatMessage(record);
		sb.append(record.getLevel().getLocalizedName());
		sb.append(": ");
		sb.append(message);
		sb.append(this.lineSeparator);
		if(record.getThrown() != null) {
			try {
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				record.getThrown().printStackTrace(pw);
				pw.close();
				sb.append(sw.toString());
			} catch (Exception ex) {
			}
		}
		return sb.toString();
	}

}
