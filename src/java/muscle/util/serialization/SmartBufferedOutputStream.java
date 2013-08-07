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
//			byte[] printbytes = new byte[size];
//			System.arraycopy(data, 0, printbytes, 0, size);
//			System.out.println(Arrays.toString(printbytes));
			out.write(data, 0, size);
			size = 0;	
		}
	}

	@Override
	public void write(int b) throws IOException {
		//Logger.getLogger(SmartBufferedOutputStream.class.getName()).log(Level.INFO, "Writing {0} ({1} bytes in cache)", new Object[] {b, size});
		data[size] = (byte)(b & 0xff);
		size++;
		if (size == data.length) writeBuffer();
	}
	
	@Override
	public void write(byte[] b, int offset, int len) throws IOException {
		if (len > tradeoff_size || size + len > data.length) {
		 //   Logger.getLogger(SmartBufferedOutputStream.class.getName()).log(Level.INFO, "Writing {1} bytes in cache and {0} new bytes", new Object[] {len, size});
			writeBuffer();
			out.write(b, offset, len);
		} else if (len > 0) {
		  //  Logger.getLogger(SmartBufferedOutputStream.class.getName()).log(Level.INFO, "Copying {0} bytes to cache ({1} bytes already in cache)", new Object[] {len, size});
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
		//Logger.getLogger(SmartBufferedOutputStream.class.getName()).log(Level.INFO, "Flushing {0} bytes", size);
		writeBuffer();
		out.flush();
	}
	
	@Override
	public void close() throws IOException {
		//Logger.getLogger(SmartBufferedOutputStream.class.getName()).log(Level.INFO, "Closing stream (flushing {0} bytes)", size);
		writeBuffer();
		out.close();
	}
}
