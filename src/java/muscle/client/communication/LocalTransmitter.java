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
import muscle.client.communication.message.BasicMessage;
import muscle.client.communication.message.Message;
import muscle.client.communication.message.Signal;
import muscle.core.model.Observation;
import muscle.id.PortalID;
import muscle.util.serialization.DataConverter;

/**
 * Transmits data within a MUSCLE process by sending it to the local data
 * handler, after the converter has the possibility to make a copy.
 * 
 * @author Joris Borgdorff
 * @param <T> datatype that is transmitted
 */
public class LocalTransmitter<T extends Serializable> extends Transmitter<T, T> {
	private final LocalDataHandler dataHandler;
	
	public LocalTransmitter(LocalDataHandler dataHandler, DataConverter<Observation<T>, Observation<T>> converter, PortalID portalID) {
		super(converter, portalID);
		this.dataHandler = dataHandler;
	}

	@Override
	public final void transmit(Observation<T> obs) {
		// If converter is null here, we made a mistake creating the transmitter.
		// We need to copy here, so that the data on the receiving end is independent from the sending end.
		Message<T> msg = new BasicMessage<T>(converter.serialize(obs), portalID);
		this.dataHandler.put(msg);
	}

	@Override
	public final void signal(Signal signal) {
		@SuppressWarnings("unchecked")
		Message<T> msg = new BasicMessage<T>(signal, portalID);
		this.dataHandler.put(msg);
	}
}
