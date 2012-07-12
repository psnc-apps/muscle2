/*
 * 
 */

package muscle.util.serialization;

import java.io.Serializable;
import muscle.core.model.Observation;

/**
 *
 * @author Joris Borgdorff
 */
public class ObservationConverter<E extends Serializable,F extends Serializable> extends AbstractDataConverter<Observation<E>, Observation<F>> {
	protected final DataConverter<E, F> converter;
	public ObservationConverter(DataConverter<E,F> converter) {
		this.converter = converter;
	}
	@Override
	public Observation<F> serialize(Observation<E> data) {
		return new Observation<F>(this.converter.serialize(data.getData()), data.getTimestamp(), data.getNextTimestamp());
	}

	@Override
	public Observation<E> deserialize(Observation<F> data) {
		return new Observation<E>(this.converter.deserialize(data.getData()), data.getTimestamp(), data.getNextTimestamp());
	}
}
