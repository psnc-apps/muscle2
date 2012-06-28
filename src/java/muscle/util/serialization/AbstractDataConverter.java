/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package muscle.util.serialization;

/**
 *
 * @author Joris Borgdorff
 */
public abstract class AbstractDataConverter<E,F> implements DataConverter<E, F> {
	public E copy(E data) {
		return this.deserialize(this.serialize(data));
	}
}
