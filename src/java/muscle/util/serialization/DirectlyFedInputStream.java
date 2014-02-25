package muscle.util.serialization;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author Joris Borgdorff
 */
public class DirectlyFedInputStream extends InputStream {
	private final byte[] data;
	private int idx, size;
	
	public DirectlyFedInputStream(byte[] buf) {
		data = buf;
		idx = size = 0;
	}
	
	@Override
	public int read(byte[] b) throws IOException {
		return read(b, 0, b.length);
	}
	
	@Override
	public int read(byte[] b, int offset, final int len) throws IOException {
		if (len == 0) return 0;
		if (size <= 0) {
			size = -1;
			return -1;
		}
		
		final int cp = Math.min(size, len);
		System.arraycopy(data, idx, b, offset, cp);
		size -= cp;
		idx += cp;
		return cp;
	}

	@Override
	public void close() throws IOException {
		size = 0;
	}

	@Override
	public int read() throws IOException {
		if (size > 0) {
			size--;
			return data[idx++] & 0xff;
		} else {
			size = -1;
			return -1;
		}
	}
	
	@Override
	public int available() throws IOException {
		if (size == -1) return 0;
		return size;
	}
	
	@Override
	public long skip(final long n) throws IOException {
		if (size == -1) throw new EOFException("Stream previously set to invalid");
		
		if (size >= n) {
			size -= n;
			idx += n;
			return n;
		} else {
			long skipped = size;
			size = idx = 0;
			return skipped;
		}
	}
	
	@Override
	public void reset() {
		reset(0);
	}
	public void reset(int sz) {
		idx = 0;
		size = sz;
	}
}
