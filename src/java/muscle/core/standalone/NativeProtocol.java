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

import muscle.util.serialization.Protocol;

/**
 *
 * @author joris
 */
 public enum NativeProtocol implements Protocol {
	FINALIZE(0),
	GET_KERNEL_NAME(1),
	GET_PROPERTY(2),
	HAS_PROPERTY(10),
	WILL_STOP(3),
	SEND(4),
	RECEIVE(5),
	GET_PROPERTIES(6),
	GET_TMP_PATH(7),
	HAS_NEXT(8),
	GET_LOGLEVEL(9);
	private final int num;

	NativeProtocol(int num) { this.num = num; }
	@Override
	public int intValue() { return num; }
}
