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

package muscle.core;

import java.io.IOException;

import muscle.core.kernel.RawKernel;
import muscle.core.messaging.BufferingRemoteDataSinkTail;
import muscle.core.messaging.RemoteDataSinkTail;
import muscle.core.messaging.SinkObserver;
import muscle.core.messaging.jade.DataMessage;
import muscle.core.wrapper.DataWrapper;


/**
this is the (remote) tail of a conduit,
an exit receives data from the conduit agent
@author Jan Hegewald
*/
public class ConduitExit<T> extends Portal<T> implements RemoteDataSinkTail<DataMessage<DataWrapper<T>>> {// generic T will be the underlying unwrapped data, e.g. double[]

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private RemoteDataSinkTail<DataMessage<DataWrapper<T>>> sinkDelegate;

	//
	public ConduitExit(PortalID newPortalID, RawKernel newOwnerAgent, int newRate, DataTemplate<T> newDataTemplate) {
		super(newPortalID, newOwnerAgent, newRate, newDataTemplate);

		this.sinkDelegate = new BufferingRemoteDataSinkTail<DataMessage<DataWrapper<T>>>(this.getLocalName());

      SinkObserver<DataMessage<?>> sinkObserver = newOwnerAgent;
		this.sinkDelegate.addObserver(sinkObserver);
	}


	//
   public void put(DataMessage<DataWrapper<T>> d) {

//		if( messageQueue.size() > QUEUE_WARNING_THRESHOLD )
//			logger.info("["+getLocalName()+"] <"+messageQueue.size()+"> messages in exit queue");

		this.sinkDelegate.put(d);
	}


   //
   public DataMessage<DataWrapper<T>> take() {
		return this.sinkDelegate.take();
   }


   //
   public DataMessage<DataWrapper<T>> poll() {
		return this.sinkDelegate.poll();
   }


   //
   public String id() {
		return this.sinkDelegate.id();
   }


	/**
	return our unwrapped data once
	*/
	public T receive() {

		DataMessage<DataWrapper<T>> dmsg = this.take();

		DataWrapper<T> wrapper = dmsg.getStored();
		T data = wrapper.getData();

		assert this.getDataTemplate().getDataClass().isInstance(data);

		this.increment();

		return data;
	}


	// deserialize
	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {

		// do default deserialization
		in.defaultReadObject();
	}

   public void addObserver(SinkObserver<DataMessage<?>> o) {
      this.sinkDelegate.addObserver(o);
   }

}
