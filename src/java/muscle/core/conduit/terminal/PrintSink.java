/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package muscle.core.conduit.terminal;

import java.util.Arrays;
import muscle.core.model.Observation;

/**
 * Prints all data that is sent to it to console.
 * @author Joris Borgdorff
 */
public class PrintSink extends Sink {
	@Override
	public void send(Observation msg) {
		String dataString = Arrays.deepToString(new Object[] {msg.getData()});
		System.out.println("PrintSink " + getLocalName() + ": received message " + msg);
		System.out.println("PrintSink " + getLocalName() + ": with data " + dataString);
	}
}
