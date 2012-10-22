/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package muscle.exception;

/**
 *
 * @author Joris Borgdorff
 */
public class MUSCLEDatatypeException extends MUSCLERuntimeException {
	public MUSCLEDatatypeException() {
		
	}
	public MUSCLEDatatypeException(String message) {
		super(message);
	}
	public MUSCLEDatatypeException(Throwable cause) {
		super(cause);
	}
	public MUSCLEDatatypeException(String message, Throwable cause) {
		super(message, cause);
	}
}
