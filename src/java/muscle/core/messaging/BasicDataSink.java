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

package muscle.core.messaging;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import muscle.exception.MUSCLERuntimeException;

/**
@author Jan Hegewald
*/
public class BasicDataSink<E> implements DataSink<E> {
	protected BlockingQueue<E> data;
	protected final String id;
	
	public BasicDataSink(String newID) {
		this(newID, new ArrayBlockingQueue<E>(1));
	}
	
	protected BasicDataSink(String newID, BlockingQueue<E> queue) {
		id = newID;
		data = queue;
	}

	// returns first element or null
	public E poll() {
		return data.poll();
	}
	
	public E poll(long time, TimeUnit unit) throws InterruptedException {
		return data.poll(time, unit);
	}
	
	// blocking put
	public void put(E d) {
		data.add(d);
	}
	
	// blocking poll, returns first element or blocks
	public E take() throws InterruptedException {
		return data.take();
	}

	public final String id() {		
		return id;
	}
}
