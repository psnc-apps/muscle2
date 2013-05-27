/*
* Copyright 2008, 2009 Complex Automata Simulation Technique (COAST) consortium
* Copyright 2010-2013 Multiscale Applications on European e-Infrastructures (MAPPER) project
*
* GNU Lesser General Public License
* 
* This file is part of MUSCLE (Multiscale Coupling Library and Environment).
* 
* MUSCLE is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
* 
* MUSCLE is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Lesser General Public License for more details.
* 
* You should have received a copy of the GNU Lesser General Public License
* along with MUSCLE.  If not, see <http://www.gnu.org/licenses/>.
*/
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
