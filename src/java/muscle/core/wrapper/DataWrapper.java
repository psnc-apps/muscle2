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

import javax.measure.DecimalMeasure;
import javax.measure.quantity.Duration;


/**
container for a data message
@author Jan Hegewald
*/
public class DataWrapper<T> implements java.io.Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private DecimalMeasure<Duration> siTime; // global time where this data belongs to (may be null)
	private T data; // our unwrapped data


	//
	public DataWrapper(T newData, DecimalMeasure<Duration> newSITime) {

		this.siTime = newSITime;
		this.data = newData;
	}


	//
	public T getData() {

		return this.data;
	}


	//
	public DecimalMeasure<Duration> getSITime() {

		return this.siTime;
	}


	//
	@Override
	public String toString() {

		return this.getClass().getName()+" time:"+this.siTime+" dataclass:"+this.data.getClass();
	}
}

