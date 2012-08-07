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

package examples.simplecpp;

import muscle.core.JNIConduitEntrance;
import muscle.core.Scale;
import muscle.core.kernel.CAController;
import muscle.core.model.Distance;


/**
example of a kernel which is using native code to send and receive data
@author Jan Hegewald
*/
public class Sender extends CAController {
	static {
		System.loadLibrary("simplecpp");
	}

	private JNIConduitEntrance<double[],double[]> entrance;

	private native void callNative(JNIConduitEntrance entranceJref);	

	public Scale getScale() {
		Distance delta = new Distance(1);
		return new Scale(delta,delta);
	}

	public void addPortals() {
		entrance = addJNIEntrance("data", double[].class);
	}

	protected void execute() {
		callNative(entrance);	
	}
}
