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

package examples.transmutable;

import muscle.core.JNIConduitEntrance;
import muscle.core.JNIConduitExit;
import muscle.util.serialization.DataConverter;
import muscle.util.serialization.DoubleStringConverter;


/**
example of a kernel which is using native code to send and receive data to itself<br>
this example uses a Transmutable to map cpp data to a Java class
@author Jan Hegewald
*/
public class Kernel extends muscle.core.kernel.CAController {

	static {
		System.loadLibrary("transmutable");
	}

	private JNIConduitEntrance<double[],String> entrance;
	private JNIConduitExit<String,double[]> exit;
	
	private native void callNative(int length, JNIConduitExit exitJref, JNIConduitEntrance entranceJref);

	public void addPortals() {
		DataConverter<double[], String> dc = new DoubleStringConverter();
		entrance = addJNIEntrance("writer", double[].class, String.class, dc);

		exit = addJNIExit("reader", String.class, double[].class, dc);
	}

	protected void execute() {
		Object[] args = getArguments();
		if(args.length > 0) {
			System.out.println("first kernel arg: "+(String)args[0]);
		}

		int length = 4;

		callNative(length, exit, entrance);	
	}
}
