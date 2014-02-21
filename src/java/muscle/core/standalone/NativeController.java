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

package muscle.core.standalone;

import muscle.util.data.SerializableData;

/** Implement this interface to be able to communicate with a native MUSCLE command. */
public interface NativeController {
	public void isFinished();
	public String getKernelName();
	public String getProperty(String name);
	public boolean hasProperty(String name);
	public boolean willStop();
	public void send(String entranceName, SerializableData data);
	public SerializableData receive(String exitName);
	public String getProperties();
	public String getTmpPath();
	public boolean hasNext(String exitName);
	public int getLogLevel();
	public void fatalException(Throwable thr);	
}
