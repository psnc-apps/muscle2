package muscle.util.serialization;

import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author joris
 */
public class ModifiableInputStream extends InputStream {
	private InputStream in;
	
	public void setInputStream(InputStream newIn) {
		in = newIn;
	}
	
	public boolean hasInputStream() {
		return in != null;
	}

	@Override
	public int read() throws IOException {
		return in.read();
	}
	@Override
	public int read(byte[] data, int offset, int length) throws IOException {
		return in.read(data, offset, length);
	}
	
	@Override
	public int read(byte[] data) throws IOException {
		return in.read(data);
	}
	
	@Override
	public void close() throws IOException {
		in.close();
	}
	
	@Override
	public int available() throws IOException {
		return in.available();
	}
	
	@Override
	public long skip(long n) throws IOException {
		return in.skip(n);
	}
	
	@Override
	public void mark(int n) {
		in.mark(n);
	}
	
	@Override
	public boolean markSupported() {
		return in.markSupported();
	}
	
	@Override
	public void reset() throws IOException {
		in.reset();
	}
}
