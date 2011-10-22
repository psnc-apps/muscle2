/*
Copyright 2008,2009 Complex Automata Simulation Technique (COAST) consortium

GNU Lesser General Public License

This file is part of MUSCLE (Multiscale Coupling Library and Environment).

    MUSCLE is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    MUSCLE is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with MUSCLE.  If not, see <http://www.gnu.org/licenses/>.
*/

package muscle.core.wrapper;

import muscle.core.messaging.Timestamp;

/**
container for a data message
@author Jan Hegewald
*/
public class Observation<T> implements java.io.Serializable {
	private Timestamp siTime; // global time where this data belongs to (may be null)
	private T data; // our unwrapped data

	public Observation(T newData, Timestamp newSITime) {
		siTime = newSITime;
		data = newData;
	}

	public T getData() {
		return data;
	}

	public Timestamp getTimestamp() {
		return siTime;
	}

	public String toString() {
		return getClass().getName()+" time:"+siTime+" dataclass:"+data.getClass();
	}	
}

