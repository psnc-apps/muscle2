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
