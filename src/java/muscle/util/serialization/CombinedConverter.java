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
/*
 * 
 */
package muscle.util.serialization;

/**
 *
 * @author Joris Borgdorff
 */
public class CombinedConverter<E, F, G> extends AbstractDataConverter<E,G> {
	private DataConverter<E, F> c1;
	private DataConverter<F, G> c2;
	
	public CombinedConverter(DataConverter<E, F> c1, DataConverter<F, G> c2) {
		this.c1 = c1;
		this.c2 = c2;
	}

	@Override
	public G serialize(E data) {
		return c2.serialize(c1.serialize(data));
	}

	@Override
	public E deserialize(G data) {
		return c1.deserialize(c2.deserialize(data));
	}
}
