/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package muscle.core;

import java.io.Serializable;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import muscle.core.conduit.filter.QueueConsumer;
import muscle.core.messaging.Duration;
import muscle.core.messaging.Observation;
import muscle.core.messaging.Timestamp;
import muscle.core.messaging.serialization.ByteJavaObjectConverter;
import muscle.core.messaging.serialization.DataConverter;

/**
 * Sends information over a conduit
 * @author Joris Borgdorff
 */
public class ConduitEntrance<T extends Serializable> {
	private final QueueConsumer<Observation<T>> consumer;
	protected Timestamp nextTime;
	protected final Duration dt;
	private final DataConverter<T,?> serializer;
	private Queue<Observation<T>> queue;
	
	public ConduitEntrance(ConduitEntranceController<T> controller) {
		this(controller, new Timestamp(0d), new Duration(1d));
	}
	
	public ConduitEntrance(QueueConsumer<Observation<T>> controller, Timestamp origin, Duration timeStep) {
		this.serializer = new ByteJavaObjectConverter<T>();
		this.queue = new LinkedBlockingQueue<Observation<T>>();
		controller.setIncomingQueue(queue);

		this.nextTime = origin;
		this.dt = timeStep;
		
		this.consumer = controller;
	}
	
	/**
	 * Send a piece of data. This assumes that the current timestep and the next
	 * follow statically from the temporal scale.
	 */
	public void send(T data) {
		this.send(data, nextTime);
	}
	
	/**
	 * Send a piece of data at the current timestep. This assumes that the next timestep
	 * follows statically from the temporal scale.
	 */
	public void send(T data, Timestamp currentTime) {
		nextTime = nextTime.add(dt);
		this.send(data, currentTime, nextTime);		
	}

	/**
	 * Send a piece of data at the current timestep, also mentioning when the next
	 * piece of data will be sent.
	 */
	public void send(T data, Timestamp currentTime, Timestamp next) {
		this.nextTime = next;
		T dataCopy = serializer.copy(data);
		Observation<T> msg = new Observation<T>(dataCopy, currentTime, next);
		this.send(msg);
	}
	
	/** Send an observation. */
	private void send(Observation<T> msg) {
		this.queue.add(msg);
		this.consumer.apply();
	}
}
