/*
 * 
 */

package muscle.util.serialization;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Joris Borgdorff
 */
public class SmartBufferedOutputStream extends OutputStream {
	private final byte[] data;
	private final OutputStream out;
	private final int tradeoff_size;
	private int size;
	
	public SmartBufferedOutputStream(OutputStream original, int bufsize, float tradeoff_factor) {
		out = original;
		data = new byte[bufsize];
		tradeoff_size = (int) (bufsize / tradeoff_factor);
		size = 0;
	}
	
	private void writeBuffer() throws IOException {
		if (size > 0) {
			out.write(data, 0, size);
			size = 0;	
		}
	}

	@Override
	public void write(int b) throws IOException {
		data[size] = (byte)(b & 0xff);
		size++;
		if (size == data.length) writeBuffer();
	}
	
	@Override
	public void write(byte[] b, int offset, int len) throws IOException {
		if (len > tradeoff_size || size + len > data.length) {
			writeBuffer();
			out.write(b, offset, len);
		} else if (len > 0) {
		  	System.arraycopy(b, offset, data, size, len);
			size += len - offset;
		}
	}
	
	@Override
	public void write(byte[] b) throws IOException {
		write(b, 0, b.length);
	}
	
	@Override
	public void flush() throws IOException {
		writeBuffer();
		out.flush();
	}
	
	@Override
	public void close() throws IOException {
		writeBuffer();
		out.close();
	}
}
