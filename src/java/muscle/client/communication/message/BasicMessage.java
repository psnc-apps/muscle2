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
package muscle.client.communication.message;

import java.io.Serializable;
import muscle.core.model.Observation;
import muscle.id.Identifier;

/**
 * A message containing either a data or a signal, associated with a recipient.
 * 
 * @author Joris Borgdorff
 * @param <E> Type of the data in the message
 */
public class BasicMessage<E extends Serializable> implements Message<E> {
	private static final long serialVersionUID = 1L;
	private final Observation<E> obs;
	private final Identifier recv;
	private final Signal signal;
	
	public BasicMessage(Signal s, Identifier recipient) {
		this.signal = s;
		this.recv = recipient;
		this.obs = null;
	}
	
	public BasicMessage(Observation<E> obs, Identifier recipient) {
		this.obs = obs;
		this.recv = recipient;
		this.signal = null;
	}

	@Override
	public E getRawData() {
		return this.obs.getData();
	}

	@Override
	public Observation<E> getObservation() {
		return this.obs;
	}

	@Override
	public Identifier getRecipient() {
		return this.recv;
	}

	@Override
	public boolean isSignal() {
		return this.signal != null;
	}

	@Override
	public Signal getSignal() {
		return this.signal;
	}
	
	@Override
	public String toString() {
		if (isSignal()) {
			return "BasicMessage<" + this.signal + "," + this.recv + ">";
		} else {
			return "BasicMessage<" + this.obs + "," + this.recv + ">";
		}
	}
}
