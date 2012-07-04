/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package muscle.core;

import java.io.Serializable;
import java.util.concurrent.BlockingQueue;
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
 */
public class ConduitEntrance<T extends Serializable> {
	private final ConduitEntranceController<T> consumer;
	protected Timestamp nextTime;
	protected final Distance dt;
	private BlockingQueue<Observation<T>> queue;
	
	public ConduitEntrance(ConduitEntranceController<T> controller, Scale sc) {
		this(controller, new Timestamp(0d), sc == null ? new Distance(1) : sc.getDt());
	}
	
	public ConduitEntrance(ConduitEntranceController<T> controller, Timestamp origin, Distance timeStep) {
		this.nextTime = origin;
		this.dt = timeStep;
		
		this.consumer = controller;
	}
	
	/**
	 * Send a piece of data. This assumes that the current timestep and the next
	 * follow statically from the temporal scale. It is a non-blocking call, meaning
	 * that the function returns almost immediately, no matter when the message is actually sent. To make
	 * this possible, the data is copied before its sent, so that the submodel may make use of it and modify
	 * it without affecting the data sent. It is not necessary to make a copy of the data inside the submodel
	 * before sending.
	 */
	public void send(T data) {
		this.send(data, nextTime);
	}
	
	/**
	 * Send a piece of data at the current timestep. This assumes that the next timestep
	 * follows statically from the temporal scale.
	 * @see send(T)
	 */
	public void send(T data, Timestamp currentTime) {
		Timestamp next = currentTime.add(dt);
		this.send(data, currentTime, next);
	}

	/**
	 * Send a piece of data at the current timestep, also mentioning when the next
	 * piece of data will be sent.
 	 * @see send(T)
	 */
	public void send(T data, Timestamp currentTime, Timestamp next) {
		this.nextTime = next;
		this.consumer.send(data, currentTime, next);
	}
}
