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
