/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package muscle.client.communication;

import muscle.util.concurrency.Disposable;
import muscle.util.serialization.DataConverter;

/**
 *
 * @author Joris Borgdorff
 */
public interface CommunicatingPoint<E,F> extends Disposable {
	public void setDataConverter(DataConverter<E,F> convert);
}
