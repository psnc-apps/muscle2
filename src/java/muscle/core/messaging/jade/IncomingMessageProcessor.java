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

package muscle.core.messaging.jade;

import java.util.Queue;

import muscle.core.MultiDataAgent;


/**
process the agent message queue from a sub-thread of the agents main thread
this allows us to actively push messages to their individual sinks
@author Jan Hegewald
*/
public class IncomingMessageProcessor extends java.lang.Thread {


	private MultiDataAgent owner;
	private boolean shouldRun = true;
	private Queue<DataMessage> queue;


	//
	public IncomingMessageProcessor(MultiDataAgent newOwner, Queue<DataMessage> newQueue) {

		this.owner = newOwner;
		this.queue = newQueue;
	}


	//
	public synchronized void pause() {

		this.shouldRun = false;
	}


//	//
//	public synchronized void proceed() {
//		shouldRun = true;
//		run();
//	}


	//
   @Override
   public void run() {

		this.owner.getLogger().info("starting "+this.getClass());

		long milliDelay = 1;
		int nanoDelay = 0;

		while(this.shouldRun) {
			// blocking poll for next message


			// todo: use Object#wait and Object#notify instead of Thread#sleep
			DataMessage dmsg = null;
			while( this.shouldRun && ((dmsg = this.queue.poll()) == null) ) {
				try {
					Thread.sleep(milliDelay, nanoDelay);
				}
				catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}

			if(dmsg != null) {
				this.owner.handleDataMessage(dmsg);
			}
		}
	}

}



