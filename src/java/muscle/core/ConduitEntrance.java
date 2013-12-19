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
package muscle.core;

import java.io.Serializable;
import muscle.core.model.Distance;
import muscle.core.model.Observation;
import muscle.core.model.Timestamp;

/**
 * A ConduitEntrance adds data to a conduit.
 * It has a send() interface with which it can send data messages. To make use of dynamic temporal scales, a
 * timestamp may be given with a piece of data, and if possible also a next timestamp, being the next timestamp
 * at which a piece of data will be sent.
 * 
 * @author Joris Borgdorff
 * @param <T> datatype that will be sent
 */
public class ConduitEntrance<T extends Serializable> {
	private final ConduitEntranceController<T> controller;
	protected Timestamp nextTime;
	protected Distance dt;
	
	public ConduitEntrance(ConduitEntranceController<T> controller, Timestamp origin, Distance timeStep) {
		this.nextTime = origin;
		this.dt = timeStep;
		
		this.controller = controller;
	}
	
	/**
	 * Sets the timestep between messages.
	 * @param dt timestep
	 */
	public void setDt(Distance dt) {
		this.dt = dt;
	}
	
	/**
	 * Send a piece of data. This assumes that the current timestep and the next
	 * follow statically from the temporal scale. It is a non-blocking call, meaning
	 * that the function returns almost immediately, no matter when the message is actually sent. To make
	 * this possible, the data is copied before its sent, so that the submodel may make use of it and modify
	 * it without affecting the data sent. It is not necessary to make a copy of the data inside the submodel
	 * before sending.
	 * @param data data
	 */
	public void send(T data) {
		this.send(data, nextTime, nextTime.add(dt));
	}
	
	/**
	 * Send a piece of data at the current timestep. This assumes that the next timestep
	 * follows statically from the temporal scale.
	 * @param data data
	 * @param currentTime time at which the data is sent
	 * @see send(T)
	 */
	public void send(T data, Timestamp currentTime) {
		this.send(data, currentTime, currentTime.add(dt));
	}

	/**
	 * Send a piece of data at the current timestep, also mentioning when the next
	 * piece of data will be sent.
	 * @param data data
	 * @param currentTime time at which the data is sent
	 * @param next time at which the next message will be sent
 	 * @see send(T)
	 */
	public void send(T data, Timestamp currentTime, Timestamp next) {
		this.nextTime = next;
		this.controller.send(new Observation<T>(data, currentTime, next));
	}
	
	/**
	 * Send an observation with data at the current timestep, also mentioning when the next
	 * piece of message will be sent.
	 * @param obs observation to be sent
 	 * @see send(T)
	 */
	public void send(Observation<T> obs) {
		this.nextTime = obs.getNextTimestamp();
		this.controller.send(obs);
	}
	
	/** Indicate that no more messages will be sent over the current conduit. */
	public void close() {
		this.controller.dispose();
	}
	
	@Override
	public String toString() {
		return this.controller.toString();
	}
}
