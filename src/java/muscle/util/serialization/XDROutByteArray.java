/*
 * 
 */

package muscle.util.serialization;

import java.io.IOException;
import java.io.OutputStream;

/**
 *
 * @author Joris Borgdorff
 */
public class XDROutByteArray {
	private final byte[] buffer;
	private int idx;
	private final OutputStream out;
	
	public XDROutByteArray(OutputStream out, int bufsize) {
		buffer = new byte[bufsize];
		idx = 0;
		this.out = out;
	}
	
	public void put(long value) {
		buffer[idx++] = (byte)((value >> 56) & 0xffL);
		buffer[idx++] = (byte)((value >> 48) & 0xffL);
		buffer[idx++] = (byte)((value >> 40) & 0xffL);
		buffer[idx++] = (byte)((value >> 32) & 0xffL);
		buffer[idx++] = (byte)((value >> 24) & 0xffL);
		buffer[idx++] = (byte)((value >> 16) & 0xffL);
		buffer[idx++] = (byte)((value >>  8) & 0xffL);
		buffer[idx++] = (byte)( value        & 0xffL);
	}
	
	public void put(int value) {
		buffer[idx++] = (byte)((value >> 24) & 0xff);
		buffer[idx++] = (byte)((value >> 16) & 0xff);
		buffer[idx++] = (byte)((value >>  8) & 0xff);
		buffer[idx++] = (byte)(value & 0xff);
	}
	
	public void put(final short value) {
		buffer[idx++] = 0;
		buffer[idx++] = 0;
		buffer[idx++] = (byte)((value >>  8) & 0xff);
		buffer[idx++] = (byte)(value & 0xff);
	}
	
	public void put(byte value) {
		buffer[idx++] = 0;
		buffer[idx++] = 0;
		buffer[idx++] = 0;
		buffer[idx++] = value;		
	}
	
	public void put(byte[] b, final int offset, final int length) throws IOException {
		write();
		out.write(b, offset, length);
		int padding = (4 - (length & 3)) & 3;
		for (int i = 0; i < padding; i++) {
			buffer[idx++] = 0;
		}
	}
	
	public void write() throws IOException {
		if (idx > 0) {
			out.write(buffer, 0, idx);
			idx = 0;
		}
	}
	
	public void flush() throws IOException {
		write();
		out.flush();
	}
	
	public int size() {
		return idx;
	}
	
	public int remaining() {
		return buffer.length - idx;
	}
}
