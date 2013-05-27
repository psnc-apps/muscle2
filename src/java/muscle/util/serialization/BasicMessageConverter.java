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
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package muscle.util.serialization;

import java.io.Serializable;
import muscle.client.communication.message.BasicMessage;
import muscle.core.model.Observation;

/**
 *
 * @author Joris Borgdorff
 */
public class BasicMessageConverter<E extends Serializable,F extends Serializable> extends AbstractDataConverter<BasicMessage<E>,BasicMessage<F>> {
	private final DataConverter<E, F> subconverter;
	public BasicMessageConverter(DataConverter<E,F> subconverter) {
		this.subconverter = subconverter;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public BasicMessage<F> serialize(BasicMessage<E> data) {
		if (data.isSignal()) {
			return (BasicMessage<F>)data;
		} else {
			Observation<E> obs = data.getObservation();
			F newData = subconverter.serialize(obs.getData());
			return new BasicMessage<F>(obs.copyWithNewData(newData), data.getRecipient());
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public BasicMessage<E> deserialize(BasicMessage<F> data) {
		if (data.isSignal()) {
			return (BasicMessage<E>)data;
		} else {
			Observation<F> obs = data.getObservation();
			E newData = subconverter.deserialize(obs.getData());
			return new BasicMessage<E>(obs.copyWithNewData(newData), data.getRecipient());
		}
	}
}
