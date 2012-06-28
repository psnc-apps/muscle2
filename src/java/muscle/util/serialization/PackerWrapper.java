/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package muscle.util.serialization;

import java.io.IOException;
import java.io.OutputStream;
import org.msgpack.packer.Packer;

/**
 *
 * @author Joris Borgdorff
 */
public class PackerWrapper implements SerializerWrapper {
	private final Packer packer;
	private final OutputStream stream;
	private final OutputStream socketStream;

	public PackerWrapper(Packer packer, OutputStream stream, OutputStream socketStream) {
		this.packer = packer;
		this.stream = stream;
		this.socketStream = socketStream;
	}

	@Override
	public void writeInt(int num) throws IOException {
		packer.write(num);
	}

	@Override
	public void writeBoolean(boolean bool) throws IOException {
		packer.write(bool);
	}

	@Override
	public void writeByteArray(byte[] bytes) throws IOException {
		packer.write(bytes);
	}

	@Override
	public void writeString(String str) throws IOException {
		packer.write(str);
	}

	@Override
	public void writeDouble(double d) throws IOException {
		packer.write(d);
	}

	@Override
	public void flush() throws IOException {
		packer.flush();
		stream.flush();
		socketStream.flush();
	}

	@Override
	public void close() throws IOException {
		packer.close();
	}
	
	public Packer getPacker() {
		return this.packer;
	}
}
