/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package muscle.core;

import java.io.Serializable;
import java.util.concurrent.BlockingQueue;
import muscle.core.messaging.Duration;
import muscle.core.messaging.Observation;
import muscle.core.messaging.Timestamp;
import muscle.core.messaging.serialization.DataConverter;
import muscle.core.messaging.serialization.SerializableDataConverter;
import utilities.data.SingleProducerConsumerBlockingQueue;

/**
 * Sends information over a conduit
 * @author Joris Borgdorff
 */
public class ConduitEntrance<T extends Serializable> {
	private final ConduitEntranceController<T> consumer;
	protected Timestamp nextTime;
	protected final Duration dt;
	private final DataConverter<T,?> serializer;
	private BlockingQueue<Observation<T>> queue;
	
	public ConduitEntrance(ConduitEntranceController<T> controller, Scale sc) {
		this(controller, new Timestamp(0d), sc.getDt());
	}
	
	public ConduitEntrance(ConduitEntranceController<T> controller, Timestamp origin, Duration timeStep) {
		this.serializer = new SerializableDataConverter<T>();
		this.queue = new SingleProducerConsumerBlockingQueue<Observation<T>>(1024);
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
		nextTime = currentTime.add(dt);
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
		try {
			this.queue.put(msg);
		} catch (InterruptedException ex) {
			throw new IllegalStateException("Can not send message", ex);
		}
		
		// Update the willStop timestamp as soon as the message is sent by the Instance, not when it is processed.
		this.consumer.setNextTimestamp(msg.getNextTimestamp());
		this.consumer.apply();
	}
}
