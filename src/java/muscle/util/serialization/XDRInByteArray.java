/*
 * 
 */

package muscle.util.serialization;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author Joris Borgdorff
 */
public class XDRInByteArray {
	private final byte[] buffer;
	private int idx, limit;
	private final InputStream in;
	
	public XDRInByteArray(InputStream in, int bufsize) {
		buffer = new byte[bufsize];
		idx = limit = 0;
		this.in = in;
	}
	
	public void read(int minimal) throws IOException {
		if (limit - idx < minimal) {
			if (idx > 0) {
				if (idx < limit) {
					System.arraycopy(buffer, idx, buffer, 0, limit - idx);
				}
				limit -= idx;
				idx = 0;
			}
			do {
				// Move the remaining buffer to the start
				final int bytesRead = in.read(buffer, limit, buffer.length - limit);
				if (bytesRead == -1) throw new EOFException("Can not fill buffer");
				limit += bytesRead;
			} while (limit < minimal);
		}
	}
	
	public long skip(long n) throws IOException {
		if (n <= size()) {
			idx += n;
			return n;
		} else {
			int skipped = size();
			idx = limit = 0;
			return skipped + in.skip(n - skipped);
		}
	}
	
	public long getLong() {
		return ((buffer[idx++] & 0xffL) << 56) | ((buffer[idx++] & 0xffL) << 48) | ((buffer[idx++] & 0xffL) << 40) | ((buffer[idx++] & 0xffL) << 32) | ((buffer[idx++] & 0xffL) << 24) | ((buffer[idx++] & 0xffL) << 16) | ((buffer[idx++] & 0xffL) << 8) | (buffer[idx++] & 0xffL);
	}
	
	public int getInt() {
		return ((buffer[idx++] & 0xff) << 24) | ((buffer[idx++] & 0xff) << 16) | ((buffer[idx++] & 0xff) << 8) | (buffer[idx++] & 0xff);
	}
	
	public short getShort() {
		idx += 4;
		return (short)(((buffer[idx - 2] & 0xff) << 8) | (buffer[idx - 1] & 0xff));
	}
	
	public byte getByte() {
		idx += 4;
		return buffer[idx - 1];
	}
	
	public int getPart(byte[] b, int offset, int length) throws IOException {
		if (idx == limit) {
			return in.read(b, offset, length);
		} else {
			final int cp = Math.min(length, limit - idx);
			System.arraycopy(buffer, idx, b, offset, cp);
			idx += cp;
			return cp;
		}
	}
	
	public int size() {
		return limit - idx;
	}
	
	public int remaining() {
		return buffer.length - limit;
	}
}
