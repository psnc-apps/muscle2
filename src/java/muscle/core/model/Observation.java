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
	@SuppressWarnings("unchecked")
	public final static Observation EMPTY = new Observation(null, null, null);
	
	/** Creates a new observation that uses the same data as the source.
	 * @param newData the data
	 * @param newSITime the time that the data was observed
	 * @param newNextSITime the time that the next observation will be done
	 */
	public Observation(T newData, Timestamp newSITime, Timestamp newNextSITime) {
		this(newData, newSITime, newNextSITime, false, true);
	}

	/** Creates a new observation that may be copied to ensure independence.
	 * @param newData the data
	 * @param newSITime the time that the data was observed
	 * @param newNextSITime the time that the next observation will be done
	 * @param newIsIndependent whether the given data is independent from the source data, in other words, if it needs to be copied to be guaranteed to be independent
	 */
	public Observation(T newData, Timestamp newSITime, Timestamp newNextSITime, boolean newIsIndependent) {
		this(newData, newSITime, newNextSITime, newIsIndependent, true);
	}
	
	/** Creates a new observation.
	 * @param newData the data
	 * @param newSITime the time that the data was observed
	 * @param newNextSITime the time that the next observation will be done
	 * @param newIsIndependent whether the given data is independent from the source data, in other words, if it needs to be copied to be guaranteed to be independent
	 * @param newMayCopy whether the data should be copied by MUSCLE to ensure independence, as a best effort scheme
	 */
	public Observation(T newData, Timestamp newSITime, Timestamp newNextSITime, boolean newIsIndependent, boolean newMayCopy) {
		siTime = newSITime;
		data = newData;
		nextSITime = newNextSITime;
		isIndependent = newIsIndependent;
		mayCopy = newMayCopy;
	}

	/** Get the observed data. */
	public T getData() {
		return data;
	}
	
	/** Get the time that the observation was made. */
	public Timestamp getTimestamp() {
		return siTime;
	}

	/** Get the time that the next observation of the state will be made. */
	public Timestamp getNextTimestamp() {
		return nextSITime;
	}
	
	/** Whether the data is null. */
	public boolean hasNull() {
		return data == null;
	}
	
	/** Sets that if possible, the data will not be copied but given as is in shared memory. */
	public void shouldNotCopy() {
		this.mayCopy = false;
	}
	
	/**
	 * Get an observation that has data independent from the source.
	 * If the observation is instructed that it should not be copied, the original data is returned.
	 * @param converter converter that will be used to copy the data
	 */
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

	/**
	 * Get an observation that has data independent from the source.
	 * If the observation is instructed that it should not be copied, the original data is returned. For optimal performance, use {@see privateCopy(DataConverter)} with a reused
	 * SerializableDataConverter instead, this will cache the datatype and detect and solve if it is inappropriate.
	 * @param type type of data that the observation contains
	 */
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

	/**
	 * Get an observation that has data independent from the source.
	 * If the observation is instructed that it should not be copied, the original data is returned. For
	 * increased performance see {@see privateCopy(SerializableDatatype)} and {@see privateCopy(DataConverter)}.
	 */
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
	
	/** Whether the contained data should be prevented to be copied, on a best effort basis. */
	public boolean mayCopy() {
		return this.mayCopy;
	}
	
	/**
	 * Create an observation with new data but reuse the metadata of the original observation.
	 */
	public <R extends Serializable> Observation<R> copyWithNewData(R newData) {
		boolean nextIndependent = this.isIndependent || (newData != this.data);
		if (isFinestLog && !this.isIndependent && nextIndependent) {
			logger.log(Level.FINEST, "Creating a new {0} with independent data.", this);
		}
		return new Observation<R>(newData, siTime, nextSITime, nextIndependent, this.mayCopy);
	}
	
	/**
	 * Create an observation with the same data but with new timestamps.
	 */
	public Observation<T> copyWithNewTimestamps(Timestamp time, Timestamp nextTime) {
		return new Observation<T>(data, time, nextTime, this.isIndependent, this.mayCopy);
	}

	public String toString() {
		String name = data == null ? "NULL" : data.getClass().getSimpleName();
		return "Observation<"+siTime+",type="+name+">";
	}	
}

