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

package examples.dataintegrity;

import muscle.core.standalone.NativeKernel;
import muscle.util.data.SerializableData;
import muscle.util.data.SerializableDatatype;

/**
example of a kernel which is using native code to send and receive data using a new Native MUSCLE API
@author Mariusz Mamonski
*/
public class Check extends NativeKernel {
	
//	@Override
//	public void send(String entranceName, SerializableData data) {
//		if (data.getType() == SerializableDatatype.STRING) {
//			System.out.println("Sending string '" + data.getValue() + "' of size " + data.getSize());
//		}
//		super.send(entranceName, data);
//	}
	
}
