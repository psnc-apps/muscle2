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
		sb.append(' ').append(name);
		sb.append(") ");
		
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
