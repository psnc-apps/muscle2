/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package muscle.util.logging;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

/**
 *
 * @author Joris Borgdorff
 */
public class ModuleFormatter extends SimpleFormatter {
	private final static String format = "(%tT %6.6s) %s%s\n%s";
	private final static int SEVERE = Level.SEVERE.intValue();
	private final static int WARNING = Level.WARNING.intValue();
	private final static int INFO = Level.INFO.intValue();
	private final String name;

	public ModuleFormatter(String name) {
		super();
		this.name = name;
	}
	
	public synchronized String format(LogRecord record) {		
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
		
		return String.format(format, System.currentTimeMillis(), this.name, level, msg, err);
	}
}
