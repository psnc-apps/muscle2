/*
* Copyright 2008, 2009 Complex Automata Simulation Technique (COAST) consortium
* Copyright 2010-2013 Multiscale Applications on European e-Infrastructures (MAPPER) project
*
* GNU Lesser General Public License
* 
* This file is part of MUSCLE (Multiscale Coupling Library and Environment).
* 
* MUSCLE is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
* 
* MUSCLE is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Lesser General Public License for more details.
* 
* You should have received a copy of the GNU Lesser General Public License
* along with MUSCLE.  If not, see <http://www.gnu.org/licenses/>.
*/
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package muscle.util.logging;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

/**
 *
 * @author Joris Borgdorff
 */
public class ModuleFormatter extends SimpleFormatter {
	private final static int SEVERE = Level.SEVERE.intValue();
	private final static int WARNING = Level.WARNING.intValue();
	private final static int INFO = Level.INFO.intValue();
	private final String name;

	public ModuleFormatter(String name) {
		super();
		if (name.length() > 6) {
			this.name = name.substring(0, 6);
		} else {
			if (name.length() < 6) {
				for (int i = name.length(); i < 6; i++) {
					name = " " + name;
				}
			}
			this.name = name;
		}
	}
	
	public String format(LogRecord record) {
		StringBuilder sb = new StringBuilder(100);
		sb.append('(');
		MuscleFormatter.time(sb, new GregorianCalendar());
		sb.append(' ').append(name).append(") ");
		
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
				
				sb.append("[================== ERROR ===================] ")
						.append(thrown.getClass().getName())
						.append(": ")
						.append(thrown.getMessage());
				sb.append(sw);
				sb.append("[================ END TRACE =================]\n");
			} catch (Exception ex) {
				// Do nothing
			}
		}
		return sb.toString();
	}
}
