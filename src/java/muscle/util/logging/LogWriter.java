/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package muscle.util.logging;

import java.util.logging.LogRecord;

/**
 *
 * @author Joris Borgdorff
 */
public interface LogWriter {
	public void write(LogRecord r);
}
