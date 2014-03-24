package muscle.util.serialization;

import java.io.IOException;
import java.io.OutputStream;

/**
 *
 * @author joris
 */
public class ModifiableOutputStream extends OutputStream {
	private OutputStream out;
	
	public void setOutputStream(OutputStream newOut) {
		out = newOut;
	}

	public boolean hasOutputStream() {
		return out != null;
	}

	@Override
	public void write(int b) throws IOException {
		out.write(b);
	}
	@Override
	public void write(byte[] data, int offset, int length) throws IOException {
		out.write(data, offset, length);
	}
	
	@Override
	public void write(byte[] data) throws IOException {
		out.write(data);
	}
	
	@Override
	public void close() throws IOException {
		out.close();
	}
	
	@Override
	public void flush() throws IOException {
		out.flush();
	}
}
