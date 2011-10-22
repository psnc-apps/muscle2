/*
 * 
 */
package muscle.core.messaging.serialization;

/**
 * Unsafe converter that only casts to another class
 * @author Joris Borgdorff
 */
public class CastConverter<E, F> implements DataConverter<E,F> {
	@Override
	public F serialize(E data) throws ClassCastException {
		return (F)data;
	}

	@Override
	public E deserialize(F data) throws ClassCastException {
		return (E)data;
	}
}
