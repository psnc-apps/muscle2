/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package muscle.util.logging;

import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

/**
 *
 * @author Joris Borgdorff
 */
public class TrivialFormatter extends SimpleFormatter {
	@Override
	public String format(LogRecord record) {
		return record.getMessage();
	}
}
