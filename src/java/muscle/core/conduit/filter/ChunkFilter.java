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

package muscle.core.conduit.filter;

import muscle.core.model.Observation;

/**
 *
 * @author Joris Borgdorff
 */
public class ChunkFilter extends AbstractFilter<byte[], byte[]> {
	private final int chunks;
	public ChunkFilter(double chunks) {
		this.chunks = (int)chunks;
	}
	
	@Override
	protected void apply(Observation<byte[]> subject) {
		byte[] data = subject.getData();
		int chunk_sz = data.length / chunks;
		int index = 0;
		for (int i = 0; i < chunks - 1; i++) {
			byte[] chunk = new byte[chunk_sz];
			if (chunk_sz > 0) {
				System.arraycopy(data, index, chunk, 0, chunk_sz);
				index += chunk_sz;
			}
			put(subject.copyWithNewData(chunk));
		}
		
		int final_sz = data.length - index;
		byte[] chunk = new byte[final_sz];
		System.arraycopy(data, index, chunk, 0, final_sz);
		put(subject.copyWithNewData(chunk));
	}
}
