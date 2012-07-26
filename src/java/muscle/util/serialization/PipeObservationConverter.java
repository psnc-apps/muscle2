/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package muscle.util.serialization;

import java.io.Serializable;
import muscle.core.model.Observation;

/**
 *
 * @author Joris Borgdorff
 */
public class PipeObservationConverter<T extends Serializable> extends ObservationConverter<T,T> {
	public PipeObservationConverter(DataConverter<T,T> converter) {
		super(converter);
	}
	public Observation<T> serialize(Observation<T> data) {
		return data.copyWithNewData(this.converter.copy(data.getData()));
	}
}
