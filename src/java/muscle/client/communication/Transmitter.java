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
package muscle.client.communication;

import java.io.Serializable;
import muscle.client.communication.message.Signal;
import muscle.core.model.Observation;
import muscle.id.PortalID;
import muscle.util.serialization.DataConverter;

/**
 * Implementation of a point that is communicated from or to, storing a
 * way to convert between the internal data representation and the serialized
 * data.
 * 
 * @author Joris Borgdorff
 * @param <E> datatype that is seen internally
 * @param <F> serialized datatype
 */
public abstract class Transmitter<E extends Serializable,F extends Serializable> implements CommunicatingPoint<Observation<E>,Observation<F>> {
	protected DataConverter<Observation<E>, Observation<F>> converter;
	protected final PortalID portalID;
	private boolean isDone;

	public Transmitter(DataConverter<Observation<E>, Observation<F>> converter, PortalID portalID) {
		this.converter = converter;
		this.portalID = portalID;
		this.isDone = false;
	}
	
	@Override
	public void setDataConverter(DataConverter<Observation<E>,Observation<F>> converter) {
		this.converter = converter;
	}
	
	@Override
	public synchronized void dispose() {
		this.isDone = true;
	}
	
	@Override
	public synchronized boolean isDisposed() {
		return this.isDone;
	}
	
	public abstract void transmit(Observation<E> obs);
	public abstract void signal(Signal signal);
}
