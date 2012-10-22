/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package muscle.exception;

/**
 *
 * @author Joris Borgdorff
 */
public class MUSCLEConduitExhaustedException extends MUSCLERuntimeException {
	public MUSCLEConduitExhaustedException() {
		
	}
	public MUSCLEConduitExhaustedException(String message) {
		super(message);
	}
	public MUSCLEConduitExhaustedException(Throwable cause) {
		super(cause);
	}
	public MUSCLEConduitExhaustedException(String message, Throwable cause) {
		super(message, cause);
	}
}
