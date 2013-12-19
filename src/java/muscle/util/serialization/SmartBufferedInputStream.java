/*
 * 
 */

package muscle.util.serialization;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author Joris Borgdorff
 */
public class SmartBufferedInputStream extends InputStream {
	private final byte[] data;
	private final InputStream in;
	private final int tradeoff_size;
	private int idx, size;
	
	public SmartBufferedInputStream(InputStream original, int bufsize, float tradeoff_factor) {
		in = original;
		data = new byte[bufsize];
		tradeoff_size = (int) (bufsize / tradeoff_factor);
		size = 0;
		idx = 0;
	}
	
	@Override
	public int read(byte[] b) throws IOException {
		return read(b, 0, b.length);
	}
	
	private int copy(byte[] b, final int offset, final int len) {
		if (size == 0) return 0;
		final int cp = Math.min(size, len);
		System.arraycopy(data, idx, b, offset, cp);
		size -= cp;
		idx += cp;
		return cp;
	}

	private int readFromStream(byte[] b, final int offset, final int atLeastLen, final int atMostLen) throws IOException {
		int readIdx = 0;

		do {
			final int ret = in.read(b, offset + readIdx, atMostLen - readIdx);
			if (ret == -1) {
				size = -1;
				return readIdx > 0 ? readIdx : -1;
			}

			readIdx += ret;
		} while (readIdx < atLeastLen);
		
		return readIdx;
	}
	
	@Override
	public int read(byte[] b, int offset, final int len) throws IOException {
		
		if (len == 0) return 0;
		if (size == -1) return -1;
		
		final int cp = copy(b, offset, len);
		if (cp == len) return cp;
		final int newLen = len - cp;
		final int newOffset = offset + cp;

		if (newLen > tradeoff_size) {
			final int ret = readFromStream(b, newOffset, newLen, newLen);
			return (ret == -1 ? cp : ret + cp);
		} else {
			final int ret = readFromStream(data, 0, newLen, data.length);
			if (size == -1) {
				final int newCp = cp + copy(b, newOffset, Math.min(ret, newLen));
				return newCp;
			} else {
				idx = 0;
				size = ret;
				final int newCp = cp + copy(b, newOffset, newLen);
				return newCp;
			}
		}
	}

	@Override
	public void close() throws IOException {
		size = 0;
		in.close();
	}

	@Override
	public int read() throws IOException {
		if (size > 0) {
			size--;
			return data[idx++] & 0xff;
		} else if (size == 0) {
			final int bytesRead = readFromStream(data, 0, 1, data.length);
			if (bytesRead == -1) {
				size = -1;
				return -1;
			} else {
				idx = 1;
				size = bytesRead - 1;
				return data[0] & 0xff;
			}
		} else {
			return -1;
		}
	}
	
	@Override
	public int available() throws IOException {
		if (size == -1) return 0;
		return size + in.available();
	}
	
	@Override
	public long skip(final long n) throws IOException {
		if (size == -1)
			throw new EOFException("Stream previously set to invalid");
		
		if (size >= n) {
			size -= n;
			idx += n;
			return n;
		} else {
			long remain = n - size;
			size = idx = 0;
			long inSkipped = -1;
			try {
				inSkipped = in.skip(remain);
			} catch (IOException ex) {
				// If the underlying stream does not support seek, or some other error which we won't handle, but which will come
				// up again when we try to read from the stream.
			}
			if (inSkipped > 0) {
				remain -= inSkipped;
			}
			if (remain > 0) {
				while (true) {
					int inRead = readFromStream(data, 0, (int)Math.min(data.length, remain), data.length);
					if (inRead == -1) {
						// on EOF, return the number of bytes successfully skipped
						return n - remain;
					} else if (inRead > remain) {
						// Succeeded, with some buffer to spare, this should be the most frequent outcome
						size -= remain;
						idx += remain;
						return n;
					} else if (inRead == remain) {
						// Succeeded, with no buffer left, outcome if the skipped bytes are the last in the file
						size = idx = 0;
						return n;
					} else if (inRead < remain) {
						// Read another buffer, outcome if the number of skipped bytes is larger than the buffer size
						size = idx = 0;
						remain -= inRead;
					}
				}
			}
			return n;
		}
	}
}
