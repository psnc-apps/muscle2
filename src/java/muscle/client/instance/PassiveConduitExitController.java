/*
* Copyright 2008, 2009 Complex Automata Simulation Technique (COAST) consortium
* Copyright 2010-2013 Multiscale Applications on European e-Infrastructures (MAPPER) project
*
* GNU Lesser General Public License
* 
* This file is part of MUSCLE (Multiscale Coupling Library and Environment).
* 
* MUSCLE is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
* 
* MUSCLE is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Lesser General Public License for more details.
* 
* You should have received a copy of the GNU Lesser General Public License
* along with MUSCLE.  If not, see <http://www.gnu.org/licenses/>.
*/
/*
 * 
 */
package muscle.client.instance;

import eu.mapperproject.jmml.util.FastArrayList;
import java.io.Serializable;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import muscle.client.LocalManager;
import muscle.client.communication.Receiver;
import muscle.client.communication.message.BasicMessage;
import muscle.client.communication.message.DetachConduitSignal;
import muscle.client.communication.message.Message;
import muscle.core.ConduitDescription;
import muscle.core.ConduitExit;
import muscle.core.conduit.filter.FilterChain;
import muscle.core.kernel.InstanceController;
import muscle.core.model.Observation;
import muscle.exception.MUSCLEDatatypeException;
import muscle.id.PortalID;
import muscle.util.data.TakeWrapper;
import muscle.util.data.Takeable;
import muscle.util.serialization.DataConverter;

/**
 *
 * @author Joris Borgdorff
 */
public class PassiveConduitExitController<T extends Serializable> extends PassivePortal<T> implements ConduitExitControllerImpl<T>, Receiver<T,BasicMessage> {
	private static final long serialVersionUID = 1L;
	private ConduitExit<T> conduitExit;
	private final BlockingQueue<Observation<T>> queue;
	private volatile boolean isDone;
	protected DataConverter<Message<T>,BasicMessage> converter;
	private final static Logger logger = Logger.getLogger(PassiveConduitEntranceController.class.getName());
	private final FilterChain filters;

	public PassiveConduitExitController(PortalID newPortalID, InstanceController newOwnerAgent, Class<T> newDataClass, boolean threaded, ConduitDescription desc) {
		super(newPortalID, newOwnerAgent, newDataClass);
		this.queue = new LinkedBlockingQueue<Observation<T>>();
		this.conduitExit = null;
		this.isDone = false;
		this.filters = createFilterChain(desc, threaded);
	}
	
	/** Create a filter chain from the given arguments */
	private FilterChain createFilterChain(ConduitDescription desc, boolean threaded) {
		String[] args = desc.getArgs();
		int exitArgDiv = 0;
		for (int i = 0; i < args.length; i++) {
			if ("".equals(args[i])) {
				exitArgDiv = i + 1;
				break;
			}
		}

		List<String> exitArgs;
		if (threaded) {
			exitArgs = new FastArrayList<String>(args.length - exitArgDiv + 1);
			exitArgs.add("thread");
		} else if (exitArgDiv == args.length) {
			return null;
		} else {
			exitArgs = new FastArrayList<String>(args.length - exitArgDiv);
		}
		for (int i = exitArgDiv; i < args.length; i++) {
			exitArgs.add(args[i]);
		}
		
		FilterChain fc = new FilterChain() {
			@SuppressWarnings("unchecked")
			public void queue(Observation subject) {
				Serializable data = subject.getData();
				if (data != null && dataClass != null && !dataClass.isInstance(data)) {
					throw new MUSCLEDatatypeException("Data type "+ data.getClass().getSimpleName() + " received through conduit exit " + PassiveConduitExitController.this + " does not match expected data type " + dataClass.getSimpleName());
				}

				queue.add(subject);
			}
		};
		
		fc.init(exitArgs);
		logger.log(Level.INFO, "The conduit exit ''{0}'' will use filter(s) {1}.", new Object[] {desc.getExit(), exitArgs});
		return fc;
	}
	
	public void setExit(ConduitExit<T> exit) {
		this.conduitExit = exit;
	}

	public ConduitExit<T> getExit() {
		return this.conduitExit;
	}
	
	@Override
	public synchronized void dispose() {
		if (this.filters != null && !isDisposed()) {
			this.filters.dispose();
		} else {
			this.isDone = true;
		}
	}

	public String toString() {
		return "in:" + this.getLocalName();
	}

	@Override
	public Takeable<Observation<T>> getMessageQueue() {
		return new TakeWrapper<Observation<T>>(this.queue);
	}

	@Override
	public void messageReceived(Observation<T> obs) {
		this.resetTime(obs.getNextTimestamp());
	}

	@Override
	public void put(BasicMessage dmsg) {
		Message<T> msg = converter.deserialize(dmsg);
		if (dmsg != null) {
			if (msg.isSignal()) {
				if (msg.getSignal() instanceof DetachConduitSignal) {
					@SuppressWarnings("unchecked")
					Observation<T> empty = Observation.EMPTY;
					queue.add(empty);
					this.dispose();
				}
			} else {
				if (this.filters == null) {
					Observation<T> subject = msg.getObservation();
					T data = subject.getData();
					if (data != null && dataClass != null && !dataClass.isInstance(data)) {
						throw new MUSCLEDatatypeException("Data type "+ data.getClass().getSimpleName() + " received through conduit exit " + this + " does not match expected data type " + dataClass.getSimpleName());
					}

					this.queue.add(msg.getObservation());
				} else {
					try {
						this.filters.process(msg.getObservation());
					} catch (Throwable ex) {
						logger.log(Level.SEVERE, "Could not filter message " + msg + " properly, probably the coupling is not correct.", ex);
						this.filters.dispose();
						LocalManager.getInstance().shutdown(4);
					}
				}
				increment();
			}
		}
	}

	public void setDataConverter(DataConverter<Message<T>, BasicMessage> serializer) {
		this.converter = serializer;
	}

	@Override
	public synchronized boolean isDisposed() {
		return this.isDone;
	}
		
	@Override
	public Message<T> receive() {
		throw new UnsupportedOperationException("Not supported.");
	}

	@Override
	public void start() {
		// Do nothing
	}
}
