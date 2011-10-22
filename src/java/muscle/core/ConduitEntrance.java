/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package muscle.core;

<<<<<<< HEAD
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import muscle.core.conduit.filter.QueueConsumer;
import muscle.core.ident.Identifier;
import muscle.core.messaging.BasicMessage;
import muscle.core.messaging.Duration;
import muscle.core.messaging.Message;
import muscle.core.messaging.Timestamp;
import muscle.core.wrapper.Observation;
=======
import muscle.core.ident.PortalID;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import muscle.Constant;
import muscle.core.kernel.InstanceController;
import muscle.core.messaging.RemoteDataSinkHead;
import muscle.core.messaging.jade.DataMessage;
import muscle.core.messaging.serialization.ACLConverter;
import muscle.core.wrapper.DataWrapper;
import muscle.exception.MUSCLERuntimeException;
>>>>>>> a8c652eb292cdbcf135af6155a23c69c08aef9c0

/**
 * Sends information over a conduit
 * @author Joris Borgdorff
 */
<<<<<<< HEAD
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
	
=======
public class ConduitEntrance<T extends java.io.Serializable> extends Portal<T> implements RemoteDataSinkHead<DataMessage<DataWrapper<T>>> {// generic T will be the underlying unwrapped data, e.g. double[]

	private EntranceDependency[] dependencies;
	private AID dstAgent;
	private String dstSink;
	private DataMessage<DataWrapper<T>> dataMessage;
	private boolean shouldPause = false;

	public ConduitEntrance(PortalID newPortalID, InstanceController newOwnerAgent, int newRate, DataTemplate newDataTemplate, EntranceDependency... newDependencies) {
		super(newPortalID, newOwnerAgent, newRate, newDataTemplate);

		dependencies = newDependencies; // dependencies.length == 0 if there are no EntranceDependency references in argument list		
	}

	@Override
	public AID dstAgent() {
		return dstAgent;
	}

	@Override
	public synchronized void pause() {
		shouldPause = true;
		this.notifyAll();
	}

	@Override
	public synchronized void resume() {
		shouldPause = false;
	}

	@Override
	public void put(DataMessage<DataWrapper<T>> dmsg) {
		if (shouldPause) {
			synchronized (this) {
				while (shouldPause) {
					try {
						this.wait();
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					}
				}
			}
		}

		ownerAgent.sendMessage(dmsg);
	}

	@Override
	public DataMessage<DataWrapper<T>> take() {
		throw new java.lang.UnsupportedOperationException("can not take from " + getClass());
	}

	@Override
	public DataMessage<DataWrapper<T>> poll() {
		throw new java.lang.UnsupportedOperationException("can not poll from " + getClass());
	}

	public DataMessage<DataWrapper<T>> poll(long time, TimeUnit unit) throws InterruptedException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public String id() {
		return dstSink;
	}

	public void setDestination(AID newDstAgent, String newDstSink) {
		// allow only once to connect this sender
		if (dstAgent != null) {
			throw new IllegalStateException("already connected to <" + dstAgent + ":" + dstSink + ">");
		}

		dstAgent = newDstAgent;
		dstSink = newDstSink;

		// set up message dummy for outgoing data messages
		dataMessage = new DataMessage(id());
		dataMessage.addReceiver(dstAgent());
	}

	/**
	pass raw unwrapped data to this entrance
	 */
>>>>>>> a8c652eb292cdbcf135af6155a23c69c08aef9c0
	public void send(T data) {
		this.send(data, nextTime);
	}
	
	public void send(T data, Timestamp currentTime) {
		nextTime = nextTime.add(dt);
		this.send(data, currentTime, nextTime);		
	}

<<<<<<< HEAD
	public void send(T data, Timestamp currentTime, Timestamp next) {
		this.nextTime = next;
		Observation<T> msg = new Observation<T>(data, currentTime, next);
		this.send(msg);
=======
	public void detachDestination() {
		assert ownerAgent != null;
		// if we are connected to a conduit, tell conduit to detach this exit		
		if (dstAgent != null) {
			ownerAgent.sendData(getDetachDstMessage());
		}

		dstAgent = null;
>>>>>>> a8c652eb292cdbcf135af6155a23c69c08aef9c0
	}
	
	private void send(Observation<T> msg) {
		System.out.println("Sending observation...");
		this.queue.add(msg);
		this.consumer.apply();
	}
}
