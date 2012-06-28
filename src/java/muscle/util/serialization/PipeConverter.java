/*
 * 
 */
package muscle.util.serialization;

/**
 * Unsafe converter that only casts to another class
 * @author Joris Borgdorff
 */
public class PipeConverter<E> extends AbstractDataConverter<E,E> {
	@Override
	public E serialize(E data) {
		return data;
	}

	@Override
	public E deserialize(E data) {
		return data;
	}
}
