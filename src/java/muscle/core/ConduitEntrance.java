/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package muscle.core;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import muscle.core.conduit.filter.QueueConsumer;
import muscle.core.ident.Identifier;
import muscle.core.messaging.BasicMessage;
import muscle.core.messaging.Duration;
import muscle.core.messaging.Message;
import muscle.core.messaging.Timestamp;
import muscle.core.wrapper.Observation;

/**
 * Sends information over a conduit
 * @author Joris Borgdorff
 */
public class ConduitEntrance<T> {
	private final QueueConsumer<Observation<T>> consumer;
	private final Queue<Observation<T>> queue;
	protected Timestamp nextTime;
	protected final Duration dt;
	
	public ConduitEntrance(QueueConsumer<Observation<T>> controller) {
		this(controller, new Timestamp(0d), new Duration(1d));
	}
	
	public ConduitEntrance(QueueConsumer<Observation<T>> controller, Timestamp origin, Duration timeStep) {
		this.queue = new LinkedBlockingQueue<Observation<T>>();
		
		this.nextTime = origin;
		this.dt = timeStep;
		
		this.consumer = controller;
		this.consumer.setIncomingQueue(this.queue);
	}
	
	public void send(T data) {
		this.send(data, nextTime);
	}
	
	public void send(T data, Timestamp currentTime) {
		nextTime = nextTime.add(dt);
		this.send(data, currentTime, nextTime);		
	}

	public void send(T data, Timestamp currentTime, Timestamp next) {
		this.nextTime = next;
		Observation<T> msg = new Observation<T>(data, currentTime, next);
		this.send(msg);
	}
	
	private void send(Observation<T> msg) {
		System.out.println("Sending observation...");
		this.queue.add(msg);
		this.consumer.apply();
	}
}
