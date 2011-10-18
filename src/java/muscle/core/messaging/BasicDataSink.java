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


/**
@author Jan Hegewald
*/
public class BasicDataSink<E> implements DataSink<E> {

	E data;
	String id;
	
	
	//
	public BasicDataSink(String newID) {
		id = newID;
	}


	// returns first element or null
	public E poll() {
		
		E d = data;
		data = null;
		return d;
	}
	
	
	// blocking put
	public void put(E d) {
		
		while( data != null ) {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}			
		}
		
		data = d;
	}
	

	// blocking poll, returns first element or blocks
	public E take() {

		E d = null;
		while( (d = poll()) == null ) {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}			
		}
		
		data = null;
		return d;
	}


	//
	public String id() {
		
		return id;
	}
}



