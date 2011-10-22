/*
 * 
 */
package muscle.core.messaging.serialization;

/**
 *
 * @author Joris Borgdorff
 */
public class CombinedConverter<E, F, G> implements DataConverter<E,G> {
	private DataConverter<E, F> c1;
	private DataConverter<F, G> c2;
	
	public CombinedConverter(DataConverter<E, F> c1, DataConverter<F, G> c2) {
		this.c1 = c1;
		this.c2 = c2;
	}

	@Override
	public G serialize(E data) {
		return c2.serialize(c1.serialize(data));
	}

	@Override
	public E deserialize(G data) {
		return c1.deserialize(c2.deserialize(data));
	}
}
