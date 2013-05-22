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
public class DechunkFilter extends AbstractFilter<byte[], byte[]> {
	private final int chunks;
	private byte[] dataCache;
	private int chunksSoFar;
	private int indexSoFar;
	
	public DechunkFilter(double chunks) {
		this.chunks = (int)chunks;
		reset();
	}
	
	@Override
	protected void apply(Observation<byte[]> subject) {
		byte[] data = subject.getData();
		// Initialize data cache
		if (this.chunksSoFar == 0) {
			int total_sz = (data.length + 1) * this.chunks;
			dataCache = new byte[total_sz];
		}
		
		// Copy data chunk
		System.arraycopy(data, 0, dataCache, indexSoFar, data.length);
		indexSoFar += data.length;
		this.chunksSoFar++;
		
		// Send data cache
		if (chunksSoFar == chunks) {
			byte[] sendData = new byte[indexSoFar];
			System.arraycopy(dataCache, 0, sendData, 0, indexSoFar);
			put(subject.copyWithNewData(sendData));
			reset();
		}
	}
	
	private void reset() {
		dataCache = null;
		this.chunksSoFar = 0;
		indexSoFar = 0;		
	}
}
