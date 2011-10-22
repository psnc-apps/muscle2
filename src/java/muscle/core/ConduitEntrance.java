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

/**
 * Sends information over a conduit
 * @author Joris Borgdorff
 */
public class ConduitEntrance<T> {
	private final QueueConsumer<Message<T>> consumer;
	private final Queue<Message<T>> queue;
	protected Timestamp nextTime;
	protected final Duration dt;
	
	public ConduitEntrance(QueueConsumer<Message<T>> controller) {
		this(controller, new Timestamp(0d), new Duration(1d));
	}
	
	public ConduitEntrance(QueueConsumer<Message<T>> controller, Timestamp origin, Duration timeStep) {
		this.queue = new LinkedBlockingQueue<Message<T>>();
		
		this.nextTime = origin;
		this.dt = timeStep;
		
		this.consumer = controller;
		this.consumer.setIncomingQueue(this.queue);
	}
	
	public void send(T data) {
		Timestamp currentTime = nextTime;
		nextTime = nextTime.add(dt);
		Message<T> msg = new BasicMessage<T>(data, currentTime, nextTime, null);
		this.send(msg);
	}
	
	public void send(Message<T> msg) {
		this.queue.add(msg);
		this.consumer.apply();
	}
}
