/*
 * 
 */

package muscle.util.serialization;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

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
	//	Logger.getLogger(SmartBufferedInputStream.class.getName()).log(Level.INFO, "Copying {0} bytes (max {1})", new Object[]{len, size});
		final int cp = size >= len ? len : size;
		System.arraycopy(data, idx, b, offset, cp);
		size -= cp;
		idx += cp;
		return cp;
	}

	private int readFromStream(byte[] b, final int offset, final int atLeastLen, final int atMostLen) throws IOException {
		int readIdx = 0;
//		int sleepMilli;
//		int sleepNano;
//		if (atLeastLen < 10*1000) {
//			sleepNano = 0;
//			sleepMilli = 0;
//		} else {
//			sleepNano = (atLeastLen % 10000000)/10;
//			if (sleepNano == 0) sleepNano = 1;
//			sleepMilli = atLeastLen / 10000000;
//		}
		while (true) {
			final int ret = in.read(b, readIdx + offset, atMostLen - readIdx);
			if (ret == -1) {
				size = -1;
				return readIdx > 0 ? readIdx : -1;
			}

			readIdx += ret;
			if (readIdx >= atLeastLen)
				return readIdx;
			//Logger.getLogger(SmartBufferedInputStream.class.getName()).log(Level.INFO, "Blocking for {0} bytes ({1} read)", new Object[]{atLeastLen - readIdx, ret});
//			if (sleepNano > 0) {
//				Logger.getLogger(SmartBufferedInputStream.class.getName()).log(Level.INFO, "Sleeping {0} ms and {1} ns for {2} bytes ({3} read)", new Object[]{sleepMilli, sleepNano, atLeastLen, ret});
//				try {
//					Thread.sleep(sleepMilli, sleepNano);
//				} catch (InterruptedException ex) {
//					Thread.currentThread().interrupt();
//					return readIdx;
//				}
//			}
		}
	}
	
	@Override
	public int read(byte[] b, int offset, final int len) throws IOException {
	//	Logger.getLogger(SmartBufferedInputStream.class.getName()).log(Level.INFO, "Reading {0} bytes ({1} bytes in cache)", new Object[]{len, size});
		if (size == -1) return -1;
		if (len == 0) return 0;

		final int cp = copy(b, offset, len);
		if (cp == len) return cp;
		
		int newLen = len - cp;
		int newOffset = offset + cp;

		final int avail = in.available();
		if (newLen > tradeoff_size || newLen > avail - tradeoff_size) {
	//		Logger.getLogger(SmartBufferedInputStream.class.getName()).log(Level.INFO, "Reading {0} bytes without cache ({1} available)", new Object[]{newLen, avail});
			final int ret = readFromStream(b, newOffset, newLen, newLen);
			return (ret == -1 ? cp : ret + cp);
		} else {
	//	    Logger.getLogger(SmartBufferedInputStream.class.getName()).log(Level.INFO, "Reading {0} bytes of {1} available through cache", new Object[]{newLen, avail});
			final int ret = readFromStream(data, 0, newLen, data.length);
			if (size == -1) {
				return cp + copy(b, newOffset, ret);
			} else {
				idx = 0;
				size = ret;
				return cp + copy(b, newOffset, newLen);
			}
		}
	}

	public void close() throws IOException {
		size = -1;
		in.close();
	}

	@Override
	public int read() throws IOException {
	//	Logger.getLogger(SmartBufferedInputStream.class.getName()).log(Level.INFO, "Reading 1 bytes ({0} bytes in cache)", size);
		
		if (size == -1) return -1;
		if (size > 0) {
			size--;
		} else {
			final int bytesRead = readFromStream(data, 0, 1, data.length);
			if (bytesRead == -1) return -1;
			idx = 0;
			size = bytesRead - 1;
		}
		return data[idx++] & 0xff;
	}
	
	@Override
	public int available() throws IOException {
		if (size == -1) return 0;
		return size + in.available();
	}
	
	@Override
	public long skip(final long n) throws IOException {
		if (size == -1) {
			return 0;
		} else if (size < n) {
			final int tmpSize = size;
			size = 0;
			idx = 0;
			return in.skip(n - tmpSize) + tmpSize;
		} else {
			size -= n;
			idx += n;
			return n;
		}
	}
}
