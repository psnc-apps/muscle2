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

package muscle.util.logging;

import java.util.GregorianCalendar;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

/**
 *
 * @author Joris Borgdorff
 */
public class ModuleFormatter extends SimpleFormatter {
	private final String name;

	@SuppressWarnings("AssignmentToMethodParameter")
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
	
	@Override
	public String format(LogRecord record) {
		StringBuilder sb = new StringBuilder(100);
		sb.append('(');
		MuscleFormatter.time(sb, new GregorianCalendar());
		sb.append(' ').append(name).append(") ");
		
		MuscleFormatter.formatMessage(sb, record);
		sb.append('\n');
		MuscleFormatter.addTrace(sb, record, true);
		return sb.toString();
	}
}
