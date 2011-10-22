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

import muscle.core.ident.PortalID;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import muscle.core.kernel.InstanceController;
import muscle.core.kernel.RawKernel;
import muscle.core.messaging.BufferingRemoteDataSinkTail;
import muscle.core.messaging.RemoteDataSinkTail;
import muscle.core.messaging.SinkObserver;
import muscle.core.messaging.jade.DataMessage;
import muscle.core.wrapper.DataWrapper;
import muscle.exception.MUSCLERuntimeException;


/**
this is the (remote) tail of a conduit,
an exit receives data from the conduit agent
@author Jan Hegewald
*/
public class ConduitExit<T> extends Portal<T> implements RemoteDataSinkTail<DataMessage<DataWrapper<T>>> {// generic T will be the underlying unwrapped data, e.g. double[]
	
	private transient Logger logger;
	private RemoteDataSinkTail<DataMessage<DataWrapper<T>>> sinkDelegate;

	//
	public ConduitExit(PortalID newPortalID, InstanceController newOwnerAgent, int newRate, DataTemplate newDataTemplate) {
		super(newPortalID, newOwnerAgent, newRate, newDataTemplate);
				
		if(newOwnerAgent != null)
			logger = newOwnerAgent.getLogger();
		else
			logger = muscle.logging.Logger.getLogger(getClass());
			
		sinkDelegate = new BufferingRemoteDataSinkTail<DataMessage<DataWrapper<T>>>(getLocalName());
      
      SinkObserver<DataMessage<?>> sinkObserver = (SinkObserver<DataMessage<?>>)newOwnerAgent;
		sinkDelegate.addObserver(sinkObserver);
	}


	//
   @Override
   public void put(DataMessage<DataWrapper<T>> d) {

//		if( messageQueue.size() > QUEUE_WARNING_THRESHOLD )
//			logger.info("["+getLocalName()+"] <"+messageQueue.size()+"> messages in exit queue");

		sinkDelegate.put(d);
	}


   //
   @Override
   public DataMessage<DataWrapper<T>> take() throws InterruptedException {
		return sinkDelegate.take();
   }


   //
   @Override
   public DataMessage<DataWrapper<T>> poll() {
		return sinkDelegate.poll();
   }


   //
   @Override
   public String id() {
		return sinkDelegate.id();
   }

	
	/**
	return our unwrapped data once
	*/
	public T receive() {
	
		DataMessage<DataWrapper<T>> dmsg = null;
		try {
			dmsg = take();
		} catch (InterruptedException ex) {
			throw new MUSCLERuntimeException(ex);
		}
		
		DataWrapper<T> wrapper = dmsg.getData();
		T data = wrapper.getData();
		
		assert getDataTemplate().getDataClass().isInstance(data);

		increment();
		
		return data;
	}
	

	// deserialize
	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
		
		// do default deserialization
		in.defaultReadObject();
		
		// init transient fields
		// can not use agent logger here, because exit has no access to owner agent
		logger = muscle.logging.Logger.getLogger(ConduitExit.class);
	}

   @Override
   public void addObserver(SinkObserver<DataMessage<?>> o) {
      sinkDelegate.addObserver(o);
   }

	@Override
	public DataMessage<DataWrapper<T>> poll(long time, TimeUnit unit) throws InterruptedException {
		throw new UnsupportedOperationException("Not supported yet.");
	}
	
}
