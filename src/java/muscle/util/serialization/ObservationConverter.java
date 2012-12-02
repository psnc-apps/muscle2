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
		return data.copyWithNewData(this.converter.serialize(data.getData()));
	}

	@Override
	public Observation<E> deserialize(Observation<F> data) {
		return data.copyWithNewData(this.converter.deserialize(data.getData()));
	}
}
