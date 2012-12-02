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
package muscle.client.instance;

import eu.mapperproject.jmml.util.FastArrayList;
import java.io.Serializable;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import muscle.client.LocalManager;
import muscle.client.communication.Transmitter;
import muscle.client.communication.message.DetachConduitSignal;
import muscle.core.ConduitDescription;
import muscle.core.ConduitEntrance;
import muscle.core.ConnectionScheme;
import muscle.core.DataTemplate;
import muscle.core.conduit.filter.FilterChain;
import muscle.core.kernel.InstanceController;
import muscle.core.model.Observation;
import muscle.id.PortalID;
import muscle.util.data.SingleProducerConsumerBlockingQueue;
import muscle.util.serialization.DataConverter;
import muscle.util.serialization.SerializableDataConverter;

/**
this is the (remote) head of a conduit,
an entrance sends data to the conduit exit through a transmitter
@author Jan Hegewald
 */
public class ThreadedConduitEntranceController<T extends Serializable> extends ThreadedPortal<T>  implements ConduitEntranceControllerImpl<T> {// generic T will be the underlying unwrapped data, e.g. double[]
	private ConduitEntrance<T> conduitEntrance;
	private Transmitter<T,?> transmitter;
	private final static Logger logger = Logger.getLogger(ThreadedConduitEntranceController.class.getName());
	private boolean processingMessage;
	private final DataConverter<T,?> serializer;
	private BlockingQueue<Observation<T>> queue;
	private boolean isSharedData;
	private final FilterChain filters;
	
	public ThreadedConduitEntranceController(PortalID newPortalID, InstanceController newOwnerAgent, DataTemplate newDataTemplate) {
		super(newPortalID, newOwnerAgent, newDataTemplate);
		this.transmitter = null;
		this.conduitEntrance = null;
		this.processingMessage = false;
		this.serializer = new SerializableDataConverter<T>();
		this.queue = new SingleProducerConsumerBlockingQueue<Observation<T>>();
		this.isSharedData = false;
		this.filters = createFilterChain();
	}
	
	/** Create a filter chain from the given arguments */
	private FilterChain createFilterChain() {
		ConnectionScheme cs = ConnectionScheme.getInstance();
		ConduitDescription cd = cs.entranceDescriptionForPortal(portalID).getConduitDescription();
		List<String> args = cd.getArgs();
		int entranceArgDiv = args.indexOf("");
		if (entranceArgDiv < 1) return null;
		
		List<String> entranceArgs = new FastArrayList<String>(entranceArgDiv);
		for (int i = 0; i < entranceArgDiv; i++) {
			entranceArgs.add(args.get(i));
		}
		
		FilterChain fc = new FilterChain() {
			protected void apply(Observation subject) {
				transmitter.transmit(subject);
			}
		};
		
		fc.init(entranceArgs);
		logger.log(Level.INFO, "The conduit entrance ''{0}'' will use filter(s) {1}.", new Object[] {cd, entranceArgs});
		return fc;
	}

	/** Set the transmitter that will be used to transmit messages. Before this
	 * is called, the conduit will not be able to send messages.
	 */
	public synchronized void setTransmitter(Transmitter<T,?> trans) {
		logger.log(Level.FINE, "ConduitEntrance <{0}> is now attached.", portalID);
		this.transmitter = trans;
		this.notifyAll();
	}
	
	/** Set the entrance that will be the interface for the CAController. Before
	 *  this is called, messages are not added. */
	public void setEntrance(ConduitEntrance<T> entrance) {		
		this.conduitEntrance = entrance;
	}
	
	/** Get the entrance that is the interface for the CAController. */
	public ConduitEntrance<T> getEntrance() {
		return this.conduitEntrance;
	}
	
	/**
	 * Processing messages. When messages are finished processing, the conduitEntrance
	 * takes note of this.
	 */
	@Override
	protected void execute() throws InterruptedException {
		boolean running;
		synchronized (this) {
			running = queue != null && !queue.isEmpty();
		}
		while (running) {
			Observation<T> elem = null;
			synchronized (this) {
				this.processingMessage = true;
				if (queue != null) {
					elem = queue.remove();
				}
			}
			
			if (elem == null) {
				logger.log(Level.WARNING, "Can not send null data through ConduitEntrance {0}", this);
				continue;
			}
			
			try {
				if (filters == null) {
					this.transmit(elem);
				} else {
					filters.process(elem);
				}
			} catch (Throwable ex) {
				// Could not transmit message; this is fatal.
				LocalManager.getInstance().shutdown(5);
			}
			synchronized (this) {
				this.processingMessage = false;
				running = queue != null && !queue.isEmpty();
			}
		}
		synchronized (this) {
			this.notifyAll();
		}
	}
	
	/** Waits until no more messages have to be sent. */
	public synchronized boolean waitUntilEmpty() throws InterruptedException {
		while (!isDisposed() && (this.processingMessage || (queue != null && !queue.isEmpty()))) {
			wait();
		}
		return !this.processingMessage && (queue == null || queue.isEmpty());
	}
	
	@Override
	protected void handleInterruption(InterruptedException ex) {
		logger.log(Level.WARNING, "ConduitEntranceController " + this + " interrupted", ex);
	}
	
	/** Waits for a resume call if the thread was paused. Returns the transmitter if the thread is no longer paused and false if the thread should stop. */
	private synchronized Transmitter<T, ?> waitForTransmitter() throws InterruptedException {
		while (transmitter == null && !isDisposed()) {
			if (logger.isLoggable(Level.FINE)) {
				String msg = "ConduitEntrance <" + portalID + "> is waiting for connection to transmit ";
				if (queue != null) {
					msg += (1 + queue.size()) + " messages ";
				}
				msg += "over.";
				logger.fine(msg);
			}
			this.wait(WAIT_FOR_ATTACHMENT_MILLIS);
		}
		return transmitter;
	}

	@Override
	public synchronized void dispose() {
		// if we are connected to a conduit, tell conduit to detach this exit
		if (transmitter != null) {
			try {
				transmitter.signal(new DetachConduitSignal());
			} catch (Exception ex) {
				logger.log(Level.WARNING, "Could not detach " + this.portalID, ex);
			}
			transmitter.dispose();
			transmitter = null;
		}
		if (filters != null) {
			filters.dispose();
		}
		queue = null;
		
		super.dispose();
	}
	
	/**
	 * Actually send message to transmitter.
	 */
	private void transmit(Observation<T> msg) throws InterruptedException {
		Transmitter<T, ?> trans = waitForTransmitter();
		if (trans != null) {
			trans.transmit(msg);
			increment();
		}
	}
	
	public String toString() {
		return "threaded-out:" + this.getIdentifier();
	}

	@Override
	public void send(Observation<T> dmsg) {
		if (this.isSharedData) {
			dmsg.shouldNotCopy();
		}
		// We need to copy the data so that the sending process can modify the data again.
		Observation<T> msg = dmsg.privateCopy(serializer);
		
		try {
			this.queue.put(msg);
		} catch (InterruptedException ex) {
			throw new IllegalStateException("Can not send message", ex);
		}
		
		// Update the willStop timestamp as soon as the message is sent by the Instance, not when it is processed.
		this.resetTime(msg.getNextTimestamp());

		// Make available for processing
		this.trigger();
	}
	
	public void setSharedData() {
		this.isSharedData = true;
	}
}
