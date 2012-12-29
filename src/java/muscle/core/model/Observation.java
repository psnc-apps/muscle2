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

package muscle.core.model;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import muscle.util.data.SerializableData;
import muscle.util.data.SerializableDatatype;
import muscle.util.serialization.DataConverter;

/**
 * An immutable container for a data message. Data inside the container should
 * be handled as immutable, until a privateCopy() method is called.
 * @author Joris Borgdorff
 */
public class Observation<T extends Serializable> implements Serializable {
	private final static Logger logger = Logger.getLogger(Observation.class.getName());
	private final static boolean isFinestLog = logger.isLoggable(Level.FINEST);
	private static final long serialVersionUID = 1L;
	private final Timestamp siTime; // global time where this data belongs to (may be null)
	private final Timestamp nextSITime; // time of the next observation
	private final T data; // our unwrapped data
	private transient boolean isIndependent = false;
	private transient boolean mayCopy = true;
	
	public Observation(T newData, Timestamp newSITime, Timestamp newNextSITime) {
		this(newData, newSITime, newNextSITime, false, true);
	}

	public Observation(T newData, Timestamp newSITime, Timestamp newNextSITime, boolean newIsIndependent) {
		this(newData, newSITime, newNextSITime, newIsIndependent, true);
	}
	
	public Observation(T newData, Timestamp newSITime, Timestamp newNextSITime, boolean newIsIndependent, boolean newMayCopy) {
		siTime = newSITime;
		data = newData;
		nextSITime = newNextSITime;
		isIndependent = newIsIndependent;
		mayCopy = newMayCopy;
	}

	public T getData() {
		return data;
	}
	
	public boolean hasNull() {
		return data == null;
	}
	
	public Timestamp getTimestamp() {
		return siTime;
	}

	public void shouldNotCopy() {
		this.mayCopy = false;
	}
	
	public Timestamp getNextTimestamp() {
		return nextSITime;
	}
	
	public Observation<T> privateCopy(DataConverter<T,?> converter) {
		if (this.isIndependent || !this.mayCopy) {
			return this;
		} else {
			if (isFinestLog) {
				logger.log(Level.FINEST, "Making a copy of {0} to ensure independence.", this);
			}
			T copyData = converter.copy(data);
			return this.copyWithNewData(copyData);
		}
	}

	public Observation<T> privateCopy(SerializableDatatype type) {
		if (this.isIndependent || !this.mayCopy || data == null) {
			return this;
		} else {
			if (isFinestLog) {
				logger.log(Level.FINEST, "Making a copy of {0} to ensure independence.", this);
			}

			T copyData = SerializableData.createIndependent(data, type);
			return this.copyWithNewData(copyData);
		}
	}

	public Observation<T> privateCopy() {
		if (this.isIndependent || !this.mayCopy) {
			return this;
		} else {
			if (isFinestLog) {
				logger.log(Level.FINEST, "Making a copy of {0} to ensure independence.", this);
			}
			T copyData = SerializableData.createIndependent(data);
			return this.copyWithNewData(copyData);
		}
	}
	
	public void setIndependent() {
		this.isIndependent = true;
	}
	
	/**
	 * Whether data contained in the observation can be modified independently
	 * of the submodel that was observed.
	 * 
	 * In other words, if the pointer to the data is the same pointer as the submodel
	 * uses, it is not independent.
	 */
	public boolean isIndependent() {
		return this.isIndependent;
	}
	
	public boolean mayCopy() {
		return this.mayCopy;
	}
	
	/**
	 * Create a new observation but reuse the metadata of the original data.
	 */
	public <R extends Serializable> Observation<R> copyWithNewData(R newData) {
		boolean nextIndependent = this.isIndependent || (newData != this.data);
		if (isFinestLog && !this.isIndependent && nextIndependent) {
			logger.log(Level.FINEST, "Creating a new {0} with independent data.", this);
		}
		return new Observation<R>(newData, siTime, nextSITime, nextIndependent, this.mayCopy);
	}
	
	/**
	 * Create a new observation with the same data but with different timestamps.
	 */
	public Observation<T> copyWithNewTimestamps(Timestamp time, Timestamp nextTime) {
		return new Observation<T>(data, time, nextTime, this.isIndependent, this.mayCopy);
	}

	public String toString() {
		String name = data == null ? "NULL" : data.getClass().getSimpleName();
		return "Observation<"+siTime+",type="+name+">";
	}	
}

